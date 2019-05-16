package com.micronet.bridgetechbusoccupancy.utils;

import com.micronet.bridgetechbusoccupancy.repository.Settings;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class DatagramSocketSingletonWrapper {
    private static final String TAG = "DatagramSocketSingletonWrapper";
    private static final DatagramSocketSingletonWrapper ourInstance = new DatagramSocketSingletonWrapper();

    private DatagramSocket socket;

    public static DatagramSocketSingletonWrapper getInstance() {
        return ourInstance;
    }

    private DatagramSocketSingletonWrapper() {
        connectToServer();
    }

    public void connectToServer() {
        connectToServer(null);
    }

    public void connectToServer(final Runnable runAfterConnected) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new DatagramSocket(Settings.getInstance().getRxPort());
                    socket.connect(InetAddress.getByName(Settings.getInstance().getServerAddress()), Settings.getInstance().getServerPort());
                    if(runAfterConnected != null) {
                        runAfterConnected.run();
                    }
                } catch (UnknownHostException e) {
                    Log.e(TAG, String.format("Unknown host exception message: %s", e.getMessage()));
                    e.printStackTrace();
                } catch (SocketException e) {
                    Log.e(TAG, String.format("Socket exception message: %s", e.getMessage()));
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }

    public DatagramSocket getSocket() {
        return socket;
    }
}
