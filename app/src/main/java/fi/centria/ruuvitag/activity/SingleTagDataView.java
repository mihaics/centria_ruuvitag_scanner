package fi.centria.ruuvitag.activity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import fi.centria.ruuvitag.R;
import fi.centria.ruuvitag.data.RuuvitagDataEvent;
import fi.centria.ruuvitag.data.RuuvitagObject;
import pl.pawelkleczkowski.customgauge.CustomGauge;

/**
 * Created by ISOHAJA on 30.1.2017.
 */

public class SingleTagDataView extends Fragment implements  UpdatableFragment {


    private int type;
    private double value;
    private TextView titleView;
    private CustomGauge gauge;
    private TextView valueView;
    private RelativeLayout layout;
    private RuuvitagDataEvent currentValues;


    public static SingleTagDataView newInstance(int type) {
        SingleTagDataView f = new SingleTagDataView();
        f.setType(type);
         return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.single_tag_view, container, false);

        this.gauge = (CustomGauge) rootView.findViewById(R.id.gauge);
        this.titleView = (TextView) rootView.findViewById(R.id.title);
        this.valueView = (TextView) rootView.findViewById(R.id.textViewValue);

        this.layout  = (RelativeLayout) rootView.findViewById(R.id.singleTagViewLayout);



        if(currentValues != null)
            setValue(currentValues);
        else
            setValue(SingleTagPagerAdapter.tagToMonitor.getLastData());

        return rootView;
    }



    public void setType(int type) {

        this.type = type;
    }

    public void setValue(RuuvitagDataEvent value)
    {
        currentValues = value;

        if(gauge == null)
            return;

        final RuuvitagDataEvent finalVal = value;

        getActivity().runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                double gaugeValue = 0;
                double min = -160.0f;
                double max = 0.0f;
                if(type == SingleTagPagerAdapter.RSSI)
                {
                    gaugeValue = finalVal.getRssi();
                    titleView.setText(getActivity().getResources().getString(R.string.rssi_graph_title));
                    min = -160.00;
                            max =0.0f;
                }
                if(type == SingleTagPagerAdapter.TEMPERATURE){
                    gaugeValue = finalVal.getTemperature();
                    titleView.setText(getResources().getString(R.string.temperature_graph_title));
                    min = -60.00;
                    max =60.0f;
                }
                if(type == SingleTagPagerAdapter.HUMIDITY){
                    gaugeValue = finalVal.getHuminidity();
                    titleView.setText(getResources().getString(R.string.humidity_graph_title));
                    min = 0.00;
                    max =100.0f;
                }
                if(type == SingleTagPagerAdapter.AIR_PRESSURE){
                    gaugeValue = finalVal.getPressure();
                    titleView.setText(getResources().getString(R.string.air_pressure_graph_title));
                    min = 800.00;
                    max =1200.00;
                }


                double percentage =  ((gaugeValue - min) * 100.00) / (max - min);
                gauge.setValue((int) percentage);
                valueView.setText(String.format("%1.1f",gaugeValue));

                //layout.setBackgroundColor(Color.argb(0x20,0x00,0xFF,0x00));
            }
        });

    }

    @Override
    public void update(RuuvitagObject beaconData)
    {
        final RuuvitagObject finalData  = beaconData;

        if(getActivity() == null)
            return;

        getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (finalData == null)
                                                layout.setBackgroundColor(Color.argb(0x20, 0xFF, 0x00, 0x00));
                                            else
                                                setValue(finalData.getLastData());
                                        }
                                    });


    }

}
