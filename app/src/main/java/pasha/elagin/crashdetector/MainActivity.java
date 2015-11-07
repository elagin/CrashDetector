package pasha.elagin.crashdetector;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SensorEventListener, OnChartValueSelectedListener {

    private SensorManager mSensorManager;
    Sensor mSenAccelerometer;
    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private float max_x, max_y, max_z;
    private static final int SHAKE_THRESHOLD = 6000;
    private ImageView imageView;
    private Canvas canvas;
    private int xPos;
    private int red;

    private TextView textAccelValues;
    private TextView textMaxAccelValues;

    ArrayList<Entry> valsComp1 = new ArrayList<>();
    ArrayList<Entry> valsComp2 = new ArrayList<>();

    LineChart mChart;

    int[] mColors = ColorTemplate.VORDIPLOM_COLORS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSenAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mSenAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        textAccelValues = (TextView) findViewById(R.id.textAccelValues);
        textMaxAccelValues = (TextView) findViewById(R.id.textMaxAccelValues);
        imageView = (ImageView) findViewById(R.id.imageView);
        Bitmap bitmap = Bitmap.createBitmap((int) getWindowManager()
                .getDefaultDisplay().getWidth(), (int) getWindowManager()
                .getDefaultDisplay().getHeight(), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        imageView.setImageBitmap(bitmap);

        mChart = (LineChart) findViewById(R.id.chart);
        mChart.setOnChartValueSelectedListener(this);
        mChart.setData(new LineData());
        mChart.invalidate();
    }

    protected void showLine( int startx, int starty, int endx, int endy, int red) {
        Paint paint = new Paint();
        paint.setColor(Color.rgb(red, 153, 255));
        paint.setStrokeWidth(5);
        canvas.drawLine(startx, starty, endx, endy, paint);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSenAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        LineDataSet setComp1 = new LineDataSet(valsComp1, "Company 1");
        setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);
        LineDataSet setComp2 = new LineDataSet(valsComp2, "Company 2");
        setComp2.setAxisDependency(YAxis.AxisDependency.LEFT);
        setComp2.setColor(Color.rgb(250, 2, 2));

        ArrayList<LineDataSet> dataSets = new ArrayList<>();
        dataSets.add(setComp1);
        dataSets.add(setComp2);

        ArrayList<String> xVals = new ArrayList<String>();
        xVals.add("1.Q"); xVals.add("2.Q"); xVals.add("3.Q"); xVals.add("4.Q");

        LineData data = new LineData(xVals, dataSets);
        mChart.setData(data);
        mChart.invalidate(); // refresh
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            long curTime = System.currentTimeMillis();

            if ((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                float speed = Math.abs(x + y + z - last_x - last_y - last_z)/ diffTime * 10000;

                if (speed > SHAKE_THRESHOLD) {

                }

                last_x = x;
                last_y = y;
                last_z = z;

                if(max_x< last_x)
                    max_x = last_x;

                if(max_y< last_y)
                    max_y = last_y;

                if(max_z< last_z)
                    max_z = last_z;

                textAccelValues.setText("X: " + last_x + " Y: " + last_y + " Z: " + last_z);
                textMaxAccelValues.setText("X: " + max_x + " Y: " + max_y + " Z: " + max_z);

                if(xPos == imageView.getWidth())
                    xPos = 0;
                if(red == 255)
                    red = 0;
                showLine(xPos, imageView.getHeight(), xPos, imageView.getHeight() - Math.round(last_z * 30), red);
                showLine(xPos, imageView.getHeight(), xPos, imageView.getHeight() - Math.round(last_x * 30), red);
                imageView.invalidate();

                xPos++;
                red++;

                addEntry(last_z, last_y);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void addEntry(float valueZ, float valueY) {
        LineData data = mChart.getData();
        if(data != null) {
            LineDataSet setZ = data.getDataSetByIndex(0);
            LineDataSet setY = data.getDataSetByIndex(1);

            // add a new x-value first
            data.addXValue(setZ.getEntryCount() + "");
            data.addXValue(setY.getEntryCount() + "");
            // choose a random dataSet

            data.addEntry(new Entry(valueZ, setZ.getEntryCount()), 0);
            data.addEntry(new Entry(valueY, setY.getEntryCount()), 1);

            // let the chart know it's data has changed
            mChart.notifyDataSetChanged();

//            mChart.setVisibleXRangeMaximum(6);
//            mChart.setVisibleYRangeMaximum(15, YAxis.AxisDependency.LEFT);

//
//            // this automatically refreshes the chart (calls invalidate())
            mChart.moveViewTo(data.getXValCount()-7, 50f, YAxis.AxisDependency.LEFT);
        }
    }

    private void addDataSet() {

        LineData data = mChart.getData();

        if(data != null) {

            int count = (data.getDataSetCount() + 1);

            // create 10 y-vals
            ArrayList<Entry> yVals = new ArrayList<Entry>();

            if(data.getXValCount() == 0) {
                // add 10 x-entries
                for (int i = 0; i < 10; i++) {
                    data.addXValue("" + (i+1));
                }
            }

            for (int i = 0; i < data.getXValCount(); i++) {
                yVals.add(new Entry((float) (Math.random() * 50f) + 50f * count, i));
            }

            LineDataSet set = new LineDataSet(yVals, "DataSet " + count);
            set.setLineWidth(2.5f);
            set.setCircleSize(4.5f);

            int color = mColors[count % mColors.length];

            set.setColor(color);
            set.setCircleColor(color);
            set.setHighLightColor(color);
            set.setValueTextSize(10f);
            set.setValueTextColor(color);

            data.addDataSet(set);
            mChart.notifyDataSetChanged();
            mChart.invalidate();
        }
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
        Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected() {

    }

    private LineDataSet createSet(String label) {
        LineDataSet set = new LineDataSet(null, label);
        set.setLineWidth(2.5f);
        set.setCircleSize(4.5f);
        set.setColor(Color.rgb(240, 99, 99));
        set.setCircleColor(Color.rgb(240, 99, 99));
        set.setHighLightColor(Color.rgb(190, 190, 190));
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setValueTextSize(10f);
        return set;
    }
}
