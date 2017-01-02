package fi.centria.ruuvitag;

/**
 * Created by admin on 08/12/16.
 */

public class PostObject
{

    double latitude;
    double longitude;
    double humidity;
    double temp;
    double air_pressure;

    double time_elapsed;

    String id;
    String type;
    String version;
    String jsonData;
    public long time;
    public long rssi;

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }

    public void parseRuuvitagData(String data)
    {
        byte[] bData = Base91.decode(data.getBytes());
        int pData[] = new int[8];
        for (int i = 0; i < bData.length; i++)
            pData[i] = bData[i] & 0xFF;

        double humidity = pData[1] * 0.5;
        //The bytes are swaped during the encoding, thus read byte 3 as first byte, byte 2 as 2nd
        //Same goes for pressure and time
        double uTemp = (((pData[3] & 127) << 8) | pData[2]);
        double tempSign = (pData[3] >> 7) & 1;
        double temp = tempSign == 0.00 ? uTemp / 256.0 : -1.00 * uTemp / 256.0;
        double air_pressure = ((pData[5] << 8) + pData[4]) + 50000;
        air_pressure /= 100.00;
        double time_elapsed = (pData[7] << 8) + pData[6];


        this.air_pressure = air_pressure;
        this.humidity = humidity;
        latitude = 63.815488;
        longitude = 23.130187;
        this.temp = temp;
        this.time_elapsed = time_elapsed;

    }
}
