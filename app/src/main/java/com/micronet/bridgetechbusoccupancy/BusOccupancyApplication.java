package com.micronet.bridgetechbusoccupancy;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import com.micronet.bridgetechbusoccupancy.SharedPreferencesSingleton;
import com.micronet.bridgetechbusoccupancy.repository.Bus;
import com.micronet.bridgetechbusoccupancy.repository.BusDriver;
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
        BusDriver.initialize();
        instance = this;
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
//        filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        DatagramSocketSingletonWrapper.getInstance().setContext(getApplicationContext());
        DatagramSocketSingletonWrapper.getInstance().connectToServer(new Runnable() {
            @Override
            public void run() {
                OutgoingMessage.sendData();
            }
        });
        registerReceiver(new ConnectivityChangedReceiver(), filter);
        startService(new Intent(this, UdpService.class));
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
        boolean isDisconnected = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
        Log.d(TAG, String.format("Disconnected: %s", isDisconnected));
//        if(isDisconnected) {
//            DatagramSocketSingletonWrapper.getInstance().disconnectFromServer();
//            DatagramSocketSingletonWrapper.getInstance().setHasInternet(false);
//        }
//        else {
//            DatagramSocketSingletonWrapper.getInstance().connectToServer();
//            DatagramSocketSingletonWrapper.getInstance().setHasInternet(false);
//        }
    }
}