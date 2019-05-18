package com.micronet.bridgetechbusoccupancy.utils;

import android.content.Context;
import android.net.ConnectivityManager;

import com.micronet.bridgetechbusoccupancy.repository.Settings;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class DatagramSocketSingletonWrapper {
    private static final String TAG = "Bridgetech-UDP";
    private static final DatagramSocketSingletonWrapper ourInstance = new DatagramSocketSingletonWrapper();

    private DatagramSocket socket;
    private Context context;

    public static DatagramSocketSingletonWrapper getInstance() {
        return ourInstance;
    }

    private DatagramSocketSingletonWrapper() {
        try {
            socket = new DatagramSocket(Settings.getInstance().getRxPort());
            socket.setReuseAddress(true);
            connectToServer();
        } catch (SocketException e) {
            Log.e(TAG, String.format("Socket exception message: %s", e.getMessage()));
            e.printStackTrace();
        }
    }

    public void connectToServer() {
        connectToServer(null);
    }

    public void connectToServer(final Runnable runAfterConnected) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, String.format("Socket connected: %s", socket.isConnected()));
                try {
                    if(socket.isClosed()) {
                        Log.d(TAG, "Reopening socket");
                        socket = new DatagramSocket(Settings.getInstance().getRxPort());
                    }
                    else {
                        Log.d(TAG, "Socket is not closed");
                    }
                    socket.connect(InetAddress.getByName(Settings.getInstance().getServerAddress()), Settings.getInstance().getServerPort());
                    if(runAfterConnected != null) {
                        runAfterConnected.run();
                    }
                } catch (UnknownHostException e) {
                    Log.e(TAG, String.format("Unknown host exception message: %s", e.getMessage()));
                    e.printStackTrace();
                } catch (SocketException e) {
                    Log.e(TAG, String.format("Socket exception: %s", e.getMessage()));
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }

    public void disconnectFromServer() {
        if(socket != null) {
            socket.disconnect();
            socket.close();
        }
    }

    public DatagramSocket getSocket() {
        return socket;
    }

    public boolean hasInternet() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean hasInternet = cm.getActiveNetworkInfo() != null;
        if(!hasInternet) {
            disconnectFromServer();
        }
        else {
            connectToServer();
        }
        return hasInternet;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
