package com.micronet.bridgetechbusoccupancy.utils;

import com.micronet.bridgetechbusoccupancy.repository.Settings;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class DatagramSocketSingletonWrapper {
    private static final DatagramSocketSingletonWrapper ourInstance = new DatagramSocketSingletonWrapper();

    private DatagramSocket socket;

    public static DatagramSocketSingletonWrapper getInstance() {
        return ourInstance;
    }

    private DatagramSocketSingletonWrapper() {
        try {
            socket = new DatagramSocket(Settings.getInstance().getRxPort());
            socket.connect(InetAddress.getByName(Settings.getInstance().getServerAddress()), Settings.getInstance().getServerPort());
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public DatagramSocket getSocket() {
        return socket;
    }
}
