package com.micronet.bridgetechbusoccupancy.thread;

import com.micronet.bridgetechbusoccupancy.repository.Bus;
import com.micronet.bridgetechbusoccupancy.utils.DatagramSocketSingletonWrapper;
import com.micronet.bridgetechbusoccupancy.utils.Log;
import com.micronet.bridgetechbusoccupancy.utils.OutgoingMessage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class UdpInputRunnable implements Runnable {
    public static final String TAG = "Bridgetech-UDP";
    private static final int PACKET_ACK = 0x06;

    boolean isRunning = true;
    DatagramSocket socket;
    byte[] data;


    public UdpInputRunnable(DatagramSocket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        data = new byte[10];
        while (true) {
            while(!DatagramSocketSingletonWrapper.getInstance().hasInternet());
            DatagramSocketSingletonWrapper.getInstance().connectToServer();
            while(DatagramSocketSingletonWrapper.getInstance().hasInternet()) {
                try {
                    Log.d(TAG, "Waiting for a packet");
                    DatagramPacket packet = new DatagramPacket(data, data.length);
                    DatagramSocketSingletonWrapper.getInstance().getSocket().receive(packet);
                    Log.d(TAG, "Received a packet of some sort");
                    IncomingMessage message = new IncomingMessage(packet.getData(), packet.getLength(), packet.getOffset());
                    Thread t = new Thread(new MessageHandler(message));
                    t.start();
                } catch (SocketException e) {
                    Log.e(TAG, "SocketException: " + e.getMessage());
                    e.printStackTrace();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    continue;
                } catch (UnknownHostException e) {
                    Log.e(TAG, "UnknownHostException: " + e.getMessage());
                    e.printStackTrace();
                } catch (IOException e) {
                    Log.e(TAG, "IOException: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "No internet");
        }
    }

    public void stop() {
        isRunning = false;
    }
}

class MessageHandler implements Runnable {
    private IncomingMessage message;
    private static final String TAG = "Bridgetech-UDP";

    public MessageHandler(IncomingMessage message) {
        this.message = message;
    }

    @Override
    public void run() {
        if(message.getMessageType() == IncomingMessage.MessageType.ACK) {
            short packetId = message.getPacketId();
            Log.d(TAG, String.format("Received ACK for packet with id %s", packetId));
            OutgoingMessage.ackPacket(packetId);
        }
        else if(message.getMessageType() == IncomingMessage.MessageType.INCOMING_DATA) {
            Bus.getInstance().busNumber.postValue(message.getBusNumber());
            Bus.getInstance().currentOccupancy.postValue(message.getBusOccupancy());
            Log.d(TAG, String.format("Received packet BUS NUMBER %s and CURRENT OCCUPANCY %d", Bus.getInstance().busNumber.getValue(), Bus.getInstance().currentOccupancy.getValue()));
            new OutgoingAckPacket(message.getPacketId()).send();
        }
        else if(message.getMessageType() == IncomingMessage.MessageType.MALFORMED_MESSAGE) {
            Log.d(TAG, "Malformed message received, ignoring.");
        }
    }
}

class IncomingMessage {
    private static final byte ACK = 0x06;
    private static final byte DATA = 0x10;

    private static final int ACK_PACKET_LENGTH = 3;
    private static final int DATA_PACKET_LENGTH = 5;

    public enum MessageType {
        ACK,
        INCOMING_DATA,
        MALFORMED_MESSAGE
    }

    byte[] data;
    int length;

    public IncomingMessage(byte[] receivedBytes, int length, int offset) {
        data = new byte[length];
        System.arraycopy(receivedBytes, offset, data, 0, length);
        this.length = length;
    }

    public MessageType getMessageType() {
        if(data[0] == ACK && length == ACK_PACKET_LENGTH) {
            return MessageType.ACK;
        }
        else if (data[0] == DATA && length == DATA_PACKET_LENGTH) {
            return MessageType.INCOMING_DATA;
        }
        else {
            return MessageType.MALFORMED_MESSAGE;
        }
    }

    public short getPacketId() {
        ByteBuffer bb = ByteBuffer.allocate(2);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.put(data[1]);
        bb.put(data[2]);
        return bb.getShort(0);
    }

    public int getBusNumber() {
        return (int) data[3];
    }

    public int getBusOccupancy() {
        return (int) data[4];
    }

    public byte[] getData() {
        return data;
    }
}

class OutgoingAckPacket {
    private static final byte ACK = 0x06;

    byte[] data;

    public OutgoingAckPacket(short packetId) {
        data = new byte[3];
        data[0] = ACK;
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.putShort(packetId);
        byte[] idArray = buffer.array();
        data[1] = idArray[0];
        data[2] = idArray[1];
    }

    public void send() {
        try {
            DatagramSocket socket = DatagramSocketSingletonWrapper.getInstance().getSocket();
            socket.send(new DatagramPacket(data, data.length));
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}