package SystemB;

import SystemA.FilterExtension;
import SystemA.FrameHelper;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class PressureFilter extends FilterExtension {

    public PressureFilter(double upperLimit, double lowerLimit, int[] ids) {
        super(ids);
        this.upperLimit = upperLimit;
        this.lowerLimit = lowerLimit;
    }


    double lowerLimit;
    double upperLimit;
    ArrayList<FrameHelper> invalidFrames = new ArrayList<>();
    FrameHelper currentFrame;
    long firstValidPressure = -1;
    long secondValidPressure = -1;


    private void processFrame() throws EndOfStreamException, IOException {
        this.currentFrame = readFrame();
        double pressure = Double.longBitsToDouble(currentFrame.getPressure());
        if (pressure > upperLimit || pressure < lowerLimit) {
            invalidFrames.add(currentFrame);
            processFrame();
        } else if (invalidFrames.isEmpty() || firstValidPressure == -1) {
            firstValidPressure = currentFrame.getPressure();
        } else {
            secondValidPressure = currentFrame.getPressure();
        }
    }

    private byte[] getValidPressureBytes() {
        double validPressure;
        if (secondValidPressure == -1) {
            validPressure = Double.longBitsToDouble(firstValidPressure);
        } else {
            double firstPressure = Double.longBitsToDouble(firstValidPressure);
            double secondPressure = Double.longBitsToDouble(secondValidPressure);
            validPressure = (firstPressure + secondPressure) / 2;
        }
        validPressure = -validPressure;
        long validPressureLong = Double.doubleToLongBits(validPressure);
        ByteBuffer pressureBuff = ByteBuffer.allocate(Long.BYTES);
        pressureBuff.putLong(validPressureLong);
        return pressureBuff.array();
    }

    private void resetInvalidFrameState() {
        invalidFrames.clear();
        if (secondValidPressure != -1) {
            firstValidPressure = secondValidPressure;
        }
        secondValidPressure = -1;
    }


    public void run() {
        System.out.print("\n" + this.getName() + " Reading pressure data! " + "\n");

        while (true) {
            try {
                processFrame();
                if (!invalidFrames.isEmpty()) {
                    for (FrameHelper corrected : invalidFrames) {
                        corrected.setPressureValue(getValidPressureBytes());
                        writeFrame(corrected);
                    }
                    resetInvalidFrameState();
                }
                writeFrame(currentFrame);
            } // try

            catch (EndOfStreamException | IOException e) {
                if (!invalidFrames.isEmpty()) {
                    for (FrameHelper corrected : invalidFrames) {
                        corrected.setPressureValue(getValidPressureBytes());
                        try {
                            writeFrame(corrected);
                        } catch (IOException iox) {
                            throw new RuntimeException(iox);
                        }
                    }
                }
                ClosePorts();
                System.out.print("\n" + this.getName() +
                        " Pressure Exiting..." +
                        " bytes read: " + bytesRead +
                        " bytes written: " + bytesWritten + "\n");
                break;
            } // catch
        } // while
    }
}
