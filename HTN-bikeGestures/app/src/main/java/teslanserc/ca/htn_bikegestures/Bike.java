package teslanserc.ca.htn_bikegestures;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.TextView;

public class Bike extends Activity {

    private double aX, aY, aZ, mX, mY, mZ, gLati, gLongi;

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private LocationManager locationManager;
    private Criteria criteria;
    private String provider;
    private myLocationListener myListener;

    private TextView tX, tY, tZ, tMX, tMY, tMZ, tLati, tLongi;

    private talkToServer tts;

    private boolean status = false;

    private long lastTime;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bike);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.INTERNET}, 1);
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        tX=(TextView)findViewById(R.id.currentX);
        tY=(TextView)findViewById(R.id.currentY);
        tZ=(TextView)findViewById(R.id.currentZ);
        tMX=(TextView)findViewById(R.id.maxX);
        tMY=(TextView)findViewById(R.id.maxY);
        tMZ=(TextView)findViewById(R.id.maxZ);
        tLati=(TextView)findViewById(R.id.currentLati);
        tLongi=(TextView)findViewById(R.id.currentLongi);

        mX=0;
        mY=0;
        mZ=0;

        WifiManager manager=(WifiManager)getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = manager.getConnectionInfo();
        String macAddress=info.getMacAddress();

        sensorManager=(SensorManager)getSystemService(Context.SENSOR_SERVICE);
        accelerometer=sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorManager.registerListener(new mySensorListener(), accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        locationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
        criteria=new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        provider=locationManager.getBestProvider(criteria, false);

        Location location=locationManager.getLastKnownLocation(provider);

        myListener = new myLocationListener();

        if(location!=null){
            myListener.onLocationChanged(location);
        }
        else {
            Intent intent=new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }

        locationManager.requestLocationUpdates(provider, 200, 1, myListener);

        tts = new talkToServer("bike1", "bike");
        lastTime = System.currentTimeMillis();
    }

    private class mySensorListener implements SensorEventListener {

        public void onSensorChanged(SensorEvent event){
            aX=event.values[0];
            aY=event.values[1];
            aZ=event.values[2];

            if(mX<aX){
                mX=aX;
            }
            if(mY<aY){
                mY=aY;
            }
            if(mZ<aZ){
                mZ=aZ;
            }

            tX.setText(Double.toString(aX));
            tY.setText(Double.toString(aY));
            tZ.setText(Double.toString(aZ));

            tMX.setText(Double.toString(mX));
            tMY.setText(Double.toString(mY));
            tMZ.setText(Double.toString(mZ));

            if (System.currentTimeMillis() - lastTime > 5)
            {
                if(Math.sqrt(Math.pow(aX,2)+Math.pow(aY,2)+Math.pow(aZ,2))>5){
                    if (!status) {
                            tts.upload(gLati, gLongi, 0.000, "N", 1);
                    }
                    else {
                            tts.upload(gLati, gLongi, 0.000, "N", 0);
                    }
                }
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy){

        }
    }

    private class myLocationListener implements LocationListener {
        public void onLocationChanged(Location location){
            gLati=location.getLatitude();
            gLongi=location.getLongitude();
            tLati.setText(Double.toString(gLati));
            tLongi.setText(Double.toString(gLongi));
        }

        public void onStatusChanged(String provider, int status, Bundle extras){

        }

        public void onProviderEnabled(String provider){

        }

        public void onProviderDisabled(String provider){

        }
    }
}
