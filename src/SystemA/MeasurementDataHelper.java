package SystemA;

public class MeasurementDataHelper {
    public byte[] bytes;
    public long measurement;

    // byte to measurement
    MeasurementDataHelper(DataHelper readData) {
        this.bytes = readData.bytes;
        this.measurement = readData.measurement;
    }
}
