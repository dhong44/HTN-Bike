package teslanserc.ca.htn_bikegestures;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Bike extends Activity {

    private double aX, aY, aZ, mX, mY, mZ, gLati, gLongi, lastLat, lastLong, direction;

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

    private String macAddress;

    private int eventDetectionState = 0;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bike);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.INTERNET}, 1);
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
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

        lastLat=Double.NaN;
        lastLong=Double.NaN;

        WifiManager manager=(WifiManager)getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = manager.getConnectionInfo();
        macAddress=info.getMacAddress();

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

            eventDetectionState = evaluateSignalState(eventDetectionState, Math.sqrt(Math.pow(aX,2)+Math.pow(aY,2)+Math.pow(aZ,2)));

            /*if (System.currentTimeMillis() - lastTime > 5000)
            {
                if(Math.sqrt(Math.pow(aX,2)+Math.pow(aY,2)+Math.pow(aZ,2))>5){
                    if (!status) {
                        WebServiceTask wst = new WebServiceTask(WebServiceTask.GET_TASK, Bike.this, "Posting data...");

                        wst.execute(new String[]{MainActivity.serverAddress + String.format("upload/%s/%s/%f/%f/%f/%s/%d",
                                macAddress, "bike", gLati, gLongi, 0.000, getOrientation(direction), 1)});
                        Toast.makeText(getApplicationContext(), "1", Toast.LENGTH_SHORT).show();
                        status = !status;
                    }
                    else {
                        WebServiceTask wst = new WebServiceTask(WebServiceTask.GET_TASK, Bike.this, "Posting data...");

                        wst.execute(new String[]{MainActivity.serverAddress + String.format("upload/%s/%s/%f/%f/%f/%s/%d",
                                macAddress, "bike", gLati, gLongi, 0.000, getOrientation(direction), 0)});
                        Toast.makeText(getApplicationContext(), "0", Toast.LENGTH_SHORT).show();
                        status = !status;
                    }
                    lastTime = System.currentTimeMillis();
                }
            }*/
        }

        private int evaluateSignalState(int prevState, double accR) {
            int retState = prevState;
            switch (prevState) {
                case 0:
                    if (accR > 3) {
                        retState = 1;
                    }
                    break;
                case 1:
                    if (accR < 1) {
                        retState = 2;
                    }
                    break;
                case 2:
                    if (accR > 3) {
                        retState = 3;
                    }
                    break;
                case 3:
                    if (accR < 1) {
                        retState = 0;
                        if (!status) {
                            WebServiceTask wst = new WebServiceTask(WebServiceTask.GET_TASK, Bike.this, "Posting data...");

                            wst.execute(new String[]{MainActivity.serverAddress + String.format("upload/%s/%s/%f/%f/%f/%s/%d",
                                    macAddress, "bike", gLati, gLongi, 0.000, "N", 1)});
                            Toast.makeText(getApplicationContext(), "1", Toast.LENGTH_SHORT).show();
                            status = !status;
                        }
                        else {
                            WebServiceTask wst = new WebServiceTask(WebServiceTask.GET_TASK, Bike.this, "Posting data...");

                            wst.execute(new String[]{MainActivity.serverAddress + String.format("upload/%s/%s/%f/%f/%f/%s/%d",
                                    macAddress, "bike", gLati, gLongi, 0.000, "N", 0)});
                            Toast.makeText(getApplicationContext(), "0", Toast.LENGTH_SHORT).show();
                            status = !status;
                        }
                    }
                    break;
            }
            return retState;
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy){

        }
    }

    public String getOrientation(double angle){
        angle = Math.toDegrees(angle);
        if((int)angle>=-45&&(int)angle<45){
            return "E";
        }
        else if((int)angle>=45&&(int)angle<135){
            return "N";
        }
        else if((int)angle>=-135&&(int)angle<=-45){
            return "S";
        }
        else{
            return "E";
        }
    }

    private class myLocationListener implements LocationListener {
        public void onLocationChanged(Location location){
            if(lastLat==Double.NaN||lastLong==Double.NaN){
                lastLat=location.getLatitude();
                lastLong=location.getLongitude();
            }
            gLati=location.getLatitude();
            gLongi=location.getLongitude();
            tLati.setText(Double.toString(gLati));
            tLongi.setText(Double.toString(gLongi));

            direction=Math.atan2((gLati-lastLat),(gLongi-lastLong));
            lastLat=gLati;
            lastLong=gLongi;
        }

        public void onStatusChanged(String provider, int status, Bundle extras){

        }

        public void onProviderEnabled(String provider){

        }

        public void onProviderDisabled(String provider){

        }
    }

    public void handleResponse(String response) {
        try {

        } catch (Exception e) {
        }
    }

    private class WebServiceTask extends AsyncTask<String, Integer, String> {
        public static final int GET_TASK = 1;

        private static final int CONN_TIMEOUT = 3000;

        private static final int SOCKET_TIMEOUT = 5000;

        private int taskType = GET_TASK;
        private Context mContext = null;
        private String processMessage = "Processing...";

        private ProgressDialog pDlg = null;

        public WebServiceTask(int taskType, Context mContext, String processMessage) {
            this.taskType = taskType;
            this.mContext = mContext;
            this.processMessage = processMessage;
        }

        private void showProgressDialog() {
            pDlg = new ProgressDialog(mContext);
            pDlg.setMessage(processMessage);
            pDlg.setProgressDrawable(mContext.getWallpaper());
            pDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pDlg.setCancelable(false);
            pDlg.show();
        }

        @Override
        protected void onPreExecute() {
            showProgressDialog();
        }

        protected String doInBackground(String... urls) {
            String url = urls[0];
            String result = "";

            HttpResponse response = doResponse(url);

            if (response == null) {
                return result;
            } else {
                try {
                    result = inputStreamToString(response.getEntity().getContent());
                } catch (IOException e) {
                }
            }
            return result;
        }

        protected void onPostExecute(String response) {
            handleResponse(response);
            pDlg.dismiss();
        }

        private HttpParams getHttpParams() {
            HttpParams htpp = new BasicHttpParams();

            HttpConnectionParams.setConnectionTimeout(htpp, CONN_TIMEOUT);
            HttpConnectionParams.setSoTimeout(htpp, SOCKET_TIMEOUT);

            return htpp;
        }

        private HttpResponse doResponse(String url) {
            HttpClient httpclient = new DefaultHttpClient(getHttpParams());

            HttpResponse response = null;

            try {
                HttpGet httpget = new HttpGet(url);
                response = httpclient.execute(httpget);
            } catch (Exception e) {
            }

            return response;
        }

        private String inputStreamToString(InputStream is) {
            String line = "";
            StringBuilder total = new StringBuilder();

            BufferedReader rd = new BufferedReader(new InputStreamReader(is));

            try {
                while ((line = rd.readLine()) != null) {
                    total.append(line);
                }
            } catch (IOException e) {
            }

            return total.toString();
        }
    }
}
