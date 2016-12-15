package com.example.aliarslan.myapplication;

import android.graphics.Matrix;

/**
 * Created by aliarslan on 12/15/16.
 */

public class RotationManager {
    float bearing;
    float anglerot;

    public void setBearing(float b) {
        bearing = b;
        anglerot = 360 - bearing;
    }


}
