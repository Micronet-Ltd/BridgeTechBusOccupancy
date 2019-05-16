package com.micronet.bridgetechbusoccupancy.utils;

import android.os.Handler;
import com.micronet.bridgetechbusoccupancy.repository.Bus;
import com.micronet.bridgetechbusoccupancy.repository.BusDriver;
import com.micronet.bridgetechbusoccupancy.repository.Settings;
import com.micronet.bridgetechbusoccupancy.thread.UdpInputRunnable;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class OutgoingMessage implements TimestampProvider {
    private static String serialNumber;
    private static short packetId = 0;
    private static ConcurrentHashMap<Short, byte[]> unackedPackets;
    public static final String TAG = UdpInputRunnable.TAG;
    private static boolean threadIsRunning = false;
    private static Thread unackedPacketThread = new Thread(new Runnable() {
        @Override
        public void run() {
            while (true) {
                for (short packetId : unackedPackets.keySet()) {
                    resendPacket(packetId);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    });

    private static Handler keepAliveHandler;
    private static int keepAliveInterval = 60000;

    static {
        unackedPackets = new ConcurrentHashMap<>();
        if(!threadIsRunning) {
            threadIsRunning = true;
            unackedPacketThread.start();
        }
    }

    public static void resendPacket(short id) {
        if(unackedPackets.containsKey(id)) {
            Log.d(TAG,  "Resending packet with id " + id);
            sendBytes(unackedPackets.get(id));
        }
    }

    public static void ackPacket(short packetId) {
        if(unackedPackets.containsKey(packetId)) {
            unackedPackets.remove(packetId);
        }
    }

    private static String serialNumber() {
        if(serialNumber == null) {
            try {
                Class<?> c = Class.forName("android.os.SystemProperties");
                Method get = c.getMethod("get", String.class);
                serialNumber = (String) get.invoke(c, "ro.serialno");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return serialNumber;
    }

    private static byte[] byteRepresentation() {
        byte[] data = new byte[25];
        data[0] = 0x10;
        for(int i = 0; i <= 3; i++) {
            int v = Integer.parseInt(serialNumber().substring(i * 2, i * 2 + 2), 16);
            Log.d(TAG, String.format("v[%d]: %d", i, v));
            data[i + 1] = (byte)v;
        }
        data[5] = (byte) Bus.getInstance().currentOccupancy.getValue().intValue();
        data[6] = (byte) BusDriver.getInstance().breakType.getValue().intValue();
        int odometerReading = Bus.getInstance().odometerReading.getValue();
        ByteBuffer odometerBuffer = ByteBuffer.allocate(4);
        odometerBuffer.putInt(odometerReading);
        byte[] odometerArray = odometerBuffer.array();
        data[7] = odometerArray[0];
        data[8] = odometerArray[1];
        data[9] = odometerArray[2];
        data[10] = odometerArray[3];
        data[11] = (byte) (0xFF & Integer.valueOf(Bus.getInstance().gatherBusNumber()).intValue());

        try {
            ByteBuffer opsNumberBuffer = ByteBuffer.allocate(2);
            opsNumberBuffer.putShort((short)BusDriver.getInstance().opsNumber.getValue().intValue());
            byte[] opsNumberBytes = opsNumberBuffer.array();
            data[12] = opsNumberBytes[0];
            data[13] = opsNumberBytes[1];
        }
        catch (Exception e) {
            data[12] = (byte)0xFF;
            data[13] = (byte)0xFF;
        }
        try {
            data[14] = (byte) (0xFF & Objects.requireNonNull(Settings.getInstance().currentRoute.getValue()).intValue());
        }
        catch (Exception e) {
            data[14] = (byte)0xFF;
        }
        ByteBuffer timestampBuffer = ByteBuffer.allocate(8);
        timestampBuffer.putLong(System.currentTimeMillis());
        byte[] timestampArray = timestampBuffer.array();
        data[15] = timestampArray[0];
        data[16] = timestampArray[1];
        data[17] = timestampArray[2];
        data[18] = timestampArray[3];
        data[19] = timestampArray[4];
        data[20] = timestampArray[5];
        data[21] = timestampArray[6];
        data[22] = timestampArray[7];
        ByteBuffer packetIdBuffer = ByteBuffer.allocate(2);
        packetIdBuffer.putShort(packetId);
        byte[] packetIdArray = packetIdBuffer.array();
        data[23] = packetIdArray[0];
        data[24] = packetIdArray[1];
        unackedPackets.put(packetId, data);
        Log.d(TAG, "Sending packet with ID " + packetId);
        packetId++;
        return data;
    }

    public static void sendData() {
        sendBytes(byteRepresentation());
    }

    private static void sendBytes(final byte[] bytes) {
        final DatagramSocket socket = DatagramSocketSingletonWrapper.getInstance().getSocket();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket.send(new DatagramPacket(bytes, bytes.length));
                } catch (IOException e) {
                    Log.e(TAG, String.format("Could not send packet.  Error: %s", e.getMessage()));
                    e.printStackTrace();
                    DatagramSocketSingletonWrapper.getInstance().connectToServer();
                }
            }
        });
        t.start();
    }

    @Override
    public long getTimestamp() {
        return System.currentTimeMillis();
    }

    public static void startKeepAliveMechanism() {
        keepAliveHandler = new Handler();

        // Every 60 seconds ping the server to keep address up to date.
        keepAliveHandler.postDelayed(keepAliveUpdate, keepAliveInterval);
//        Log.d("Bridgetech-KeepAlive", "Started keep alive handler.");
    }

    public static void stopKeepAliveMechanism() {
        if(keepAliveHandler != null){
            keepAliveHandler.removeCallbacks(keepAliveUpdate);
//            Log.d("Bridgetech-KeepAlive", "Stopped keep alive handler.");
        }
    }

    private static Runnable keepAliveUpdate = new Runnable() {
        @Override
        public void run() {
            sendData();
//            Log.d("Bridgetech-KeepAlive", "Sent keep alive message.");
            keepAliveHandler.postDelayed(this, keepAliveInterval);
        }
    };
}
