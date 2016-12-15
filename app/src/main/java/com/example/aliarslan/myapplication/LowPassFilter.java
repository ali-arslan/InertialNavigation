package com.example.aliarslan.myapplication;

public class LowPassFilter {
    private static final float ALPHA = 0.2f;

    private LowPassFilter() {
    }

    public static float[] filter(float[] input, float[] output) {
        if (output == null) {
            return input;
        }
        for (int i = 0; i < input.length; i++) {
            output[i] = output[i] + (ALPHA * (input[i] - output[i]));
        }
        return output;
    }
}