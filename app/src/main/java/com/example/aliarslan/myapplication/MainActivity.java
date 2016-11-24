package com.example.aliarslan.myapplication;

import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.motion.Smotion;
import com.samsung.android.sdk.motion.SmotionPedometer;

public class MainActivity extends AppCompatActivity {

    TextView debugTW, cX, cY;
    Boolean offsetter = true;
    int startX, startY; double distOffset;
    private Smotion mMotion;
    private SmotionPedometer mPedometer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        debugTW = (TextView)findViewById(R.id.debug_data);
        cX = (TextView)findViewById(R.id.coordX);
        cY = (TextView)findViewById(R.id.coordY);

        startX = 0;
        startY = 0;

        // initialize samsung motion
        mMotion = new Smotion();
        try {
            mMotion.initialize(this);
        } catch (IllegalArgumentException e) {
        } catch (SsdkUnsupportedException e) {
        }
        // Create SmotionPedometer instance
        mPedometer = new SmotionPedometer(Looper.getMainLooper(), mMotion);
        // Start Pedometer
        mPedometer.start(changeListener);


        // compass
        // step detection


        //

        Log.i("From main activity", "ffdhfdh");
    }


    final SmotionPedometer.ChangeListener changeListener = new
            SmotionPedometer.ChangeListener() {
                @Override
                public void onChanged(SmotionPedometer.Info info) {
                    // TODO Auto-generated method stub
                    SmotionPedometer.Info pedometerInfo = info;
                    double distance = pedometerInfo.getDistance();
                    setDistOffset(distance);
                    double speed = pedometerInfo.getSpeed();
                    long count = pedometerInfo.getCount(SmotionPedometer.Info.COUNT_TOTAL);
                    String debg = "Distance: "+Util.round(distance-distOffset, 2)+" Speed: "+speed+"\nSteps taken: "+count;
                    debugTW.setText(debg);
                    Log.i("Spedometer", debg);
                }
            };

    void updateDisplayedCoords(int x, int y) {
        cX.setText("Coordinate X: "+ x);
        cY.setText("Coordinate Y: "+ y);
    }
    void setDistOffset(double offset) {
        if (offsetter) {
            distOffset = offset;
            offsetter = false;
        }
    }
}

