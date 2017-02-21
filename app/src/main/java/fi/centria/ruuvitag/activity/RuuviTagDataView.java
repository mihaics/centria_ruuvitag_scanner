
package fi.centria.ruuvitag.activity;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
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

import fi.centria.ruuvitag.R;
import fi.centria.ruuvitag.data.DataSnapshot;
public class RuuviTagDataView extends LinearLayout {

    public static final int TEMPERATURE = 1;
    public static final int HUMIDITY = 2;
    public static final int PRESSURE = 3;
    private TextView mViewTitle;

    public static  int KMaxX = 60*2; //THIS IS THE MAX LOGGING WINDOW SIZE IN MINUTES
    public ArrayList<Date> xIndex = new ArrayList<Date>();

    HashMap<String,LineGraphSeries<DataPoint>> allSeries;

    private int type;
    private GraphView mGraph;
    private int newX = 0;

    String temperatureLabels[] = new String[] {"-30", "-15", "0", "30", "60"};
    static int KMinTemperature = -30;
    static int KMaxTemperature = 60;

    String humidityLabels[] = new String[] {"0", "100"};
    static int KMinHumidity = 0;
    static int KMaxHumidity = 100;

    String airPressureLabels[] = new String[] {"950", "975", "1000", "1025", "1050"};
    static int KMinAirPressure = 900;
    static int KMaxAirPressure = 1100;

    public RuuviTagDataView(Context context)
    {
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

        mGraph.getViewport().setScrollable(false);
        mGraph.getViewport().setScrollableY(false);
        mGraph.getViewport().setScalable(false);
        mGraph.getViewport().setScalableY(false);

        allSeries = new HashMap<String,LineGraphSeries<DataPoint>>();

    }


    public void setType(int type)
    {
        mGraph.setTitleColor(Color.WHITE);
        mViewTitle.setTextColor(getResources().getColor(R.color.graphPrimaryColor));
        mGraph.getGridLabelRenderer().setGridColor(getResources().getColor(R.color.graphPrimaryColor));
        mGraph.getGridLabelRenderer().setHorizontalLabelsColor(getResources().getColor(R.color.graphPrimaryColor));
        mGraph.getGridLabelRenderer().setVerticalLabelsColor(getResources().getColor(R.color.graphPrimaryColor));
        mGraph.getGridLabelRenderer().setHorizontalAxisTitleColor(getResources().getColor(R.color.graphPrimaryColor));
        mGraph.getGridLabelRenderer().setVerticalAxisTitleColor(getResources().getColor(R.color.graphPrimaryColor));

        mGraph.getGridLabelRenderer().setVerticalLabelsVisible(true);

        mGraph.getGridLabelRenderer().setHorizontalLabelsVisible(true);


        this.type = type;
        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(mGraph);
        switch (type)
        {
            case TEMPERATURE:
                mViewTitle.setText(getResources().getText(R.string.temperature_graph_title));

               // mGraph.getViewport().setYAxisBoundsManual(true);
                //mGraph.getViewport().setMinY(KMinTemperature);
                //mGraph.getViewport().setMaxY(KMaxTemperature);
                staticLabelsFormatter.setVerticalLabels(temperatureLabels);
                mGraph.getGridLabelRenderer().setNumVerticalLabels(1);

                break;
            case HUMIDITY:
                mViewTitle.setText(getResources().getText(R.string.humidity_graph_title));

              //  mGraph.getViewport().setYAxisBoundsManual(true);
               // mGraph.getViewport().setMinY(KMinHumidity);
               // mGraph.getViewport().setMaxY(KMaxHumidity);

                staticLabelsFormatter.setVerticalLabels(humidityLabels);
                mGraph.getGridLabelRenderer().setNumVerticalLabels(2);

                break;
            case PRESSURE:
                mViewTitle.setText(getResources().getText(R.string.air_pressure_graph_title));

             //   mGraph.getViewport().setYAxisBoundsManual(true);
             //   mGraph.getViewport().setMinY(KMinAirPressure);
             //   mGraph.getViewport().setMaxY(KMaxAirPressure);

                staticLabelsFormatter.setVerticalLabels(airPressureLabels);
                mGraph.getGridLabelRenderer().setNumVerticalLabels(1);

                break;
            default:
                break;
        }
        mGraph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
    }


    public void update(Date time, DataSnapshot data)
    {
        double value =  0;



        for(int i = 0; i < data.objects.size(); i++)
        {
            switch (type)
            {
                case TEMPERATURE:
                    value = data.objects.get(i).getLastData().getTemperature();
                    break;
                case HUMIDITY:
                    value = data.objects.get(i).getLastData().getHuminidity();
                    break;
                case PRESSURE:
                    value = data.objects.get(i).getLastData().getPressure();
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
                allSeries.get(devId).setColor(data.objects.get(i).getColor());

                allSeries.get(devId).setTitle("" + data.objects.get(i).getLastData().getPressure());
                mGraph.addSeries(allSeries.get(devId));
            }
        }

        mGraph.getLegendRenderer().setWidth(200);
        mGraph.getLegendRenderer().setVisible(false);
        mGraph.getLegendRenderer().setVisible(true);
        mGraph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        mGraph.requestLayout();
        mGraph.invalidate();
        newX++;

        xIndex.add(time);


        mGraph.getViewport().setXAxisBoundsManual(true);
        mGraph.getGridLabelRenderer().setHorizontalLabelsAngle(20);
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

        mGraph.getGridLabelRenderer().setNumHorizontalLabels(4);
        mGraph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX)
                {

                    Date xValue = xIndex.get(0);

                    Calendar c = new GregorianCalendar();
                    c.setTime(xValue);

                    c.add(Calendar.MILLISECOND, ((int)value)*RuuvitagScannerActivity.RUN_INTERVAL_MS); // adds one hour

                    DateFormat formatter = new SimpleDateFormat("HH:mm");
                    String dateFormatted = formatter.format(c.getTime());


                    return dateFormatted;
                } else {

                    return "" + (int) value;
                }
            }
        });

        Date date = xIndex.get(newX-1);
        DateFormat formatter = new SimpleDateFormat("HH:mm");
        String dateFormatted = formatter.format(date);

        switch (type)
        {
            case TEMPERATURE:
                mViewTitle.setText( dateFormatted + " - " + getResources().getString(R.string.temperature_graph_title));
                break;
            case HUMIDITY:
                mViewTitle.setText( dateFormatted + " - " + getResources().getString(R.string.humidity_graph_title));
                break;
            case PRESSURE:
                mViewTitle.setText( dateFormatted + " - " + getResources().getString(R.string.air_pressure_graph_title));
                break;
            default:
                break;
        }



    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        /*IMPROVE THIS WORK LANDSCAPE SCREEN AND TABLETS*/

        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
        this.setMeasuredDimension(parentWidth, (int) (parentWidth*(4.00/3.00)));
        this.setLayoutParams(new LinearLayout.LayoutParams(parentWidth,(int) (parentWidth*(4.00/3.00))));
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}