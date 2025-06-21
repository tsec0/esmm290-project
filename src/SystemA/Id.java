package SystemA;

public class Id {
    public byte[] bytes;
    public int id;

    Id(DataHelper data) {
        this.bytes = data.bytes;
        this.id = data.id;
    }
}
