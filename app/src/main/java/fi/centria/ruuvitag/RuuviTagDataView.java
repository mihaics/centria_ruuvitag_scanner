
package fi.centria.ruuvitag;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import pl.pawelkleczkowski.customgauge.CustomGauge;

/**
 * Created by admin on 09/12/16.
 */

public class RuuviTagDataView extends LinearLayout {


    int[] barColors = {
            Color.rgb(93,138,168),
            Color.rgb(227,38,54),
            Color.rgb(255,191,0 ),
            Color.rgb(164,198,57),
            Color.rgb(205,149,117),
            Color.rgb(0 ,128,0 ),
            Color.rgb(0,255,255),
            Color.rgb(127,255,212),
            Color.rgb(75,83,32),
            Color.rgb(233,214,107 ),
            Color.rgb(178,190,181),
            Color.rgb(165,42,420),
            Color.rgb(253,238,0),
            Color.rgb(0,127,255),
            Color.rgb(132,132,130),
            Color.rgb(255,225,53),
            Color.rgb(0,0,0),
            Color.rgb(254,111,94),
            Color.rgb(135,50,96),
            Color.rgb(181,166,66),
            Color.rgb(102 ,255,0),
            Color.rgb(0,66,37),
            Color.rgb(205,127,50),
            Color.rgb(165,42,42),
            Color.rgb(204,85,0),
            Color.rgb(0,204,153),
    };


    public static final int TEMPERATURE = 1;
    public static final int HUMINIDITY = 2;
    public static final int PRESSURE = 3;
    public static final int RSSI = 4;
    private TextView mViewTitle;

    public static  int KMaxX = 60*4;//60*12;
    public ArrayList<Date> xIndex = new ArrayList<Date>();


    private LineGraphSeries<DataPoint> mSeries1;

    HashMap<String,LineGraphSeries<DataPoint>> allSeries;

    private int type;
    private GraphView mGraph;
    private int newX = 0;


    public RuuviTagDataView(Context context) {
        super(context);
        init();
    }

    public RuuviTagDataView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RuuviTagDataView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init()
    {
        inflate(getContext(), R.layout.view_ruuvitag_data, this);
        mViewTitle = (TextView) findViewById(R.id.textViewTemperatureTitle);
        mGraph = (GraphView) findViewById(R.id.graph);

        mGraph.getViewport().setScrollable(false); // enables horizontal scrolling
        mGraph.getViewport().setScrollableY(false); // enables vertical scrolling
        mGraph.getViewport().setScalable(false); // enables horizontal zooming and scrolling
        mGraph.getViewport().setScalableY(false); // enables vertical zooming and scrolling

        allSeries = new HashMap<String,LineGraphSeries<DataPoint>>();



    }


    public void setType(int type)
    {
        this.type = type;
        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(mGraph);
        switch (type)
        {
            case TEMPERATURE:
                mViewTitle.setText("Temperature (C)");

                mGraph.getViewport().setYAxisBoundsManual(true);
                mGraph.getViewport().setMinY(-40);
                mGraph.getViewport().setMaxY(40);

                staticLabelsFormatter.setVerticalLabels(new String[] {"-40", "-20", "0", "20", "40"});
                mGraph.getGridLabelRenderer().setNumVerticalLabels(1);
                mGraph.getGridLabelRenderer().setVerticalLabelsColor(Color.BLACK);
                mGraph.getGridLabelRenderer().setVerticalLabelsVisible(true);

                mGraph.getGridLabelRenderer().setHorizontalLabelsVisible(true);
                mGraph.getGridLabelRenderer().setHorizontalLabelsColor(Color.BLACK);
                mGraph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
                break;
            case HUMINIDITY:
                mViewTitle.setText("Huminidity (%)");

                mGraph.getViewport().setYAxisBoundsManual(true);
                mGraph.getViewport().setMinY(0);
                mGraph.getViewport().setMaxY(100);

                staticLabelsFormatter.setVerticalLabels(new String[] {"0", "100"});
                mGraph.getGridLabelRenderer().setNumVerticalLabels(2);
                mGraph.getGridLabelRenderer().setVerticalLabelsColor(Color.BLACK);
                mGraph.getGridLabelRenderer().setVerticalLabelsVisible(true);

                mGraph.getGridLabelRenderer().setHorizontalLabelsVisible(true);
                mGraph.getGridLabelRenderer().setHorizontalLabelsColor(Color.BLACK);
                mGraph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);

                break;
            case PRESSURE:
                mViewTitle.setText("Air Pressure (hPA)");

                mGraph.getViewport().setYAxisBoundsManual(true);
                mGraph.getViewport().setMinY(950);
                mGraph.getViewport().setMaxY(1050);

                staticLabelsFormatter.setVerticalLabels(new String[] {"990", "995", "1000", "1005", "1010"});
                mGraph.getGridLabelRenderer().setNumVerticalLabels(1);
                mGraph.getGridLabelRenderer().setVerticalLabelsColor(Color.BLACK);
                mGraph.getGridLabelRenderer().setVerticalLabelsVisible(true);

                mGraph.getGridLabelRenderer().setHorizontalLabelsVisible(true);
                mGraph.getGridLabelRenderer().setHorizontalLabelsColor(Color.BLACK);
                mGraph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);

