package fi.centria.ruuvitag.location;

import android.location.Location;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by visitor on 18.11.2016.
 */

public class LocationData extends Location
{
    public Calendar dateTime;

     public LocationData()
    {
        super("");

        dateTime = GregorianCalendar.getInstance();
    }

}
