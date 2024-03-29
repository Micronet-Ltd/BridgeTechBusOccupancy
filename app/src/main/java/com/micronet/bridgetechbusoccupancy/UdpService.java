package com.micronet.bridgetechbusoccupancy;

import static com.micronet.bridgetechbusoccupancy.utils.OutgoingMessage.startKeepAliveMechanism;
import static com.micronet.bridgetechbusoccupancy.utils.OutgoingMessage.stopKeepAliveMechanism;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.micronet.bridgetechbusoccupancy.thread.UdpInputRunnable;
import com.micronet.bridgetechbusoccupancy.utils.DatagramSocketSingletonWrapper;
import com.micronet.bridgetechbusoccupancy.utils.Log;

public class UdpService extends Service {

    public UdpService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Bridgetech-UDP", "In UdpService starting the thread");
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                    Thread t = new Thread(new UdpInputRunnable(DatagramSocketSingletonWrapper.getInstance().getSocket()));
                    t.start();
            }
        });
        thread.start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Start keep alive mechanism
        startKeepAliveMechanism();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        stopKeepAliveMechanism();
    }
}
