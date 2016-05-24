package com.rhodes.video;

import java.util.MissingFormatArgumentException;

public class Logger {
    private static final String TAG = "PAPA";

    public static void log(String... s) {
        if (!BuildConfig.DEBUG) return;//DEBUG

        String log = "";
        for (int i = 0; i < s.length; i++) {
            log += " " + s[i];
        }

        android.util.Log.e(TAG, log);
    }

    public static void i(String msg, Object... args) {
        try {
            if (BuildConfig.DEBUG)
                android.util.Log.i(TAG, String.format(msg, args));
        } catch (MissingFormatArgumentException e) {
            android.util.Log.e(TAG, "papa.Log", e);
            android.util.Log.i(TAG, msg);
        }
    }

    public static void d(String msg, Object... args) {
        try {
            if (BuildConfig.DEBUG)
                android.util.Log.d(TAG, String.format(msg, args));
        } catch (MissingFormatArgumentException e) {
            android.util.Log.e(TAG, "papa.Log", e);
            android.util.Log.d(TAG, msg);
        }
    }

    public static void e(String msg, Object... args) {
        try {
            android.util.Log.e(TAG, String.format(msg, args));
        } catch (MissingFormatArgumentException e) {
            android.util.Log.e(TAG, "papa.Log", e);
            android.util.Log.e(TAG, msg);
        }
    }

    public static void e(String msg, Throwable t) {
        android.util.Log.e(TAG, msg, t);
    }
}
