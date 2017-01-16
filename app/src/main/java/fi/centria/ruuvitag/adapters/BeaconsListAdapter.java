package fi.centria.ruuvitag.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
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
import java.util.Locale;

import fi.centria.ruuvitag.R;
import fi.centria.ruuvitag.data.RuuvitagObject;
import pl.pawelkleczkowski.customgauge.CustomGauge;


/*

 TODO HOLDER SHOULD BE IMPLEMENTED IN HERE

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
        {   TextView beaconTitle = (TextView) v.findViewById(R.id.beaconTitle);
            beaconTitle.setTextColor(p.getColor());
            beaconTitle.setText(p.getId());

            long now = System.currentTimeMillis();

            TextView textViewLastSeen   = (TextView) v.findViewById(R.id.textViewLastSeen);
            Date expiry = new Date( p.getLastSeen());
            expiry.getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            String formatedText = sdf.format(expiry);
            textViewLastSeen.setText(p.getUrl()+ "  /  " + formatedText);
            if(now - p.getLastSeen() > 20 * 1000)
            {
                textViewLastSeen.setTextColor(Color.RED);

            }
            else
                textViewLastSeen.setTextColor(Color.WHITE);

            CustomGauge gaugeRssi = (CustomGauge) v.findViewById(R.id.gaugeRssi);
            TextView textViewRssi = (TextView) v.findViewById(R.id.textViewRssi);
            gaugeRssi.setValue((int)p.getLastData().getRssi());

            textViewRssi.setText(String.format("%d",(int)p.getLastData().getRssi()));


            CustomGauge gaugePressure = (CustomGauge) v.findViewById(R.id.gaugePressure);
            TextView textViewPressure = (TextView) v.findViewById(R.id.textViewPressure);
            gaugePressure.setValue((int)p.getLastData().getPressure());
            textViewPressure.setText(String.format("%1.1f",p.getLastData().getPressure()));
            textViewPressure.setTextColor(p.getColor());

            CustomGauge gaugeTemperature = (CustomGauge) v.findViewById(R.id.gaugeTemperature);
            TextView textViewTemperature = (TextView) v.findViewById(R.id.textViewTemperature);
            gaugeTemperature.setValue((int)p.getLastData().getTemperature());
            textViewTemperature.setText(String.format("%1.1f",p.getLastData().getTemperature()));
            textViewTemperature.setTextColor(p.getColor());

            CustomGauge gaugeHuminidity = (CustomGauge) v.findViewById(R.id.gaugeHuminidity);
            TextView textViewHuminidity = (TextView) v.findViewById(R.id.textViewHuminidity);
            gaugeHuminidity.setValue((int)p.getLastData().getHuminidity());
            textViewHuminidity.setText(String.format("%1.1f",p.getLastData().getHuminidity()));
            textViewHuminidity.setTextColor(p.getColor());

        }

        return v;
    }

}