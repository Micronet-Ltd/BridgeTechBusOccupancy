package com.micronet.bridgetechbusoccupancy.utils;

import com.micronet.bridgetechbusoccupancy.repository.Settings;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class DatagramSocketSingletonWrapper {
    private static final DatagramSocketSingletonWrapper ourInstance = new DatagramSocketSingletonWrapper();

    private DatagramSocket receiveSocket;
    private DatagramSocket transmitSocket;

    public static DatagramSocketSingletonWrapper getInstance() {
        return ourInstance;
    }

    private DatagramSocketSingletonWrapper() {
        try {
            receiveSocket = new DatagramSocket(8080);
            receiveSocket.connect(InetAddress.getByName(Settings.getInstance().getServerAddress()), Settings.getInstance().getPort());

            transmitSocket = new DatagramSocket(8081);
            transmitSocket.connect(InetAddress.getByName(Settings.getInstance().getServerAddress()), Settings.getInstance().getPort());
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public DatagramSocket getReceiveSocket() {
        return receiveSocket;
    }

    public DatagramSocket getTransmitSocket() {
        return transmitSocket;
    }
}
