package fi.centria.ruuvitag.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import fi.centria.ruuvitag.R;
import fi.centria.ruuvitag.data.RuuvitagObject;
import pl.pawelkleczkowski.customgauge.CustomGauge;



import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

import fi.centria.ruuvitag.R;
import fi.centria.ruuvitag.data.RuuvitagObject;
import pl.pawelkleczkowski.customgauge.CustomGauge;

/**
 * Created by ISOHAJA on 8.1.2017.
 */

public class BeaconsListAdapter  extends ArrayAdapter<RuuvitagObject> {

    public BeaconsListAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public BeaconsListAdapter(Context context, int resource, List<RuuvitagObject> items) {
        super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null)
        {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.listview_item, null);
        }

        RuuvitagObject p = getItem(position);

        if (p != null)
        {
            CustomGauge gaugeRssi = (CustomGauge) v.findViewById(R.id.gaugeRssi);
            TextView textViewRssi = (TextView) v.findViewById(R.id.textViewRssi);
            gaugeRssi.setValue((int)p.getLastData().getRssi());
            textViewRssi.setText(String.format("%d",(int)p.getLastData().getRssi()));

            TextView beaconTitle = (TextView) v.findViewById(R.id.beaconTitle);
            beaconTitle.setText(p.getId());
            beaconTitle.setTextColor(p.getColor());

            TextView textViewLastSeen   = (TextView) v.findViewById(R.id.textViewLastSeen);
            textViewLastSeen.setText(p.getUrl());

            CustomGauge gaugePressure = (CustomGauge) v.findViewById(R.id.gaugePressure);
            TextView textViewPressure = (TextView) v.findViewById(R.id.textViewPressure);
            gaugePressure.setValue((int)p.getLastData().getPressure());
            textViewPressure.setText(String.format("%1.1f",(int)p.getLastData().getPressure()));

            CustomGauge gaugeTemperature = (CustomGauge) v.findViewById(R.id.gaugeTemperature);
            TextView textViewTemperature = (TextView) v.findViewById(R.id.textViewTemperature);
            gaugeTemperature.setValue((int)p.getLastData().getTemperature());
            textViewTemperature.setText(String.format("%1.1f",p.getLastData().getTemperature()));

            CustomGauge gaugeHuminidity = (CustomGauge) v.findViewById(R.id.gaugeHuminidity);
            TextView textViewHuminidity = (TextView) v.findViewById(R.id.textViewHuminidity);
            gaugeHuminidity.setValue((int)p.getLastData().getHuminidity());
            textViewHuminidity.setText(String.format("%1.1f",p.getLastData().getHuminidity()));


        }

        return v;
    }

}