                break;
            case RSSI:
                mViewTitle.setText("RSSI (dbA)");

                mGraph.getViewport().setYAxisBoundsManual(true);
                mGraph.getViewport().setMinY(-100);
                mGraph.getViewport().setMaxY(0);

                staticLabelsFormatter.setVerticalLabels(new String[] {"-100","-50", "0"});
                mGraph.getGridLabelRenderer().setNumVerticalLabels(2);
                mGraph.getGridLabelRenderer().setVerticalLabelsColor(Color.BLACK);
                mGraph.getGridLabelRenderer().setVerticalLabelsVisible(true);

                staticLabelsFormatter.setHorizontalLabels(new String[] {"-100","-50", "0"});
                mGraph.getGridLabelRenderer().setNumHorizontalLabels(2);
                mGraph.getGridLabelRenderer().setHorizontalLabelsVisible(true);
                mGraph.getGridLabelRenderer().setHorizontalLabelsColor(Color.BLACK);

                mGraph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);

                break;
            default:
                break;
        }
    }


    public void update(int time, LoggedEvent data)
    {
        double value =  0;


        for(int i = 0; i < data.objects.size(); i++)
        {
            switch (type)
            {
                case TEMPERATURE:
                    value = data.objects.get(i).temp;
                    break;
                case HUMINIDITY:
                    value = data.objects.get(i).humidity;
                    break;
                case PRESSURE:
                    value = data.objects.get(i).air_pressure;
                    break;
                case RSSI:
                    value = data.objects.get(i).rssi;
                    break;
                default:
                    break;
            }

            String devId = data.objects.get(i).getId();
            if (allSeries.keySet().contains(data.objects.get(i).getId()))
            {
                if (newX < KMaxX)
                    allSeries.get(devId).appendData(new DataPoint(newX, value), false, newX);
                else
                    allSeries.get(devId).appendData(new DataPoint(newX, value), true, newX);

                allSeries.get(devId).setTitle("" + value);
            }
            else
            {
                LineGraphSeries<DataPoint> dps = new LineGraphSeries<DataPoint>();
                allSeries.put(devId, dps);
                allSeries.get(devId).appendData(new DataPoint(newX, value), false, KMaxX);
                allSeries.get(devId).setThickness(2);
                allSeries.get(devId).setColor(barColors[i]);

                allSeries.get(devId).setTitle("" + data.objects.get(i).air_pressure);
                mGraph.addSeries(allSeries.get(devId));
            }
        }

        mGraph.getLegendRenderer().setVisible(true);
        mGraph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);

        newX++;

        xIndex.add(data.dateTime);


        mGraph.getViewport().setXAxisBoundsManual(true);
        mGraph.getGridLabelRenderer().setHorizontalLabelsAngle(45);
        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(mGraph);

        if(newX < KMaxX)
        {
            mGraph.getViewport().setMinX(0);
            mGraph.getViewport().setMaxX(KMaxX);

        }
        else
        {
            mGraph.getViewport().setMinX(newX-KMaxX);
            mGraph.getViewport().setMaxX(newX);
        }

        mGraph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    Date lastest = xIndex.get(0);

               //     if(newX > KMaxX)
                 //       lastest = xIndex.get(newX);

                    Calendar c = new GregorianCalendar();
                    c.setTime(lastest);

                    c.add(Calendar.MINUTE, (int) value); // adds one hour

                    DateFormat formatter = new SimpleDateFormat("HH:mm");
                    String dateFormatted = formatter.format(c.getTime());


                    return dateFormatted;//super.formatLabel(value, isValueX);
                } else {
                    // show currency for y values
                    return "" + (int) value;//super.formatLabel(value, isValueX);
                }
            }
        });

        Date date = xIndex.get(newX-1);
        DateFormat formatter = new SimpleDateFormat("HH:mm");
        String dateFormatted = formatter.format(date);

        switch (type)
        {
            case TEMPERATURE:
                mViewTitle.setText( dateFormatted + " - Temperature (C);");
                break;
            case HUMINIDITY:
                mViewTitle.setText( dateFormatted + " - HUMINIDITY (%);");
                break;
            case PRESSURE:
                mViewTitle.setText( dateFormatted + " - Air Pressure (hPA);");
                break;
            case RSSI:
                mViewTitle.setText( dateFormatted + " - Air Pressure (dba);");
                break;
            default:
                break;
        }



    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
        this.setMeasuredDimension(parentWidth, (int) (parentWidth*(4.00/3.00)));
        this.setLayoutParams(new LinearLayout.LayoutParams(parentWidth,(int) (parentWidth*(4.00/3.00))));
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}