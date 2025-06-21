package SystemA;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.util.stream.IntStream;

public class FilterExtension extends FilterFramework {

    /*
    * bytesRead - Number of bytes read from the input file
    * bytesWritten - Number of bytes written to the stream.
    * dataByte - byte data read from the stream
    * measurements - store all measurements
    * id - id of measurement
    */
    protected DataInputStream inputDataStream;
    protected int bytesRead = 0;
    protected int bytesWritten = 0;
    protected byte dataByte = 0;
    private long measurement;
    private int id;

    private int[] idsToRead = {};

    public FilterExtension() {}

    public FilterExtension(int[] ids) {
        idsToRead = ids;
    }

    // function to read data from stream
    private DataHelper readData(int dataLength, PipedInputStream InputReadPort) throws
            EndOfStreamException, IOException {
        byte[] bytes = new byte[dataLength];
        for (int i = 0; i < dataLength; i++) {
            if (inputDataStream != null) {
                dataByte = inputDataStream.readByte();
            } else {
                // Read bytes from stream
                dataByte = ReadFilterInputPort(InputReadPort);
            }

            // Append the byte on ID
            if (dataLength == Integer.BYTES) {
                bytes[i] = dataByte;
                id = id | (dataByte & 0xFF);
            } else {
                bytes[i] = dataByte;
                if (inputDataStream == null) {
                    measurement = measurement | (dataByte & 0xFF);
                }
            }

            // If this is not the last byte, then slide
            if (i != dataLength - 1)
            {
                if (dataLength == Integer.BYTES) {
                    id = id << 8;
                } else if (inputDataStream == null) {
                    measurement = measurement << 8;
                }
            }
            bytesRead++;
        }

        DataHelper readData;
        if (dataLength == Integer.BYTES) {
            readData = new DataHelper(bytes, this.id);
            this.id = 0;
        } else {
            readData = new DataHelper(bytes, this.measurement);
            this.measurement = 0;
        }
        return readData;
    }

    protected void writeData(byte[] data) {
        for (byte datum : data) {
            WriteFilterOutputPort(datum);
            bytesWritten++;
        }
    }

    protected void writeId(byte[] data) {
        id = 0;
        writeData(data);
    }

    protected void writeMeasurement(byte[] data) {
        measurement = 0;
        writeData(data);
    }

    protected Id readId(PipedInputStream InputReadPort) throws
            EndOfStreamException, IOException {
        DataHelper readData = readData(Integer.BYTES, InputReadPort);
        return new Id(readData);
    }

    protected MeasurementDataHelper readMeasurement(PipedInputStream InputReadPort) throws
            EndOfStreamException, IOException {
        DataHelper readData = readData(Long.BYTES, InputReadPort);
        return new MeasurementDataHelper(readData);
    }

    protected FrameHelper readFrame(PipedInputStream inputReadPort) throws
            EndOfStreamException, IOException {
        FrameHelper readFrame = new FrameHelper();
        for (int i = 0; i < idsToRead.length; i++) {
            Id data = readId(inputReadPort);
            MeasurementDataHelper measurementData = readMeasurement(inputReadPort);
            if (IntStream.of(idsToRead).anyMatch(id -> id == data.id)) {

                if (data.id == Ids.Time.ordinal()) {
                    readFrame.setTimeStampID(data.bytes.clone());
                    readFrame.setTimeStampValue(measurementData.bytes.clone());

                } else if (data.id == Ids.Altitude.ordinal()) {
                    readFrame.setAltitudeID(data.bytes.clone());
                    readFrame.setAltitudeValue(measurementData.bytes.clone());

                } else if (data.id == Ids.Temperature.ordinal()) {
                    readFrame.setTemperatureID(data.bytes.clone());
                    readFrame.setTemperatureValue(measurementData.bytes.clone());

                } else if (data.id == Ids.Pressure.ordinal()) {
                    readFrame.setPressureID(data.bytes.clone());
                    readFrame.setPressureValue(measurementData.bytes.clone());

                } else if (data.id == Ids.Attitude.ordinal()) {
                    readFrame.setAttitudeID(data.bytes.clone());
                    readFrame.setAttitudeValue(measurementData.bytes.clone());
                }
            }
        }
        return readFrame;
    }

    //Read frame from first input port
    protected FrameHelper readFrame() throws EndOfStreamException, IOException {
        return this.readFrame(this.InputReadPortA);
    }

    protected void writeFrame(FrameHelper frame) throws IOException {
        byte[] outputData = frame.outputByteArray();
        for (byte dataByte : outputData) {
            WriteFilterOutputPort(dataByte);
        }
    }
}
