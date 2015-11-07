package pasha.elagin.crashdetector;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

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
        Bitmap bitmap = Bitmap.createBitmap(imageView.getWidth(), imageView.getHeight(), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        imageView.setImageBitmap(bitmap);
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

                int stepX = imageView.getWidth() / 4;


                if(xPos == imageView.getWidth())
                    xPos = 0;
                if(red == 255)
                    red = 0;
                showLine(xPos, imageView.getHeight(), xPos, imageView.getHeight() - Math.round(last_z * 30), red);
                showLine(xPos, imageView.getHeight(), xPos, imageView.getHeight() - Math.round(last_x * 30), red);
                xPos++;
                red++;
                imageView.invalidate();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
