package fi.centria.ruuvitag.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.support.v7.app.AlertDialog;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
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
import org.altbeacon.beacon.utils.UrlBeaconUrlCompressor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import fi.centria.ruuvitag.R;
import fi.centria.ruuvitag.adapters.BeaconsListAdapter;
import fi.centria.ruuvitag.data.DataSnapshot;
import fi.centria.ruuvitag.data.RuuvitagDataEvent;
import fi.centria.ruuvitag.data.RuuvitagObject;
import fi.centria.ruuvitag.networking.DweetIoConnector;

import fi.centria.ruuvitag.support.ColorsGenerator;
import pl.pawelkleczkowski.customgauge.CustomGauge;

public class RuuvitagScannerActivity extends Activity implements BeaconConsumer
{
    private BeaconsListAdapter beaconsAdapter;
    private Intent myServiceIntent;

    public static final int RUN_INTERVAL_MS = 1000;
    private Handler mHandler = new Handler();
    private Timer mTimer = null;
    ArrayList<Pair<Date,DataSnapshot>> historicalData;
    private RuuviTagDataView huminidityGraphView;
    private RuuviTagDataView pressureGraphView;
    private RuuviTagDataView temperatureGraphView;

    class TimeDisplayTimerTask extends TimerTask {

        @Override
        public void run() {
            // run on another thread
            mHandler.post(new Runnable() {

                @Override
                public void run()
                {

                    Date now = GregorianCalendar.getInstance().getTime();

                    DataSnapshot snapShot = new DataSnapshot();
                    snapShot.objects = beaconsInRange;

                    historicalData.add(new Pair<Date, DataSnapshot>(now,snapShot));

                    huminidityGraphView.update(now,snapShot);
                    pressureGraphView.update(now,snapShot);
                    temperatureGraphView.update(now,snapShot);

                }

            });
        }

    };


    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

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

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode)
        {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
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
        }
    }



    @Override
    protected void onPause()
    {


        if(!useBackgroundScan)
        {
            backgroundPowerSaver = null;
            beaconManager.unbind(this);
            beaconManager = null;
            if (mTimer != null)
            {
                mTimer.cancel();
                mTimer = null;
            }
            clearHistoryData();
        }

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

        historicalData = new   ArrayList<Pair<Date,DataSnapshot>>();

        state =  SCAN_STATE_STOPPED;
        backgroundPowerSaver = new BackgroundPowerSaver(this);
        beaconsInRange = new ArrayList<>();

        switchBackgrounState = (Switch) this.findViewById(R.id.switchBackgrounState);
        scanButton = (Button) this.findViewById(R.id.scanButton);
        progressBarScanning = (ProgressBar) this.findViewById(R.id.progressBarScanning);
        beaconsList = (ListView) this.findViewById(R.id.beaconsListView);

        progressBarScanning.setVisibility(View.GONE);

        switchBackgrounState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                disableScanning();

                if (isChecked)
                {
                    useBackgroundScan = true;
                } else
                {
                    useBackgroundScan = false;
                }
                initScanning();
            }
        });



        beaconsAdapter = new BeaconsListAdapter(this, R.layout.listview_item, this.beaconsInRange);

        beaconsList .setAdapter(beaconsAdapter);


        createGraphViews();


    }

    private void createGraphViews()
    {
        grahpViewsContainer = (ScrollView) this.findViewById(R.id.grapViewsLayout);

        LinearLayout ll = new LinearLayout(this);
        ll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        huminidityGraphView = new RuuviTagDataView(this);
        huminidityGraphView.setType(RuuviTagDataView.HUMINIDITY);
        ll.addView(huminidityGraphView);

        temperatureGraphView = new RuuviTagDataView(this);
        temperatureGraphView.setType(RuuviTagDataView.TEMPERATURE);
        ll.addView(temperatureGraphView);

        pressureGraphView = new RuuviTagDataView(this);
        pressureGraphView.setType(RuuviTagDataView.PRESSURE);
        ll.addView(pressureGraphView);

        Button closeButton = new Button(this);
        ll.addView(closeButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                grahpViewsContainer.setVisibility(View.GONE);
            }
        });

    }

    public void onClickshowGraphs(View view) {

        grahpViewsContainer.setVisibility(View.VISIBLE);
    }

    public void onClickScanButton(View view) {
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
        scanButton.setText("START SCANNING");
        progressBarScanning.setVisibility(View.GONE);


        backgroundPowerSaver = null;

        if(beaconManager != null)
        {
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
            Toast.makeText(this,"No permission to scan BLE devices",Toast.LENGTH_SHORT).show();
        }
        state = SCAN_STATE_STARTED;
        scanButton.setText("STOP SCANNING");
        progressBarScanning.setVisibility(View.VISIBLE);


            mTimer = new Timer();
            mTimer.scheduleAtFixedRate(new TimeDisplayTimerTask(), 0, RUN_INTERVAL_MS);


    }

    @Override
    @TargetApi(23)
    public void onResume()
    {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            // Android M Permission check 
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                    }
                });
                builder.show();
            }
            else
                scanPermissionOK = true;
        }
        scanPermissionOK = true;

        if(state == SCAN_STATE_STARTED)
        {
             mTimer = new Timer();
            mTimer.scheduleAtFixedRate(new TimeDisplayTimerTask(), 0, RUN_INTERVAL_MS);
        }
    }

    @Override
    public void onBeaconServiceConnect()
    {


        beaconManager.addRangeNotifier(new RangeNotifier()
        {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region)
            {
                long now = System.currentTimeMillis();

                for (Beacon beacon: collection)
                {
                    RuuvitagObject existingObject = null;

                    if (beacon.getServiceUuid() == 0xfeaa && beacon.getBeaconTypeCode() == 0x10)
                    {


                        String url = UrlBeaconUrlCompressor.uncompress(beacon.getId1().toByteArray());

                        if(url.startsWith("https://ruu.vi"))
                        {
                            processRuuviTag(url,beacon,now,existingObject);
                        }
                    }
                }

                for (Iterator<RuuvitagObject> iterator = beaconsInRange.iterator(); iterator.hasNext(); )
                {
                    RuuvitagObject po = iterator.next();
                    if(now - po.getLastSeen() > 20 * 1000) //REMOVE SENSOR IF NOT SEEN IN 20s.
                    {
                        iterator.remove();
                        return;
                    }
                }
            }
        });


        ArrayList<Identifier> identifiers = new ArrayList<Identifier>();
        identifiers.add(null);
        Region region = new Region("RuuvitagRegion", identifiers);

        beaconManager.setForegroundBetweenScanPeriod(10000);
        beaconManager.setForegroundScanPeriod(5000);

        if(useBackgroundScan)
        {
            beaconManager.setBackgroundBetweenScanPeriod(60000);
            beaconManager.setBackgroundMode(true);
            beaconManager.setBackgroundScanPeriod(5000);
        }
        else
        {

            beaconManager.setBackgroundMode(false);

        }
        try
        {
            beaconManager.startRangingBeaconsInRegion(region);
            state = SCAN_STATE_STARTED;
            scanningEnabled();
        }
        catch (RemoteException e)
        {
            e.printStackTrace();
        }


    }

    private void processRuuviTag(String url, Beacon beacon, long now, RuuvitagObject existingObject)
    {
        for (RuuvitagObject o: beaconsInRange)
        {
            if(o.getId().equalsIgnoreCase(beacon.getBluetoothAddress()))
            {
                o.setLastSeen(now);
                existingObject = o;
            }
        }

        if(existingObject == null)
        {
            existingObject = new RuuvitagObject();
            existingObject.setId(beacon.getBluetoothAddress());
            existingObject.setLastSeen(now);
            existingObject.setColor(ColorsGenerator.getColor(beaconsInRange.size()));
            beaconsInRange.add(existingObject);
        }

        String data = url.split("#")[1];

        RuuvitagDataEvent dataEvent = new RuuvitagDataEvent();
        dataEvent.setRssi(beacon.getRssi());

        existingObject.setUrl(url);
        try
        {

            if(!dataEvent.parseRuuvitagDataFromB91(data))
                dataEvent.parseRuuvitagDataFromB64(data);
        }
        catch (Exception e)
        {

        }
        existingObject.addData(dataEvent);

        DweetIoConnector dweetIoConnector = new DweetIoConnector();
        dweetIoConnector.postData(existingObject,getBaseContext());


        runOnUiThread(new Runnable()
        {
            public void run()
            {
                beaconsAdapter.notifyDataSetChanged();
            }
        });

    }


}
