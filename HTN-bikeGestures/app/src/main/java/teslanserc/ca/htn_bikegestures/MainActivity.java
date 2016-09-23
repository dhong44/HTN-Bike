package teslanserc.ca.htn_bikegestures;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {
    Button bike, car;
    public static final String serverAddress = "http://10.21.213.29:5000/";

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
