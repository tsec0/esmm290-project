package SystemA;

public class DataHelper {
    byte[] bytes;
    int id;
    long measurement;

    // helps in transforming bytes to integer
    DataHelper(byte[] bytes, int id) {
        this.bytes = bytes;
        this.id = id;
    }

    // helps in transforming bytes to long
    DataHelper(byte[] bytes, long measurement) {
        this.bytes = bytes;
        this.measurement = measurement;
    }
}
