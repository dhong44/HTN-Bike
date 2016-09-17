package teslanserc.ca.htn_bikegestures;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class Car extends Activity {
    TextView speedView;

    int speed;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car);

        speed = 50;

        speedView=(TextView)findViewById(R.id.speedView);

        speedView.setText(Integer.toString(speed)+" km/h");
    }
}
