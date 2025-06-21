package SystemC;

import SystemA.FilterExtension;
import SystemA.FrameHelper;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class AttPsiFilter extends FilterExtension {
    public AttPsiFilter(double pressureLimit, double attitudeLimit, int[] ids) {
        super(ids);
        this.pressureLimit = pressureLimit;
        this.attitudeLimit = attitudeLimit;
    }

    double pressureLimit;
    double attitudeLimit;

    ArrayList<FrameHelper> invalidFrames = new ArrayList<>();
    FrameHelper currentFrame;
    long firstValidPressure = -1;
    long secondValidPressure = -1;

    long firstValidAttitude = -1;
    long secondValidAttitude = -1;

    private void processFrame() throws EndOfStreamException, IOException {
        this.currentFrame = readFrame();
        double pressure = Double.longBitsToDouble(currentFrame.getPressure());
        pressure = Math.abs(pressure);
        double attitude = Double.longBitsToDouble(currentFrame.getAttitude());
        if (pressure > pressureLimit && attitude > attitudeLimit) {
            invalidFrames.add(currentFrame);
            processFrame();
        } else if (invalidFrames.isEmpty() || firstValidPressure == -1) {
            firstValidPressure = currentFrame.getPressure();
            firstValidAttitude = currentFrame.getAttitude();
        } else {
            secondValidPressure = currentFrame.getPressure();
            secondValidAttitude = currentFrame.getAttitude();
        }
    }

    private byte[] getValidMeasurementBytes(long firstValue, long secondValue) {
        double validMeasurement;
        if (secondValue == -1) {
            validMeasurement = Double.longBitsToDouble(firstValue);
        } else {
            double firstMeasurement = Double.longBitsToDouble(firstValue);
            double secondMeasurement = Double.longBitsToDouble(secondValue);
            validMeasurement = (firstMeasurement + secondMeasurement) / 2;
        }
        validMeasurement = -validMeasurement;
        long validMeasurementLng = Double.doubleToLongBits(validMeasurement);
        ByteBuffer pressureBuff = ByteBuffer.allocate(Long.BYTES);
        pressureBuff.putLong(validMeasurementLng);
        return pressureBuff.array();
    }

    private void resetInvalidFrameState() {
        invalidFrames.clear();
        if (secondValidPressure != -1) {
            firstValidPressure = secondValidPressure;
            firstValidAttitude = secondValidAttitude;
        }
        secondValidPressure = -1;
        secondValidAttitude = -1;
    }

    public void run() {
        System.out.print("\n" + this.getName() + " Reading pressure data " + "\n");

        while (true) {
            try {
                processFrame();
                if (!invalidFrames.isEmpty()) {
                    for (FrameHelper corrected : invalidFrames) {
                        corrected.setPressureID(getValidMeasurementBytes(firstValidPressure, secondValidPressure));
                        corrected.setAttitudeID(getValidMeasurementBytes(firstValidAttitude, secondValidAttitude));
                        writeFrame(corrected);
                    }
                    resetInvalidFrameState();
                }
                writeFrame(currentFrame);
            } // try

            catch (EndOfStreamException | IOException e) {
                if (!invalidFrames.isEmpty()) {
                    for (FrameHelper corrected : invalidFrames) {
                        corrected.setPressureID(getValidMeasurementBytes(firstValidPressure, secondValidPressure));
                        corrected.setAttitudeID(getValidMeasurementBytes(firstValidAttitude, secondValidAttitude));
                        try {
                            writeFrame(corrected);
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }
                ClosePorts();
                System.out.print("\n" + this.getName() +
                        " Attitude Pressure Exiting..." +
                        " bytes read: " + bytesRead +
                        " bytes written: " + bytesWritten + "\n");
                break;
            } // catch
        } // while
    }
}
