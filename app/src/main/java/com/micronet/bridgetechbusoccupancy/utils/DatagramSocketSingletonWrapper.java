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
            receiveSocket = new DatagramSocket(Settings.getInstance().getRxPort());
            receiveSocket.connect(InetAddress.getByName(Settings.getInstance().getServerAddress()), Settings.getInstance().getServerPort());

            transmitSocket = new DatagramSocket(Settings.getInstance().getTxPort());
            transmitSocket.connect(InetAddress.getByName(Settings.getInstance().getServerAddress()), Settings.getInstance().getServerPort());
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
