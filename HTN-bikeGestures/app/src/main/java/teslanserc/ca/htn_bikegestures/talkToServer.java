package teslanserc.ca.htn_bikegestures;

import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

public class talkToServer {

    private String vehicleType;
    private static final String serverAddress = "http://10.21.213.29:5000/";
    private String macAddress;

    public talkToServer(String mac, String type) {
        macAddress = mac;
        vehicleType = type;
    }

    public int upload(double latitude, double longitude, double speed, String direction, int status) {

        String requestURL = serverAddress + String.format("upload/%s/%s/%f/%f/%f/%s/%d",
                macAddress, vehicleType, latitude, longitude, speed, direction, status);
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
        HttpConnectionParams.setSoTimeout(httpParams, 5000);
        HttpClient reqConn = new DefaultHttpClient(httpParams);

        HttpResponse httpResponse = null;

        try {
            HttpGet httpGet = new HttpGet(requestURL);
            httpResponse = reqConn.execute(httpGet);
            if (httpResponse == null) {
                return -1;
            }
            else {
                BufferedReader rd = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
                String response = rd.readLine();

                if (response == null || !response.equalsIgnoreCase("success") || response.isEmpty()) {
                    return -1;
                }
                else {
                    return 0;
                }
            }
        }
        catch (Exception e) {
            return -1;
        }

    }

    public String[][] retrieve(double latitude, double longitude) {

        String requestURL = serverAddress + String.format("retrieve/%f/%f", latitude, longitude);
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
        HttpConnectionParams.setSoTimeout(httpParams, 5000);
        HttpClient reqConn = new DefaultHttpClient(httpParams);

        HttpResponse httpResponse = null;

        try {
            HttpGet httpGet = new HttpGet(requestURL);
            httpResponse = reqConn.execute(httpGet);
            if (httpResponse == null) {
                return null;
            }
            else {
                BufferedReader rd = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
                String response = rd.readLine();

                if (response == null || response.equalsIgnoreCase("FAILURE") || response.isEmpty()) {
                    return null;
                }
                else {
                    List<String[]> rawVals = new ArrayList<String[]>();
                    while (response != null && response.length() > 0 && !response.isEmpty()) {
                        rawVals.add(response.split(","));
                        response = rd.readLine();
                    }
                    return rawVals.toArray(new String[rawVals.size()][]);
                }
            }
        }
        catch (Exception e) {
            //Toast toast = Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG);
            //toast.show();
            return null;
        }
    }
    
}
