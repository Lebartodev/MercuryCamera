package com.lebartodev.mercurycamera.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.lebartodev.mercurycamera.CameraAplication;

/**
 * Created by Александр on 12.12.2016.
 */

public class BaseSharedPrefer {
    protected final static PrefHolder get() {
        return PrefHolder.getInstance(CameraAplication.getInstance());
    }

    protected static class PrefHolder {

        private final SharedPreferences preferences;
        private static volatile PrefHolder insatnce = null;

        private PrefHolder(Context context) {
            preferences = PreferenceManager
                    .getDefaultSharedPreferences(context);
        }

        public synchronized static PrefHolder getInstance(Context context) {
            if (insatnce == null) {
                insatnce = new PrefHolder(context);
            }
            return insatnce;
        }

        public void put(String key, String value) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(key, value);
            editor.commit();
        }

        public String get(String key, String defValue) {
            return preferences.getString(key, defValue);
        }

        public void put(String key, int value) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(key, value);
            editor.commit();
        }

        public int get(String key, int defValue) {
            return preferences.getInt(key, defValue);
        }

        public void put(String key, long value) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putLong(key, value);
            editor.commit();
        }

        public long get(String key, long defValue) {
            return preferences.getLong(key, defValue);
        }

        public void put(String key, boolean value) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(key, value);
            editor.commit();
        }

        public boolean get(String key, boolean defValue) {
            return preferences.getBoolean(key, defValue);
        }
    }
}