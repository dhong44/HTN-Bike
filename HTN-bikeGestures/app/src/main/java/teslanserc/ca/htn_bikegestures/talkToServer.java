package teslanserc.ca.htn_bikegestures;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

public class talkToServer {

    private String vehicleType;
    private static final String serverAddress = "http://10.21.48.47:5000/";
    private String macAddress;

    public talkToServer(String mac, String type) {
        macAddress = mac;
        vehicleType = type;
    }

    public int upload(double latitude, double longitude, double speed, String direction, int status) throws MalformedURLException, IOException{
        URL requestURL = new URL(serverAddress + String.format("upload/%s/%s/%f/%f/%f/%s/%d",
                macAddress, vehicleType, latitude, longitude, speed, direction, status));
        HttpURLConnection requestConnection = (HttpURLConnection) requestURL.openConnection();
        requestConnection.setRequestMethod("GET");
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(requestConnection.getInputStream()));
            String response = in.readLine();
            if (response == null || response.isEmpty() || !response.equalsIgnoreCase("SUCCESS")) {
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

    public String[][] retrieve(double latitude, double longitude) {
        HttpURLConnection requestConnection = null;
        try {
            URL requestURL = new URL(serverAddress + String.format("retrieve/%f/%f", latitude, longitude));
            requestConnection = (HttpURLConnection) requestURL.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(requestConnection.getInputStream()));
            List<String[]> rawVals = new ArrayList<String[]>();
            String response = in.readLine();
            if (response == null || response.equalsIgnoreCase("FAILURE") || response.isEmpty()) {
                return null;
            }
            while (response != null && response.length() > 0 && !response.isEmpty()) {
                rawVals.add(response.split(","));
                response = in.readLine();
            }
            return rawVals.toArray(new String[rawVals.size()][]);
        }
        catch (Exception e) {
            return null;
        }
        finally {
            if (requestConnection != null) {
                requestConnection.disconnect();
            }
        }
    }
    
}
