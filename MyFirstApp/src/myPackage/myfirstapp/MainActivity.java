package myPackage.myfirstapp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements SensorEventListener{

  private float mLastX, mLastY, mLastZ;
  private boolean mInitialized;
  private SensorManager mSensorManager;
  private LocationManager mLocationManager;
  private LocationListener mLocationListener;
  private Sensor mAccelerometer;
  private final float NOISE = (float) 0.8;
  static String IPaddr = "192.168.0.17";
  static String PortNo = "5000";
  public final String tag = "GPS/Accel Sensor";
  public final static String EXTRA_MESSAGE = "myPackage.myfirstapp.MESSAGE";
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    mInitialized = false;
    mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    mSensorManager.registerListener(this, mAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);
    mLocationListener = new MyLocationListener();
    mLocationManager.requestLocationUpdates(
        LocationManager.GPS_PROVIDER, 1000, 1 , 
        (android.location.LocationListener) mLocationListener);
    File file = new File(getFilesDir()+File.separator+"accel_log.txt");
    if (file.exists()) {
      file.delete();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }
  
  protected void onResume() {
    super.onResume();
    mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
  }

  protected void onPause() {
    super.onPause();
    
    mSensorManager.unregisterListener(this);
  }
  
  /** Called when the user clicks the Send button */
 public void sendLog(View view) {
      // Do something in response to button
    Intent intent = new Intent(this, DisplayMessageActivity.class);
    EditText editIP = (EditText) findViewById(R.id.enter_IP);
    String SERVER_IP = editIP.getText().toString();
    EditText editPort = (EditText) findViewById(R.id.enter_Port);
    String SERVERPORT = editPort.getText().toString() ;
    intent.putExtra(IPaddr, SERVER_IP);
    intent.getIntExtra(PortNo, 5000);
    startActivity(intent);
 }

  
  
  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void onSensorChanged(SensorEvent event) {
    // TODO Auto-generated method stub
    TextView tvX= (TextView)findViewById(R.id.x_axis);
    TextView tvY= (TextView)findViewById(R.id.y_axis);
    TextView tvZ= (TextView)findViewById(R.id.z_axis);
    float x = event.values[0];
    float y = event.values[1];
    float z = event.values[2];
    
    if (!mInitialized) {
            mLastX = x;
            mLastY = y;
            mLastZ = z;
            tvX.setText("0.0");
            tvY.setText("0.0");
            tvZ.setText("0.0");
            mInitialized = true;
    } else {
            float deltaX = mLastX - x;
            float deltaY = mLastY - y;
            float deltaZ = mLastZ - z;
            if (Math.abs(deltaX) < NOISE) deltaX = (float)0.0;
            if (Math.abs(deltaY) < NOISE) deltaY = (float)0.0;
            if (Math.abs(deltaZ) < NOISE) deltaZ = (float)0.0;
            mLastX = x;
            mLastY = y;
            mLastZ = z;
            
            tvX.setText(Float.toString(deltaX));
            tvY.setText(Float.toString(deltaY));
            tvZ.setText(Float.toString(deltaZ));
            Double timeStamp = (double) System.currentTimeMillis();
            timeStamp = timeStamp/1000;
            
            String logEntry =  "ACCELO: Time: " + timeStamp.toString() + " x: " + 
                Float.toString(deltaX) + " y: " + Float.toString(deltaY) + " z: " + Float.toString(deltaZ);
            
            int writtenBytes = 0;
            writtenBytes = appendLog(logEntry);
           
            Log.d(tag, "Accelo: Written Bytes = " + writtenBytes);
    }
  }
  
  public int appendLog(String text)
  {       
    File logFile = new File(getFilesDir()+File.separator+"accel_log.txt");
    
     if (!logFile.exists())
     {
        try
        {
           logFile.createNewFile();
        } 
        catch (IOException e)
        {
           // TODO Auto-generated catch block
           e.printStackTrace();
        }
     }
     try
     {
        //BufferedWriter for performance, true to set append to file flag
        BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true)); 
        buf.append(text);
        buf.newLine();
        buf.close();
        return text.length();
     }
     catch (IOException e)
     {
        // TODO Auto-generated catch block
        e.printStackTrace();
        return 0;
     }
  }

  public class MyLocationListener implements LocationListener{
    final String LOG_LABEL = "Location Listener>>";

    @Override
    public void onLocationChanged(Location location) {
      if (location != null) {
        //Toast.makeText(MainActivity.this, "Location changed : Lat: " + 
      //location.getLatitude() + " Lng: " + location.getLongitude(), Toast.LENGTH_SHORT).show();
        Double Lat = location.getLatitude();
        Double Lng = location.getLongitude();
        Double timeStamp = (double) System.currentTimeMillis();
        timeStamp = timeStamp/1000;
        String locEntry =  "GPS: Time: " + timeStamp.toString() + " LAT: " + Double.toString(Lat) + " LONG: " + Double.toString(Lng);
        int writtenBytes = 0;
        writtenBytes = appendLog(locEntry);
        Log.d(tag, "GPS: Written Bytes = " + writtenBytes);
        TextView tvLat= (TextView)findViewById(R.id.lat);
        TextView tvLong= (TextView)findViewById(R.id.lng);
        tvLat.setText(Double.toString(location.getLatitude()));
        tvLong.setText(Double.toString(location.getLongitude()));
    }
      
    }
    public void onProviderDisabled(String provider) {
      // TODO Auto-generated method stub
      
    }

    public void onProviderEnabled(String provider) {
      // TODO Auto-generated method stub
      
    }
    
    public void onStatusChanged(String provider, int status, Bundle extras) {
      // TODO Auto-generated method stub
      
    
    }
   }
}
  
  


