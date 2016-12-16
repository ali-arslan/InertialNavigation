package com.example.aliarslan.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.GeomagneticField;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;
import android.widget.TextView;


import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.motion.Smotion;
import com.samsung.android.sdk.motion.SmotionPedometer;

public class MainActivity extends AppCompatActivity implements SensorEventListener, LocationListener {

    TextView debugTW, cX, cY, bear;
    Boolean offsetter = true;
    double currX, currY, prevX, prevY;
    double distOffset;
    private Smotion mMotion;
    private SmotionPedometer mPedometer;

    private Sensor sensor;
    private float altitude = 0;
    private SensorManager mSensorManager;

    // Gravity for accelerometer data
    private float[] gravity = new float[3];
    // magnetic data
    private float[] geomagnetic = new float[3];
    // Rotation data
    private float[] rotation = new float[9];
    // orientation (azimuth, pitch, roll)
    private float[] orientation = new float[3];
    // smoothed values
    private float[] smoothed = new float[3];
    // sensor manager
    private SensorManager sensorManager;
    // sensor gravity
    private Sensor sensorGravity;
    private Sensor sensorMagnetic;
    private GeomagneticField geomagneticField;
    private double bearing = 0;
    private RotationManager rotationManager;

    public static final String FIXED = "FIXED";
    // location min time
    private static final int LOCATION_MIN_TIME = 30 * 1000;
    // location min distance
    private static final int LOCATION_MIN_DISTANCE = 10;
    private LocationManager locationManager;
    private Location currentLocation;

    ImageView marker;

    int markerX = 200, markerY= 1331;

    void moveMarker(int x, int y) {
        markerX+=(x*25); // 65 px = 1 foot
        markerY+=(y*25);
        AbsoluteLayout.LayoutParams OBJ = new AbsoluteLayout.LayoutParams(320,420,markerX,markerY);
        marker.setLayoutParams(OBJ);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        debugTW = (TextView) findViewById(R.id.debug_data);
        cX = (TextView) findViewById(R.id.coordX);
        cY = (TextView) findViewById(R.id.coordY);
        bear = (TextView) findViewById(R.id.bearing);
        marker = (ImageView) findViewById(R.id.marker);
        AbsoluteLayout.LayoutParams OBJ = new AbsoluteLayout.LayoutParams(320,420,markerX,markerY);
        marker.setLayoutParams(OBJ);

        currX = 0;
        currY = 0;
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
//        moveMarker(3,0);

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
        // keep screen light on (wake lock light)
        rotationManager = new RotationManager();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // step detection


        //

        Log.i("From main activity", "ffdhfdh");
    }

    //    @Override
//    protected void onResume() {
//        super.onResume();
//
//        // for the system's orientation sensor registered listeners
//        mSensorManager.registerListener((SensorEventListener) this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME);
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//
//        // to stop the listener and save battery
//        mSensorManager.unregisterListener((SensorEventListener) this);
//    }
    @Override // battery saving
    protected void onStart() {
        super.onStart();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorGravity = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorMagnetic = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        // listen to these sensors
        sensorManager.registerListener(this, sensorGravity,
                SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorMagnetic,
                SensorManager.SENSOR_DELAY_NORMAL);

        // I forgot to get location manager from system service ... Ooops :D
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // request location data
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            int gi=0;
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    gi);
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                LOCATION_MIN_TIME, LOCATION_MIN_DISTANCE, this);

    // get last known position
    Location gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

    if (gpsLocation != null) {
        currentLocation = gpsLocation;
    } else {
        // try with network provider
        Location networkLocation = locationManager
                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        if (networkLocation != null) {
            currentLocation = networkLocation;
        } else {
            // Fix a position
            currentLocation = new Location(FIXED);
            currentLocation.setAltitude(1);
            currentLocation.setLatitude(31.5546);
            currentLocation.setLongitude(74.3572);
        }

        // set current location
        onLocationChanged(currentLocation);
    }
    }
    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        // used to update location info on screen
        geomagneticField = new GeomagneticField(
                (float) currentLocation.getLatitude(),
                (float) currentLocation.getLongitude(),
                (float) currentLocation.getAltitude(),
                System.currentTimeMillis());
    }

    @Override
    protected void onStop() {
        super.onStop();
        // remove listeners
        sensorManager.unregisterListener(this, sensorGravity);
        sensorManager.unregisterListener(this, sensorMagnetic);
    }
    //

    @Override
    public void onSensorChanged(SensorEvent event) {


        // get the angle around the z-axis rotated
//        float degree = Math.round(event.values[0]);
//
//        Log.i("ROT", String.valueOf(-degree));
        boolean accelOrMagnetic = false;

        // get accelerometer data
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // we need to use a low pass filter to make data smoothed
            smoothed = LowPassFilter.filter(event.values, gravity);
            gravity[0] = smoothed[0];
            gravity[1] = smoothed[1];
            gravity[2] = smoothed[2];
            accelOrMagnetic = true;

        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            smoothed = LowPassFilter.filter(event.values, geomagnetic);
            geomagnetic[0] = smoothed[0];
            geomagnetic[1] = smoothed[1];
            geomagnetic[2] = smoothed[2];
            accelOrMagnetic = true;
        }



        // get rotation matrix to get gravity and magnetic data
        SensorManager.getRotationMatrix(rotation, null, gravity, geomagnetic);
        // get bearing to target
        SensorManager.getOrientation(rotation, orientation);
        // east degrees of true North
        bearing = orientation[0];
        // convert from radians to degrees
        bearing = Math.toDegrees(bearing);

        // fix difference between true North and magnetical North
        if (geomagneticField != null) {
            bearing += geomagneticField.getDeclination();
        }

        // bearing must be in 0-360
        if (bearing < 0) {
            bearing += 360;
        }

        // update compass view
        rotationManager.setBearing((float) bearing);
