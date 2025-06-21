package SystemA;

import java.io.IOException;
import java.nio.ByteBuffer;

public class TemperatureFilter extends FilterExtension {
    public void run() {
        // Next we write a message to the terminal as initiation
        System.out.print("\n" + this.getName() + " Reading temperature data! " + "\n");

        while (true) {
            try {
                Id data = readId(this.InputReadPortA);
                MeasurementDataHelper measurementData = readMeasurement(this.InputReadPortA);
                if (data.id == Ids.Temperature.ordinal()) {
                    double tempF = Double.longBitsToDouble(measurementData.measurement);
                    double tempC = (tempF - 32) * 5 / 9;
                    measurementData.measurement = Double.doubleToLongBits(tempC);
                    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
                    buffer.putLong(measurementData.measurement);
                    measurementData.bytes = buffer.array();
                } // if

                writeId(data.bytes);
                writeMeasurement(measurementData.bytes);
            } // try
            catch (EndOfStreamException | IOException e) {
                ClosePorts();
                System.out.print("\n" + this.getName() +
                        " Temperature Exiting..." +
                        " bytes read: " + bytesRead +
                        " bytes written: " + bytesWritten + "\n");
                break;
            } // catch
        } // while
    } // run
}
