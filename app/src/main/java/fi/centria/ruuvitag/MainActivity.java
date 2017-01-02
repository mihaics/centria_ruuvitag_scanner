package fi.centria.ruuvitag;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.RemoteException;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.utils.UrlBeaconUrlCompressor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import pl.pawelkleczkowski.customgauge.CustomGauge;

import static android.R.attr.data;
import static android.R.attr.value;


public class MainActivity extends AppCompatActivity implements BeaconConsumer, DataLogger.DataLoggerSource {
    protected static final String TAG = "MonitoringActivity";
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private BeaconManager beaconManager;
    private ArrayList<PostObject> currentSensorInformation;
    private StableArrayAdapter adapter;

    RelativeLayout mainLayout;
    private RuuviTagDataView temperatureView;
    private RuuviTagDataView huminidityView;
    private RuuviTagDataView pressureView;
    private  ScrollView tagDataScrollView;

    private long updateMs;

    private TextView textView;
    private RuuviTagDataView rssiView;

    private Handler handler;
    private DataLogger dataLogger;

    @Override
    public ArrayList<PostObject> getData()
    {
        if(temperatureView != null)
        {
            if(dataLogger.events.size() > 0)
                temperatureView.update(dataLogger.timeElapsed,dataLogger.getData());
        }
        if(pressureView != null)
        {
            if(dataLogger.events.size() > 0)
                pressureView.update(dataLogger.timeElapsed,dataLogger.getData());
        }
        if(huminidityView != null)
        {
            if(dataLogger.events.size() > 0)
                huminidityView.update(dataLogger.timeElapsed,dataLogger.getData());
        }
        return currentSensorInformation;
    }

   private class StableArrayAdapter extends ArrayAdapter<PostObject>
   {
        Context context;
        int layoutResourceId;
        ArrayList<PostObject> data = null;

        public StableArrayAdapter(Context context, int layoutResourceId, ArrayList<PostObject> data) {
            super(context, layoutResourceId, data);
            this.layoutResourceId = layoutResourceId;
            this.context = context;
            this.data = data;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            View row = convertView;
            PostObjectHolder holder = null;

            if(row == null)
            {
                LayoutInflater inflater = ((Activity)context).getLayoutInflater();
                row = inflater.inflate(layoutResourceId, parent, false);

                holder = new PostObjectHolder();

                holder.txtTitle = (TextView)row.findViewById(R.id.secondLine);

                holder.txtRssi = (TextView)row.findViewById(R.id.textViewRssi);
                holder.txtTemperature = (TextView)row.findViewById(R.id.textViewTemperature);
                holder.txtHuminidity = (TextView)row.findViewById(R.id.textViewHuminidity);
                holder.txtPressure = (TextView)row.findViewById(R.id.textViewPressure);

                holder.gaugeRssi = (CustomGauge) row.findViewById(R.id.gaugeRssi);
                holder.gaugeTemperature = (CustomGauge)row.findViewById(R.id.gaugeTemperature);
                holder.gaugeHuminidity = (CustomGauge)row.findViewById(R.id.gaugeHuminidity);
                holder.gaugePressure = (CustomGauge)row.findViewById(R.id.gaugePressure);

                row.setTag(holder);
            }
            else
            {
                holder = (PostObjectHolder)row.getTag();
            }

            PostObject weather = data.get(position);
            holder.txtTitle.setText(weather.getId());

            holder.gaugeRssi.setValue((int) weather.rssi);
            holder.txtRssi.setText("" +  weather.rssi);

            holder.gaugeTemperature.setValue((int) weather.temp);
            holder.txtTemperature.setText("" +  weather.temp);

            holder.gaugeHuminidity.setValue((int) weather.humidity);
            holder.txtHuminidity.setText("" +  weather.humidity);

            holder.gaugePressure.setValue((int) weather.air_pressure);
            holder.txtPressure.setText("" +  weather.air_pressure);

            long now = System.currentTimeMillis();
            if(now - weather.time > 10 * 1000)
                holder.txtTitle.setTextColor(Color.RED);
            else
                holder.txtTitle.setTextColor(Color.GREEN);

            return row;
        }
    }

    static class PostObjectHolder
    {
        TextView txtTitle;

        TextView txtRssi;
        TextView txtTemperature;
        TextView txtHuminidity;
        TextView txtPressure;