//        Log.i("Rot", ""+rotationManager.bearing);
        bear.setText("Bearing: " + rotationManager.bearing);


        if (accelOrMagnetic) {
//            compassView.postInvalidate();
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD
                && accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            // manage fact that compass data are unreliable ...
            // toast ? display on screen ?
        }
    }


    final SmotionPedometer.ChangeListener changeListener = new
            SmotionPedometer.ChangeListener() {
                @Override
                public void onChanged(SmotionPedometer.Info info) {
                    // TODO Auto-generated method stub
                    SmotionPedometer.Info pedometerInfo = info;
                    double distance = pedometerInfo.getDistance();
                    setDistOffset(distance);
                    double speed = pedometerInfo.getSpeed   ();
                    long count = pedometerInfo.getCount(SmotionPedometer.Info.COUNT_TOTAL);
                    String debg = "  Distance: "+Util.round(distance-distOffset, 2)+"\n  Speed: "+speed;
                    debugTW.setText(debg);
//                    Log.i("Spedometer", debg);
                    resolveVector(distance-distOffset);
                }
            };

    Boolean initSet = true;
    void resolveVector(double distance) {
        double bearing = Double.parseDouble(Float.valueOf(rotationManager.bearing).toString());
        prevX = currX;
        prevY = currY;
        if (bearing >= 0 && bearing < 90) {
            currX =+ distance*(Math.sin(Math.toRadians(bearing)));
            currY =+ distance*Math.cos(Math.toRadians(bearing));
        }
        if (bearing >= 90 && bearing < 180) {
            currX =+ distance*Math.sin(Math.toRadians(bearing));
            currY =- distance*Math.cos(Math.toRadians(bearing));
        }
        if (bearing >= 180 && bearing < 270) {
            currX =- distance*Math.sin(Math.toRadians(bearing));
            currY =- distance*Math.cos(Math.toRadians(bearing));
        }
        if (bearing >= 270 && bearing < 360) {
            currX =- distance*Math.sin(Math.toRadians(bearing));
            currY =+ distance*Math.cos(Math.toRadians(bearing));
        }
        if (initSet) {
            currX = 0;
            currY= 0;
            initSet =false;
        }
        updateDisplayedCoords(currX, currY, rotationManager.bearing);
        moveMarker(new Double(currX-prevX).intValue(), new Double(currY-prevY).intValue());
    }
    void updateDisplayedCoords(double x, double y, double be) {
        cX.setText("Coordinate X: "+ x);
        cY.setText("Coordinate Y: "+ y);
        bear.setText("Bearing: " + be);

    }
    void setDistOffset(double offset) {
        if (offsetter) {
            distOffset = offset;
            offsetter = false;
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }
    @Override
    public void onProviderEnabled(String provider) {
    }
    @Override
    public void onProviderDisabled(String provider) {
    }}

