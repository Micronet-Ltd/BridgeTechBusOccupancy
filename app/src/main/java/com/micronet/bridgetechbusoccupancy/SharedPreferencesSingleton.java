package com.micronet.bridgetechbusoccupancy;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SharedPreferencesSingleton {
    private static SharedPreferencesSingleton instance;
    private SharedPreferences sharedPreferences;
    private Context context;

    private SharedPreferencesSingleton() { }

    public static SharedPreferencesSingleton getInstance() {
        if(instance == null) {
            instance = new SharedPreferencesSingleton();
        }
        return instance;
    }

    public void initialize(Context context) {
        this.context = context;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void updateInt(String key, int value) {
        sharedPreferences.edit().putInt(key, value).apply();
    }

    public void updateBoolean(String key, boolean value) {
        sharedPreferences.edit().putBoolean(key, value).apply();
    }

    public int getInt(String key, int defaultValue) {
        return sharedPreferences.getInt(key, defaultValue);
    }
}
