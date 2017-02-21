package fi.centria.ruuvitag.data;

import com.google.gson.Gson;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.Series;

import java.util.ArrayList;

public class RuuvitagObject
{


    long lastSeen;
    String id;
    private int color;
    private String url;
    private RuuvitagDataEvent lastData;
    private String deviceId;
    private double latitude;
    private double longitude;

    public RuuvitagObject(){


    }

    public long getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public void setColor(int color) {
        this.color = color;
    }

    public void addData(RuuvitagDataEvent dataEvent)
    {


        lastData = dataEvent;

    }

    public String getLastDataJSON()
    {
        RuuvitagObject tempObject = new RuuvitagObject();
        tempObject.setId(this.getId());
        tempObject.setLastSeen(this.getLastSeen());
        tempObject.addData(lastData);
        tempObject.setDeviceId(this.getDeviceId());
        tempObject.setLocation(this.latitude,this.longitude);


        return new Gson().toJson(tempObject);

    }

    public RuuvitagDataEvent getLastData()
    {
        return lastData;
    }

    public int getColor() {
        return color;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setLocation(double latitude, double longitude) {

        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceId() {
        return deviceId;
    }
}
