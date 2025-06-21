package SystemA;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class FrameHelper {

    // IDs of fields for time, temperature, altitude, pressure and ratio
    byte[] timeStampID;
    byte[] temperatureID;
    byte[] altitudeID;
    byte[] pressureID;
    byte[] attitudeID;

    // the values of time temperature, altitude, pressure and ratio
    byte[] timeStampValue;
    byte[] temperatureValue;
    byte[] altitudeValue;
    byte[] pressureValue;
    byte[] attitudeValue;

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    public void setPressureValue(byte[] pressureValue) {
        this.pressureValue = pressureValue;
    }

    public void setAttitudeValue(byte[] attitudeValue) {
        this.attitudeValue = attitudeValue;
    }

    public void setTimeStampID(byte[] timeStampID) {
        this.timeStampID = timeStampID;
    }

    public void setTemperatureID(byte[] temperatureID) {
        this.temperatureID = temperatureID;
    }

    public void setAltitudeID(byte[] altitudeID) {
        this.altitudeID = altitudeID;
    }

    public void setPressureID(byte[] pressureID) {
        this.pressureID = pressureID;
    }

    public void setAttitudeID(byte[] attitudeID) {
        this.attitudeID = attitudeID;
    }

    public void setTimeStampValue(byte[] timeStampValue) {
        this.timeStampValue = timeStampValue;
    }

    public void setTemperatureValue(byte[] temperatureValue) {
        this.temperatureValue = temperatureValue;
    }

    public void setAltitudeValue(byte[] altitudeValue) {
        this.altitudeValue = altitudeValue;
    }

    public long convertBytesToLong(byte[] bytes) {
        long val = 0;
        for (int i = 0; i < bytes.length; i++) {
            val = val | (bytes[i] & 0xFF);
            if (i != bytes.length - 1) {
                val = val << 8;
            }
        }
        return val;
    }

    public long getPressure() {
        return convertBytesToLong(pressureValue);
    }

    public long getAttitude() {
        return convertBytesToLong(attitudeValue);
    }

    public long getTimestampInMillis() {
        return convertBytesToLong(timeStampValue);
    }

    public byte[] outputByteArray() throws IOException {
        outputStream.write(timeStampID);
        outputStream.write(temperatureValue);
        outputStream.write(altitudeID);
        outputStream.write(altitudeValue);
        outputStream.write(pressureID);
        outputStream.write(pressureValue);
        outputStream.write(temperatureID);
        outputStream.write(temperatureValue);
        if (attitudeID != null && attitudeID.length > 0) {
            outputStream.write(attitudeID);
            outputStream.write(attitudeValue);
        }
        return outputStream.toByteArray();
    }

}
