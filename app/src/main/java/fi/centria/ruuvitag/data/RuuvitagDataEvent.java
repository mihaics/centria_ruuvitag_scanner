package fi.centria.ruuvitag.data;

import android.util.Base64;

import fi.centria.ruuvitag.support.Base91;

/**
 * Created by ISOHAJA on 4.1.2017.
 */

public class RuuvitagDataEvent
{
    private double humidity;
    private double temp;
    private double air_pressure;
    double time_elapsed;



    private double rssi;

    public RuuvitagDataEvent(){

    }

    public double getRssi() {
        return rssi;
    }

    public void setRssi(double rssi) {
        this.rssi = rssi;
    }
    public boolean parseRuuvitagDataFromB91(String data)
    {
        byte[] bData = Base91.decode(data.getBytes());
        int pData[] = new int[8];
        for (int i = 0; i < bData.length; i++)
            pData[i] = bData[i] & 0xFF;

        if(pData[0] != 1)
            return false;

        parseByteData(pData,1);

        return true;

    }

    public void parseRuuvitagDataFromB64(String data) {
        try
        {
            byte[] bData = Base64.decode(data,Base64.DEFAULT);
            int pData[] = new int[8];
            for (int i = 0; i < bData.length; i++)
                pData[i] = bData[i] & 0xFF;
//bdata must start 2
            parseByteData(pData,2);
        }
        catch(Exception e)
        {

        }
    }

    private void parseByteData(int[] pData, int ruuviTagFWVersion )
    {
        if(ruuviTagFWVersion == 1)
        {
            humidity = pData[1] * 0.5;
            double uTemp = (((pData[3] & 127) << 8) | pData[2]);
            double tempSign = (pData[3] >> 7) & 1;
            temp = tempSign == 0.00 ? uTemp / 256.0 : -1.00 * uTemp / 256.0;
            air_pressure = ((pData[5] << 8) + pData[4]) + 50000;
            air_pressure /= 100.00;
            time_elapsed = (pData[7] << 8) + pData[6];
        }
        else
        {
            humidity = pData[1] * 0.5;
            double uTemp = (((pData[2] & 127) << 8) | pData[3]);
            double  tempSign = (pData[2] >> 7) & 1;
            temp = tempSign == 0.00 ? uTemp / 256.0 : -1.00 * uTemp / 256.0;
            air_pressure = ((pData[4] << 8) + pData[5]) + 50000;
            air_pressure /= 100.00;
            time_elapsed = (pData[7] << 8) + pData[6];
        }



        this.setPressure(air_pressure);
        this.setHuminidity(humidity);
        this.setTemp(temp);
        this.time_elapsed = time_elapsed;
    }



    public double getHuminidity() {
        return humidity;
    }

    public void setHuminidity(double humidity) {
        this.humidity = humidity;
    }

    public double getTemperature() {
        return temp;
    }

    public void setTemp(double temp) {
        this.temp = temp;
    }

    public double getPressure() {
        return air_pressure;
    }

    public void setPressure(double air_pressure) {
        this.air_pressure = air_pressure;
    }
}
