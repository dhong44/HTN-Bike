package teslanserc.ca.htn_bikegestures;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.HttpURLConnection;

public class talkToServer {

    private String vehicleType;
    private static final String serverAddress = "";
    private String macAddress;

    public talkToServer(String mac, String type) {
        macAddress = mac;
        vehicleType = type;
    }

    public int upload(double latitude, double longitude, double speed, int direction, int status) throws MalformedURLException, IOException{
        URL requestURL = new URL(serverAddress + String.format("upload/vehicletype=%s&id=%s&latitude=%f&longitude=%f&speed=%f&direction=%d&status=%d",
                vehicleType, macAddress, latitude, longitude, speed, direction, status));
        HttpURLConnection requestConnection = (HttpURLConnection) requestURL.openConnection();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(requestConnection.getInputStream()));
            String response = in.readLine();
            if (!response.equalsIgnoreCase("SUCCESS")) {
                return -1;
            }
            else {
                return 0;
            }
        }
        finally {
            requestConnection.disconnect();
        }
    }

    public String[][] retrieve(double latitude, double longitude, int direction) throws MalformedURLException, IOException {
        URL requestURL = new URL(serverAddress + String.format("retrieve/id=%s&latitude=%f&longitude=%f&direction=%d",
                macAddress, latitude, longitude, direction));
        HttpURLConnection requestConnection = (HttpURLConnection) requestURL.openConnection();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(requestConnection.getInputStream()));
            String response = in.readLine();
            if (!response.equalsIgnoreCase("FAILURE") && !response.isEmpty()) {
                String[][] rawVals;
                String[] rawValsVehicles = response.split("\n");
                rawVals = new String[rawValsVehicles.length][];
                for (int i = 0; i < rawValsVehicles.length; i++) {
                    rawVals[i] = rawValsVehicles[i].split(",");
                }
                return rawVals;
            }
            else {
                return null;
            }
        }
        finally {
            requestConnection.disconnect();
        }
    }
    
}
