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
        try {
            while (true) {
                Log.d(TAG, "Waiting for a packet");
                DatagramPacket packet = new DatagramPacket(data, data.length);
                socket.receive(packet);
                Log.d(TAG, "Received a packet of some sort");
                IncomingMessage message = new IncomingMessage(packet.getData());
                if(message.getMessageType() == IncomingMessage.MessageType.ACK) {
                    short packetId = message.getPacketId();
                    Log.d(TAG, String.format("Received ACK for packet with id %s", packetId));
                    OutgoingMessage.ackPacket(packetId);
                }
                else if(message.getMessageType() == IncomingMessage.MessageType.INCOMING_DATA) {
                    Bus.getInstance().busNumber.postValue(String.valueOf(message.getBusNumber()));
                    Bus.getInstance().currentOccupancy.postValue(message.getBusOccupancy());
                    Log.d(TAG, String.format("Received packet BUS NUMBER %s and CURRENT OCCUPANCY %d", Bus.getInstance().busNumber.getValue(), Bus.getInstance().currentOccupancy.getValue()));
                    new OutgoingAckPacket(message.getPacketId()).send();
                }
                else if(message.getMessageType() == IncomingMessage.MessageType.MALFORMED_MESSAGE) {
                    Log.d(TAG, "Malformed message received, ignoring.");
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        isRunning = false;
    }
}

class IncomingMessage {
    private static final byte ACK = 0x06;
    private static final byte DATA = 0x10;

    public enum MessageType {
        ACK,
        INCOMING_DATA,
        MALFORMED_MESSAGE
    }

    byte[] data;

    public IncomingMessage(byte[] receivedBytes) {
        data = receivedBytes;
    }

    public MessageType getMessageType() {
        if(data[0] == ACK) {
            return MessageType.ACK;
        }
        else if (data[0] == DATA) {
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
            DatagramSocket socket = DatagramSocketSingletonWrapper.getInstance().getTransmitSocket();
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