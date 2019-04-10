package com.micronet.bridgetechbusoccupancy;

import android.app.Application;
import android.content.Context;

import com.micronet.bridgetechbusoccupancy.SharedPreferencesSingleton;

public class BusOccupancyApplication extends Application {

    private static BusOccupancyApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferencesSingleton.getInstance().initialize(getApplicationContext());
        instance = this;
    }

    public static BusOccupancyApplication getInstance() {
        return instance;
    }
}
