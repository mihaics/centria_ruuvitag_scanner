package fi.centria.ruuvitag.location;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;


public class GPSTrackingService extends Service implements LocationListener
{

    private LocationManager locationManager;
    private final IBinder binder = new LocalBinder();
    GPSTrackerInterface listener;

    public void setListener(GPSTrackerInterface listener) {
        this.listener = listener;
    }

    public class LocalBinder extends Binder {
        public GPSTrackingService getService() {
            // Return this instance of LocalService so clients can call public methods
            return GPSTrackingService.this;
        }
    }

    public interface GPSTrackerInterface{
        public void OnLocationChange(LocationData location);
    }




    @Override
    public IBinder onBind(Intent arg0)
    {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate()
    {

        startTracking(getApplicationContext(),listener);

    }

    @Override
    public void onDestroy()
    {
        stopTracking();
    }

    private void stopTracking()
    {
    }

    public void startTracking (Context c, GPSTrackerInterface listener)
    {
        this.listener = listener;


        locationManager = (LocationManager) c.getSystemService(Context.LOCATION_SERVICE);
        boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);


        if (!enabled)
        {

        }else
        {

            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            // Initialize the location fields
            if (location != null) {
                // System.out.println("Provider " + provider + " has been selected.");
                onLocationChanged(location);

                LocationData locationData = new LocationData();
                locationData.set(location);

                if(listener != null)
                    listener.OnLocationChange(locationData);
            }
            int MIN_UPDATE_TIME_MS = 500; //minimum time interval between location updates, in milliseconds
            int MIN_UPDATE_DISTANCE_M = 50; //minimum distance between location updates, in meters
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,MIN_UPDATE_DISTANCE_M,MIN_UPDATE_TIME_MS,this);
            //Criteria criteria = new Criteria();
            //locationManager.requestLocationUpdates(500,50,criteria,this);
        }
    }

    @Override
    public void onLocationChanged(Location location)
    {
        int lat = (int) (location.getLatitude());
        int lng = (int) (location.getLongitude());

        LocationData locationData = new LocationData();
        locationData.set(location);

        if(listener != null)
            listener.OnLocationChange(locationData);

      //  listener.onLocationChange(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String provider)
    {


    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
