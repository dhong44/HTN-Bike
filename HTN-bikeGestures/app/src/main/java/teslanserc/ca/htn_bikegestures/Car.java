package teslanserc.ca.htn_bikegestures;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class Car extends Activity {
    TextView speedView;

    int speed;

    long timeSinceLastRetrieve = 0;

    private SensorManager sensorManager;
    private LocationManager locationManager;

    private Criteria criteria;
    private String provider;
    private myLocationListener myListener;

    private double gLati, gLongi;
    private TextView tLati, tLongi;

    private Timer retrieveTimer;

    private talkToServer tts;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        speed = 50;

        tLati=(TextView)findViewById(R.id.currentLati);
        tLongi=(TextView)findViewById(R.id.currentLongi);
        speedView=(TextView)findViewById(R.id.speedView);

        speedView.setText(Integer.toString(speed)+" km/h");

        WifiManager manager=(WifiManager)getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = manager.getConnectionInfo();
        String macAddress=info.getMacAddress();

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

        tts = new talkToServer(macAddress, "car");

        retrieveTimer = new Timer();
        retrieveTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                String[][] bikes = tts.retrieve(gLati, gLongi);
                boolean slowDown = false;
                for (int i = 0; i < bikes.length; i++) {
                    if (bikes[i][6] > 0)
                }
            }
        }, 0, 5000);
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