package fi.centria.ruuvitag.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.service.scanner.NonBeaconLeScanCallback;
import org.altbeacon.beacon.utils.UrlBeaconUrlCompressor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import fi.centria.ruuvitag.R;
import fi.centria.ruuvitag.adapters.BeaconsListAdapter;
import fi.centria.ruuvitag.data.DataSnapshot;
import fi.centria.ruuvitag.data.RuuvitagDataEvent;
import fi.centria.ruuvitag.data.RuuvitagObject;
import fi.centria.ruuvitag.location.GPSTrackingService;
import fi.centria.ruuvitag.location.LocationData;
import fi.centria.ruuvitag.networking.DweetIoConnector;

import fi.centria.ruuvitag.networking.HttpResponseHandler;
import fi.centria.ruuvitag.support.ColorsGenerator;
import fi.centria.ruuvitag.support.DeviceIdGenerator;
import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;

public class RuuvitagScannerActivity extends FragmentActivity implements BeaconConsumer,GPSTrackingService.GPSTrackerInterface
{
    private static final String KBEACON_URL_TO_LOOK_FOR = "https://r/";
    private static final String KBEACON_URL_TO_LOOK_FOR2 = "https://ruu.vi/";
    private static final java.lang.String KURL_SEPARATOR = "/r/";//;/"#";
    private static final java.lang.String KURL_SEPARATOR2 = "#";
    private BeaconsListAdapter beaconsAdapter;
    private Intent myServiceIntent;

    public static final int RUN_INTERVAL_MS = 60*1000; // THIS IS THE INTERVAL FOR LOGGER TO RUN
    private Handler mHandler = new Handler();
    private Timer mTimer = null;
    ArrayList<Pair<Date,DataSnapshot>> historicalData;

    private RuuviTagDataView huminidityGraphView;
    private RuuviTagDataView pressureGraphView;
    private RuuviTagDataView temperatureGraphView;
    private HttpResponseHandler connectionHandler;

    private Region region;
    private LocationData lastKnowLocation;
    private ViewPager singleTagViewLayout;
    private SingleTagPagerAdapter singleTagViewAdapter;
    private long singleTagLastSeen;
    private double latitude;
    private double longitude;

    class TimeDisplayTimerTask extends TimerTask {

        @Override
        public void run() {
            // run on another thread
            mHandler.post(new Runnable() {

                @Override
                public void run()
                {

                    try {
                        Date now = GregorianCalendar.getInstance().getTime();

                        DataSnapshot snapShot = new DataSnapshot();
                        snapShot.objects = beaconsInRange;
                        snapShot.time = System.currentTimeMillis();

                        historicalData.add(new Pair<Date, DataSnapshot>(now, snapShot));

                        huminidityGraphView.update(now, snapShot);
                        pressureGraphView.update(now, snapShot);
                        temperatureGraphView.update(now, snapShot);
                    }catch (Exception e){}

                }

            });
        }

    };


    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final int PERMISSION_REQUEST_GPS_PERMISSION= 2;

    Switch switchBackgrounState;
    Button scanButton;
    ProgressBar progressBarScanning;
    ListView beaconsList;

    private int state;
    private static final int SCAN_STATE_STOPPED = 0;
    private static final int SCAN_STATE_STARTED = 1;
    private boolean scanPermissionOK;

    private BeaconManager beaconManager;
    ArrayList<RuuvitagObject> beaconsInRange;
    private boolean useBackgroundScan;
    private BackgroundPowerSaver backgroundPowerSaver;
    ScrollView grahpViewsContainer;
    GPSTrackingService bindedService;
    private RuuvitagScannerActivity meListener = this;