        CustomGauge gaugeRssi;
        CustomGauge gaugeTemperature;
        CustomGauge gaugeHuminidity;
        CustomGauge gaugePressure;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode)
        {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    Log.d(TAG, "coarse location permission granted");
                    beaconManager = BeaconManager.getInstanceForApplication(this);

                    beaconManager.bind(this);
                } else
                {
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

    public void OnViewLogClick(View c)
    {
//        PostObject obj = adapter.getItem(position);




        mainLayout.addView(tagDataScrollView);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mainLayout = (RelativeLayout) findViewById(R.id.activity_main);

        beaconManager = BeaconManager.getInstanceForApplication(this);

        beaconManager.getBeaconParsers().clear();

        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(BeaconParser.EDDYSTONE_URL_LAYOUT));

        beaconManager.bind(this);

        final ListView listview = (ListView) findViewById(R.id.listview);

        currentSensorInformation = new ArrayList<PostObject>();

        adapter = new StableArrayAdapter(this, R.layout.listview_item, currentSensorInformation);
        listview.setAdapter(adapter);

        temperatureView = new RuuviTagDataView(getApplicationContext());
        temperatureView.setType(RuuviTagDataView.TEMPERATURE);
        huminidityView = new RuuviTagDataView(getApplicationContext());
        huminidityView.setType(RuuviTagDataView.HUMINIDITY);
        pressureView = new RuuviTagDataView(getApplicationContext());
        pressureView.setType(RuuviTagDataView.PRESSURE);


        tagDataScrollView = new ScrollView(getApplicationContext());
        tagDataScrollView.setBackgroundColor(getResources().getColor(android.R.color.white));
        tagDataScrollView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));

        LinearLayout ll = new LinearLayout(getApplicationContext());
        ll.setLayoutParams(new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT));
        ll.setOrientation(LinearLayout.VERTICAL);



        ll.addView(
                temperatureView,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT)
        );

        ll.addView(
                pressureView,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT)
        );

        ll.addView(
                huminidityView,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT)
        );


        Button btn = new Button(getApplicationContext());
        btn.setText("Close");
        ll.addView(
                btn,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT)
        );

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainLayout.removeView(tagDataScrollView);
            }
        });

        tagDataScrollView.addView(ll);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            // Android M Permission check 
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
        }

        final SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                beaconManager.setForegroundBetweenScanPeriod(progress*1000);
                try {
                    beaconManager.updateScanPeriods();
                    textView.setText("Time between BLE scans: " + progress + " s.");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        handler = new Handler();

        dataLogger = new DataLogger(60000,this);
        dataLogger.run();


    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        if(beaconManager != null)
         beaconManager.unbind(this);
    }
    @Override
    public void onBeaconServiceConnect()
    {
        beaconManager.setMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                Log.i(TAG, "I just saw an beacon for the first time!");
            }

            @Override
            public void didExitRegion(Region region) {
                Log.i(TAG, "I no longer see an beacon");
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                Log.i(TAG, "I have just switched from seeing/not seeing beacons: "+state);
            }
        });

        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region)
            {
                long now = System.currentTimeMillis();
                for (Beacon beacon: collection)
                {
                    for (PostObject po: currentSensorInformation)
                    {
                        if(po.getId().equalsIgnoreCase(beacon.getBluetoothAddress()))
                        {
                            po.time = now;
                        }

                    }
                }

                for (Iterator<PostObject> iterator = currentSensorInformation.iterator(); iterator.hasNext(); )
                {
                    PostObject po = iterator.next();
                    if(now - po.time > 20 * 1000) //REMOVE SENSOR IF NOT SEEN IN 20s.
                    {
                        iterator.remove();
                        return;
                    }
                }


                for (Beacon beacon: collection)
                {
                    if (beacon.getServiceUuid() == 0xfeaa && beacon.getBeaconTypeCode() == 0x10)
                    {
                        String url = UrlBeaconUrlCompressor.uncompress(beacon.getId1().toByteArray());

                        if(url.startsWith("https://ruu.vi"))
                        {
                            String data = url.split("#")[1];
                            final PostObject obj = new PostObject();
                            obj.parseRuuvitagData(data);

                            obj.rssi = beacon.getRssi();
                            obj.time = System.currentTimeMillis();
                            obj.setId(beacon.getBluetoothAddress());
                            String hex = Integer.toHexString(beacon.getServiceUuid());
                            obj.setType(hex);
                            obj.setVersion("1.0");

                            long timeNow = System.currentTimeMillis();

                            int i = 0;
                            boolean newBeacon = true;
                            for (Iterator<PostObject> iterator = currentSensorInformation.iterator(); iterator.hasNext(); )
                            {
                                PostObject po = iterator.next();
                                if (po.getId().equalsIgnoreCase(beacon.getBluetoothAddress()))
                                {
                                    iterator.remove();
                                    currentSensorInformation.add(i, obj);
                                    newBeacon = false;
                                    break;
                                }
                                i++;
                            }

                            if (newBeacon)
                                currentSensorInformation.add(obj);


                            runOnUiThread(new Runnable()
                            {
                                public void run()
                                {
                                    adapter.notifyDataSetChanged();
                                }
                            });


                        }
                    }
                }
                Log.d(TAG, "Number of beacons detected: "+collection.size());
            }
        });
        try
        {

            ArrayList<Identifier> identifiers = new ArrayList<Identifier>();
            identifiers.add(null);
            Region region = new Region("myRegion", identifiers);

          //  Region region = new Region("all-beacons-region", null, null, null);
            beaconManager.startRangingBeaconsInRegion(region);
            beaconManager.setForegroundBetweenScanPeriod(10000);
            beaconManager.setForegroundScanPeriod(3000);

            textView = (TextView) this.findViewById(R.id.textView);
            textView.setText("Time between BLE scans: 2s.");

        } catch (RemoteException e) {    }
    }

}

