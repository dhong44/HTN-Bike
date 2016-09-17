package teslanserc.ca.htn_bikegestures;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.widget.ImageView;
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
import java.util.Timer;
import java.util.TimerTask;

public class Car extends Activity {
    private SensorManager sensorManager;
    private LocationManager locationManager;

    private Criteria criteria;
    private String provider;
    private myLocationListener myListener;

    private double gLati, gLongi;
    private TextView tLati, tLongi;

    private Timer retrieveTimer;
    private Handler handler = new Handler();

    private talkToServer tts;

    boolean slowDown = false;

    ImageView carView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.INTERNET}, 1);
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.ACCESS_NETWORK_STATE}, 2);
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 3);
        }

        tLati=(TextView)findViewById(R.id.currentLati);
        tLongi=(TextView)findViewById(R.id.currentLongi);
        carView=(ImageView)findViewById(R.id.carView);

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
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        WebServiceTask wst = new WebServiceTask(WebServiceTask.GET_TASK, Car.this, "Posting data...");

                        wst.execute(new String[]{MainActivity.serverAddress+String.format("retrieve/%f/%f", gLati, gLongi)});

                        if (slowDown){
                            carView.setImageResource(R.drawable.car_red);
                        }
                        else {
                            carView.setImageResource(R.drawable.car_green);
                        }
                    }
                });
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

    public void handleResponse(String response) {
        try {
            Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();
            String[] bikes = response.split("\n");
            slowDown = false;
            if(bikes!=null){
                for (int i = 0; i < bikes.length; i++) {
                    Toast.makeText(getApplicationContext(), Integer.parseInt(bikes[0].split(",")[6]) + response, Toast.LENGTH_SHORT).show();
                    if (Integer.parseInt(bikes[0].split(",")[6]) > 0) {
                        slowDown = true;
                        break;
                    }
                }
            }
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
