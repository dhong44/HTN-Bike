package teslanserc.ca.htn_bikegestures;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;

<<<<<<< HEAD
public class MainActivity extends Activity {

    private long secSinceLastUpload;

    private double aX, aY, aZ, gLati, gLongi;

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private LocationManager locationManager;
    private Criteria criteria;
    private String provider;
    private myLocationListener myListener;
=======
import java.io.File;
>>>>>>> origin/master

public class MainActivity extends Activity {
    Button bike, car;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bike=(Button)findViewById(R.id.bikeButton);
        car=(Button)findViewById(R.id.carButton);

        bike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), Bike.class);
                startActivity(i);
            }
        });

        car.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), Car.class);
                startActivity(i);
            }
        });
    }
}
