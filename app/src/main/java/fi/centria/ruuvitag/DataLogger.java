package fi.centria.ruuvitag;

import android.os.Handler;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ISOHAJA on 13.12.2016.
 */


class DataLogger
{
    private final Handler handler;



    public LoggedEvent getData()
    {
        return events.get(events.size()-1);
    }

    interface DataLoggerSource
    {
        public ArrayList<PostObject> getData();
    }

    public ArrayList<LoggedEvent> events;

    int sleepTime;
    int timeElapsed;
    DataLoggerSource dataSource;

    public DataLogger(int sleepTime, DataLoggerSource source)
    {
        this.sleepTime = sleepTime;
        dataSource = source;
        events = new ArrayList<LoggedEvent>();

        handler = new Handler();
    }


    public void run()
    { // run the service

        final Runnable r = new Runnable()
        {
            public void run()
            {
                ArrayList<PostObject> currentData = dataSource.getData();
                if(currentData != null && currentData.size() > 0)
                {
                    LoggedEvent event = new LoggedEvent();
                    event.dateTime = GregorianCalendar.getInstance().getTime();
                    event.time = timeElapsed;
                    event.objects = new ArrayList<>(currentData);//.subList(0,currentData.size());
                    events.add(event);
                }
                timeElapsed+=sleepTime;
                handler.postDelayed(this, sleepTime);
            }
        };

        handler.postDelayed(r,sleepTime);
    }

}