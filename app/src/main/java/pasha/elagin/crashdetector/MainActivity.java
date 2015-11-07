package pasha.elagin.crashdetector;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

public class MainActivity extends AppCompatActivity implements SensorEventListener, OnChartValueSelectedListener, View.OnClickListener {

    private SensorManager mSensorManager;
    Sensor mSenAccelerometer;
    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private float max_x, max_y, max_z;
    private static final int SHAKE_THRESHOLD = 6000;
    private int xPos;
    private int red;

    private TextView textAccelValues;
    private TextView textMaxAccelValues;

    ArrayList<Entry> valsComp1 = new ArrayList<>();
    ArrayList<Entry> valsComp2 = new ArrayList<>();
    ArrayList<Entry> valsComp3 = new ArrayList<>();

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

        Button buttonStart = (Button) findViewById(R.id.buttonStart);
        buttonStart.setOnClickListener(this);

        Button buttonStop = (Button) findViewById(R.id.buttonStop);
        buttonStop.setOnClickListener(this);

        Button buttonClear = (Button) findViewById(R.id.buttonClear);
        buttonClear.setOnClickListener(this);

        mChart = (LineChart) findViewById(R.id.chart);
        mChart.setOnChartValueSelectedListener(this);
        createLines();
    }

    private LineDataSet createLine(String label, ArrayList<Entry> list, int color) {
        LineDataSet dataSet = new LineDataSet(list, label);
        dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        dataSet.setColor(color);
        dataSet.setCircleColor(color);
        dataSet.setCircleSize(3f);
        return dataSet;
    }

    private void createLines() {
        ArrayList<LineDataSet> dataSets = new ArrayList<>();
        dataSets.add(createLine("X", valsComp1, Color.rgb(2, 250, 2)));
        dataSets.add(createLine("Y", valsComp2, Color.rgb(250, 2, 2)));
        dataSets.add(createLine("Z", valsComp3, Color.rgb(2, 2, 250)));

        ArrayList<String> xVals = new ArrayList<>();
        LineData data = new LineData(xVals, dataSets);
        mChart.setData(data);
        mChart.invalidate();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopAccel();
    }

    protected void startAccel() {
        mSensorManager.registerListener(this, mSenAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void stopAccel() {
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

                float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;

                if (speed > SHAKE_THRESHOLD) {

                }

                last_x = x;
                last_y = y;
                last_z = z;

                if (max_x < last_x)
                    max_x = last_x;

                if (max_y < last_y)
                    max_y = last_y;

                if (max_z < last_z)
                    max_z = last_z;

                textAccelValues.setText("X: " + last_x + " Y: " + last_y + " Z: " + last_z);
                textMaxAccelValues.setText("X: " + max_x + " Y: " + max_y + " Z: " + max_z);
                addEntry(last_x, last_y, last_z);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void addEntry(float valueX, float valueY, float valueZ) {
        LineData data = mChart.getData();
        if (data != null) {
            LineDataSet setX = data.getDataSetByIndex(0);
            LineDataSet setY = data.getDataSetByIndex(1);
            LineDataSet setZ = data.getDataSetByIndex(2);

            // add a new x-value first
            data.addXValue(setX.getEntryCount() + "");

            data.addEntry(new Entry(valueX, setX.getEntryCount()), 0);
            data.addEntry(new Entry(valueY, setY.getEntryCount()), 1);
            data.addEntry(new Entry(valueZ, setZ.getEntryCount()), 2);

            // let the chart know it's data has changed
            mChart.notifyDataSetChanged();

//            mChart.setVisibleXRangeMaximum(6);
//            mChart.setVisibleYRangeMaximum(15, YAxis.AxisDependency.LEFT);

//
//            // this automatically refreshes the chart (calls invalidate())
            mChart.moveViewTo(data.getXValCount() - 7, 50f, YAxis.AxisDependency.LEFT);
        }
    }

    private void addDataSet() {

        LineData data = mChart.getData();

        if (data != null) {

            int count = (data.getDataSetCount() + 1);

            // create 10 y-vals
            ArrayList<Entry> yVals = new ArrayList<Entry>();

            if (data.getXValCount() == 0) {
                // add 10 x-entries
                for (int i = 0; i < 10; i++) {
                    data.addXValue("" + (i + 1));
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

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.buttonStart:
                startAccel();
                break;
            case R.id.buttonStop:
                stopAccel();
                break;
            case R.id.buttonClear:
                clearData();
                break;
            default:
                Log.e("Startup", "Unknown button pressed");
                break;
        }
    }

    private void clearData() {
        LineData data = mChart.getData();
        if (data != null) {
            for (int i = 0; i < data.getDataSetCount(); i++)
                data.getDataSetByIndex(i).clear();
        }
        mChart.invalidate();
    }
}
