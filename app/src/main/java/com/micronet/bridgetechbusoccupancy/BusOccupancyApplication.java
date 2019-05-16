package com.micronet.bridgetechbusoccupancy;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.micronet.bridgetechbusoccupancy.SharedPreferencesSingleton;
import com.micronet.bridgetechbusoccupancy.repository.Bus;
import com.micronet.bridgetechbusoccupancy.utils.DatagramSocketSingletonWrapper;
import com.micronet.bridgetechbusoccupancy.utils.Log;
import com.micronet.bridgetechbusoccupancy.utils.OutgoingMessage;

import java.net.SocketException;
import java.net.UnknownHostException;

public class BusOccupancyApplication extends Application {

    private static BusOccupancyApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferencesSingleton.getInstance().initialize(getApplicationContext());
        Bus.initialize();
        instance = this;
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        DatagramSocketSingletonWrapper.getInstance().connectToServer(new Runnable() {
            @Override
            public void run() {
                OutgoingMessage.sendData();
            }
        });
        registerReceiver(new ConnectivityChangedReceiver(), filter);
    }

    public static BusOccupancyApplication getInstance() {
        return instance;
    }
}

class ConnectivityChangedReceiver extends BroadcastReceiver {
    private static final String TAG = "ConnectivityChangedReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, String.format("Received intent with action %s", intent.getAction()));
        DatagramSocketSingletonWrapper.getInstance().connectToServer();
    }
}