package com.example.aliarslan.myapplication;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by aliarslan on 11/23/16.
 */

public class Util {
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
