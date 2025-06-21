package SystemA;

import java.io.IOException;
import java.nio.ByteBuffer;

public class AltitudeFilter extends FilterExtension {
    public void run() {
        // Next we write a message to the terminal as an initiation
        System.out.print("\n" + this.getName() + " Reading altitude data! " + "\n");

        while (true) {
            try {
                Id data = readId(this.InputReadPortA);
                MeasurementDataHelper measurementData = readMeasurement(this.InputReadPortA);

                if (data.id == Ids.Altitude.ordinal()) {
                    double feetAlt = Double.longBitsToDouble(measurementData.measurement);
                    double meters = feetAlt * 0.3048; // conversion to feet
                    measurementData.measurement = Double.doubleToLongBits(meters);
                    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
                    buffer.putLong(measurementData.measurement);
                    measurementData.bytes = buffer.array();
                } // if
                writeId(data.bytes);
                writeMeasurement(measurementData.bytes);
            } // try
            catch (EndOfStreamException | IOException e) {
                ClosePorts();
                System.out.print("\n" +
                        this.getName() +
                        " Altitude Exiting..." +
                        " bytes read: " + bytesRead +
                        " bytes written: " + bytesWritten + "\n");
                break;
            } // catch
        } // while
    } // run
}