    protected ServiceConnection mServerConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            GPSTrackingService.LocalBinder binder = (GPSTrackingService.LocalBinder) service;
            bindedService = (GPSTrackingService)binder.getService();
            bindedService.setListener(meListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {

        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode)
        {
            case PERMISSION_REQUEST_COARSE_LOCATION:
            {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    scanPermissionOK = true;
                }
                else
                {
                    disableScanning();
                    scanPermissionOK = false;
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener()
                    {

                        @Override
                        public void onDismiss(DialogInterface dialog)
                        {
                        }

                    });
                    builder.show();
                }
                return;
            }
            case PERMISSION_REQUEST_GPS_PERMISSION:
            {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    // Permission granted.
                    Intent intent = new Intent(this, GPSTrackingService.class);
                    bindService(intent, mServerConn, Context.BIND_AUTO_CREATE);
                    startService(intent);

                } else
                {
                    // User refused to grant permission. You can add AlertDialog here
                    Toast.makeText(this, "You didn't give permission to access device location", Toast.LENGTH_LONG).show();
                }
            }
        }
    }



    @Override
    protected void onPause()
    {
        if(!useBackgroundScan)
        {
            ((Button)findViewById(R.id.showLogsbutton)).setText(getResources().getText(R.string.show_logs_button_title));
            grahpViewsContainer.setVisibility(View.GONE);

            backgroundPowerSaver = null;
            if(beaconManager != null)
            {
                try
                {
                    if (region != null)
                        beaconManager.stopRangingBeaconsInRegion(region);

                }catch(Exception e){}

                beaconManager.removeAllRangeNotifiers();
                beaconManager.unbind(this);
                beaconManager = null;
            }
                if (mTimer != null)
            {
                mTimer.cancel();
                mTimer = null;
            }
            clearHistoryData();
        }
        else if(state == SCAN_STATE_STARTED)
            beaconManager.setBackgroundMode(true);

        super.onPause();
    }

    private void clearHistoryData()
    {
        historicalData.clear();
    }

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        switchBackgrounState = (Switch) this.findViewById(R.id.switchBackgrounState);
        scanButton = (Button) this.findViewById(R.id.scanButton);
        progressBarScanning = (ProgressBar) this.findViewById(R.id.progressBarScanning);
        beaconsList = (ListView) this.findViewById(R.id.beaconsListView);
        singleTagViewLayout = (ViewPager) this.findViewById(R.id.SingleTagViewLayout);

        singleTagViewLayout.setVisibility(View.GONE);
        progressBarScanning.setVisibility(View.GONE);

        connectionHandler = new HttpResponseHandler();

        historicalData = new   ArrayList<Pair<Date,DataSnapshot>>();

        state =  SCAN_STATE_STOPPED;
        backgroundPowerSaver = new BackgroundPowerSaver(this);
        beaconsInRange = new ArrayList<>();



        switchBackgrounState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {

                if (isChecked)
                {
                    useBackgroundScan = true;
                } else
                {
                    useBackgroundScan = false;
                }
            }
        });

        beaconsAdapter = new BeaconsListAdapter(this, R.layout.listview_item, this.beaconsInRange);
        beaconsList .setAdapter(beaconsAdapter);
        beaconsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,int position, long id)
            {
                singleTagViewLayout.setVisibility(View.VISIBLE);

                singleTagLastSeen = System.currentTimeMillis();

                inflateSingleTagView(beaconsInRange.get(position));

                beaconManager.setForegroundBetweenScanPeriod(1000);
                beaconManager.setForegroundScanPeriod(1100);
                try {
                    beaconManager.updateScanPeriods();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                ((Button)findViewById(R.id.showLogsbutton)).setText(getText(R.string.close));


            }
        });


        createGraphViews();


        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Start location tracking?");
        alertDialog.setMessage("Enable location tracking, inorder to add attach Ruuvitags to your location?");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        SmartLocation.with(getApplicationContext()).location()
                                .oneFix()
                                .start(new OnLocationUpdatedListener() {
                                    @Override
                                    public void onLocationUpdated(Location location) {

                                        latitude = location.getLatitude();
                                        longitude = location.getLongitude();
                                    }
                                });
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();


        ((TextView)this.findViewById(R.id.textViewDeviceId)).setText(DeviceIdGenerator.id(this));
    }

    private void inflateSingleTagView(RuuvitagObject ruuvitagObject)
    {
        singleTagViewAdapter = new SingleTagPagerAdapter(this,getSupportFragmentManager());
        singleTagViewAdapter.setBeacon(ruuvitagObject);
        singleTagViewLayout.setAdapter(singleTagViewAdapter);


    }

    private void createGraphViews()
    {
        grahpViewsContainer = (ScrollView) this.findViewById(R.id.grapViewsLayout);

        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        huminidityGraphView = new RuuviTagDataView(this);
        huminidityGraphView.setType(RuuviTagDataView.HUMIDITY);
        ll.addView(huminidityGraphView);

        temperatureGraphView = new RuuviTagDataView(this);
        temperatureGraphView.setType(RuuviTagDataView.TEMPERATURE);
        ll.addView(temperatureGraphView);

        pressureGraphView = new RuuviTagDataView(this);
        pressureGraphView.setType(RuuviTagDataView.PRESSURE);
        ll.addView(pressureGraphView);


        grahpViewsContainer.addView(ll);


    }

    public void onClickshowGraphs(View view) {

        Button v = (Button) view;
        if(grahpViewsContainer.getVisibility() == View.GONE && singleTagViewLayout.getVisibility() == View.GONE)
        {
            v.setText(getText(R.string.close));
            grahpViewsContainer.setVisibility(View.VISIBLE);
//            grahpViewsContainer.bringToFront();

        }
        else
        {
            v.setText(getText(R.string.show_logs_button_title));
            grahpViewsContainer.setVisibility(View.GONE);
            singleTagViewLayout.setVisibility(View.GONE);
            if(beaconManager != null) {
                beaconManager.setForegroundBetweenScanPeriod(10000 - 600); //ms between scans
                beaconManager.setForegroundScanPeriod(600l); //If advertising period is 1000, then each scan should find all Ruuvitags

                beaconManager.setBackgroundBetweenScanPeriod(60000 - 600); //when in bacground update once / min
                beaconManager.setBackgroundScanPeriod(600l);
                try {
                    beaconManager.updateScanPeriods();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

        }


    }

    public void onClickScanButton(View view)
    {
        if (state == SCAN_STATE_STARTED)
            disableScanning();
        else
        {
            initScanning();
        }
    }

    private void initScanning()
    {
        beaconManager = BeaconManager.getInstanceForApplication(this);

        beaconManager.bind(this);
        beaconManager.getBeaconParsers().clear();


       beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_URL_LAYOUT));
       

        beaconManager.bind(this);

    }

    private void disableScanning()
    {
        state = SCAN_STATE_STOPPED;
        scanButton.setText(getText(R.string.start_scanning));
        progressBarScanning.setVisibility(View.GONE);


        backgroundPowerSaver = null;

        if(beaconManager != null)
        {
            try {
                if (region != null)
                    beaconManager.stopRangingBeaconsInRegion(region);
            }catch(Exception e){}

            beaconManager.removeAllRangeNotifiers();
            beaconManager.unbind(this);
            beaconManager = null;
        }

        if (mTimer != null)
        {
            mTimer.cancel();
            mTimer = null;
        }
    }

    private void scanningEnabled()
    {
        if(!scanPermissionOK)
        {
            Toast.makeText(this,getText(R.string.no_permission_to_scan_beacons),Toast.LENGTH_SHORT).show();
        }
        state = SCAN_STATE_STARTED;
        scanButton.setText(getText(R.string.stop_scanning));
        progressBarScanning.setVisibility(View.VISIBLE);


        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new TimeDisplayTimerTask(), 0, RUN_INTERVAL_MS);


    }

    @Override
    @TargetApi(23)
    public void onResume()
    {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            // Android M Permission check 
            if (!isPermissionsGranted(this,Manifest.permission.ACCESS_COARSE_LOCATION))
            {
               requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
            }
            else
                scanPermissionOK = true;

/*            if(isPermissionsGranted(this,android.Manifest.permission.ACCESS_FINE_LOCATION))
            {
                Intent intent = new Intent(this, GPSTrackingService.class);
                bindService(intent, mServerConn, Context.BIND_AUTO_CREATE);
                startService(intent);
            }
            else
                ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_GPS_PERMISSION);
                */

        }
        scanPermissionOK = true;

        if(state == SCAN_STATE_STARTED && !useBackgroundScan)
        {
            mTimer = new Timer();
            mTimer.scheduleAtFixedRate(new TimeDisplayTimerTask(), 0, RUN_INTERVAL_MS);
            initScanning();
        }
        if(beaconManager != null)
            beaconManager.setBackgroundMode(false);
    }

    @Override
    public void onBeaconServiceConnect()
    {


        beaconManager.addRangeNotifier(new RangeNotifier()
        {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region)
            {
                for (Beacon beacon: collection)
                {
                    if (beacon.getServiceUuid() == 0xfeaa && beacon.getBeaconTypeCode() == 0x10)
                    {
                        String url = UrlBeaconUrlCompressor.uncompress(beacon.getId1().toByteArray());
                          if(url.startsWith(KBEACON_URL_TO_LOOK_FOR) || url.startsWith(KBEACON_URL_TO_LOOK_FOR2))
                        {
                            processRuuviTag(url,beacon);
                        }
                    }
                }
            }
        });


        ArrayList<Identifier> identifiers = new ArrayList<Identifier>();
        identifiers.add(null);
        Random r = new Random();
        region = new Region("RuuvitagRegion_"+r.nextInt(1000), identifiers);

        beaconManager.setForegroundBetweenScanPeriod(10000-600); //ms between scans
        beaconManager.setForegroundScanPeriod(600l); //If advertising period is 1000, then each scan should find all Ruuvitags

        beaconManager.setBackgroundBetweenScanPeriod(60000 - 600); //when in bacground update once / min
        beaconManager.setBackgroundScanPeriod(600l);

        try
        {
            beaconManager.updateScanPeriods();
            beaconManager.startRangingBeaconsInRegion(region);
            state = SCAN_STATE_STARTED;
            scanningEnabled();
        }
        catch (RemoteException e)
        {
            e.printStackTrace();
        }


    }

    private void processRuuviTag(String url, Beacon beacon)
    {
        RuuvitagObject existingObject = null;
        boolean singleTagSeen = false;

        long now = System.currentTimeMillis();
        for (RuuvitagObject o: beaconsInRange)
        {
            if(o.getId().equalsIgnoreCase(beacon.getBluetoothAddress()))
            {
                o.setLastSeen(now);
                existingObject = o;
                break;
            }
        }



        Log.d(getClass().toString(),url);

        try
        {
            String data = null;

            if(url.startsWith(KBEACON_URL_TO_LOOK_FOR))
                data = url.split(KURL_SEPARATOR)[1];
            else
                data = url.split(KURL_SEPARATOR2)[1];

            if(existingObject == null)
            {
                existingObject = new RuuvitagObject();
                existingObject.setId(beacon.getBluetoothAddress());
                existingObject.setLastSeen(now);
                existingObject.setColor(ColorsGenerator.getColor(beaconsInRange.size()));
                beaconsInRange.add(existingObject);
            }

            RuuvitagDataEvent dataEvent = new RuuvitagDataEvent();
            dataEvent.setRssi(beacon.getRssi());

            existingObject.setLocation(latitude,longitude);
            existingObject.setDeviceId(DeviceIdGenerator.id(this));
            existingObject.setUrl(url);
            try
            {

                if(!dataEvent.parseRuuvitagDataFromB91(data))
                    dataEvent.parseRuuvitagDataFromB64(data);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            existingObject.addData(dataEvent);


            if(singleTagViewLayout.getVisibility() == View.VISIBLE && singleTagViewAdapter.getBeacon().getId().equalsIgnoreCase(existingObject.getId())) {
                singleTagViewAdapter.setBeacon(existingObject);
                    singleTagLastSeen = System.currentTimeMillis();


            }
            DweetIoConnector dweetIoConnector = new DweetIoConnector();
            dweetIoConnector.postData(existingObject,getBaseContext(),connectionHandler);
        }
        catch (Exception e){
            return;
        }

        if(singleTagViewLayout.getVisibility() == View.VISIBLE && System.currentTimeMillis() - singleTagLastSeen > 5000) {
            singleTagViewAdapter.setBeaconMissing();


        }

        runOnUiThread(new Runnable()
        {
            public void run()
            {
                beaconsAdapter.notifyDataSetChanged();
            }
        });

    }


    private Boolean isPermissionsGranted(Context c, String p)
    {
        return ContextCompat.checkSelfPermission(c, p) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void OnLocationChange(LocationData location)
    {
        lastKnowLocation = location;
    }


}
