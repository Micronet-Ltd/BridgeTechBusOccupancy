package com.micronet.bridgetechbusoccupancy.repository;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;
import android.webkit.URLUtil;
import android.widget.Toast;

import com.micronet.bridgetechbusoccupancy.BusOccupancyApplication;
import com.micronet.bridgetechbusoccupancy.SharedPreferencesSingleton;
import com.micronet.bridgetechbusoccupancy.utils.Distance;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class Settings {
    public static final String SETTINGS_FILE_PATH = "/sdcard/BridgetechApp/configuration.xml";
    private static final Settings ourInstance = new Settings();
    private static final String TAG = "bridgetech-settings";

    private String serverAddress;
    private int serverPort;
    private int rxPort;
    private Distance odometerErrorThreshold;
    private SparseArray<String> breakTypes;
    private boolean noLogout = false;
    private SparseArray<String> routes;
    public MutableLiveData<Integer> currentRoute;
    private Context applicationContext;

    public static Settings getInstance() {
        return ourInstance;
    }

    public int getRxPort() {
        return rxPort;
    }

    public SparseArray<String> getBreakTypes() {
        if(breakTypes == null || breakTypes.size() == 0) {
            breakTypes = new SparseArray<>();
            breakTypes.append(0, "Lunch");
            breakTypes.append(1, "Coffee");
            breakTypes.append(2, "Relief");
        }
        return breakTypes;
    }

    public int getServerPort() {
        return serverPort;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public boolean getNoLogout() {
        return noLogout;
    }

    private Settings() {
        applicationContext = BusOccupancyApplication.getInstance().getApplicationContext();
        currentRoute = new MutableLiveData<>();
        currentRoute.postValue(SharedPreferencesSingleton.getInstance().getInt("currentRoute", -1));
        currentRoute.observeForever(new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer route) {
                SharedPreferencesSingleton.getInstance().updateInt("currentRoute", route);
            }
        });
        Document document = xmlDocument();
        if(document != null) {
            setServerFromDocument(document);
            setOdometerErrorThresholdFromDocument(document);
            setBreakTypesFromDocument(document);
            setNoLogoutFromDocument(document);
            setRoutesFromDocument(document);
        }
    }

    public SparseArray<String> getRoutes() {
        if(routes == null) {
            routes = new SparseArray<>();
        }
        return routes;
    }

    private void setRoutesFromDocument(Document document) {
        routes = new SparseArray<>();
        NodeList nodeList = document.getElementsByTagName("route");
        ArrayList<Integer> duplicateIds = new ArrayList<>();
        for(int i = 0; i < nodeList.getLength(); i++) {
            try {
                Element currentElement = (Element) nodeList.item(i);
                int id = Integer.parseInt(currentElement.getAttribute("id"));
                String name = currentElement.getAttribute("name");
                if (routes.get(id) != null) {
                    duplicateIds.add(id);
                }
                routes.append(id, name);
            }
            catch (NumberFormatException e) {
                Toast.makeText(applicationContext, "Route IDs must be integers.", Toast.LENGTH_LONG).show();
                Log.e(TAG, "Invalid ID. Ignoring.");
            }
        }
        if(!duplicateIds.isEmpty()) {
            StringBuilder duplicateList = new StringBuilder();
            for (Integer id : duplicateIds) {
                duplicateList.append(id).append(" ");
            }
            Toast.makeText(applicationContext, String.format("Duplicate route IDs: %sIgnoring some.", duplicateList.toString()), Toast.LENGTH_LONG).show();
        }
    }

    private void setNoLogoutFromDocument(Document document) {
        Element noLogoutElement = (Element) (document.getElementsByTagName("no-logout").item(0));
        if(noLogoutElement!= null && noLogoutElement.hasAttribute("value")) {
            try {
                noLogout = Boolean.valueOf(noLogoutElement.getAttribute("value"));
            }
            catch (Exception e) {
                Log.e(TAG, "No valid no-logout value found. Defaulting to false.");
            }
        }
    }

    private void setBreakTypesFromDocument(Document document) {
        breakTypes = new SparseArray<>();
        try {
            List<Integer> duplicateIds = new ArrayList<>();
            NodeList nodeList = document.getElementsByTagName("entry");
            for(int i = 0; i < nodeList.getLength(); i++) {
                Element currentElement = (Element)nodeList.item(i);
                int id = Integer.parseInt(currentElement.getAttribute("id"));
                String name = currentElement.getAttribute("value");
                if(breakTypes.get(id) != null) {
                    duplicateIds.add(id);
                }
                breakTypes.append(id, name);
            }
            if(!duplicateIds.isEmpty()) {
                StringBuilder duplicateList = new StringBuilder();
                for (Integer id : duplicateIds) {
                    duplicateList.append(id).append(" ");
                }
                Toast.makeText(applicationContext, String.format("Duplicate break type IDs: %sIgnoring some.", duplicateList.toString()), Toast.LENGTH_LONG).show();
            }
        }
        catch (Exception e) {
            breakTypes.append(0, "Lunch");
            breakTypes.append(1, "Coffee");
            breakTypes.append(2, "Relief");
        }
    }

    private void setOdometerErrorThresholdFromDocument(Document document) {
        try {
            Element odometerErrorThresholdNode = (Element) document.getElementsByTagName("odometer-error-threshold").item(0);
            float distanceValue = Float.valueOf(odometerErrorThresholdNode.getAttribute("value"));
            String unitString = odometerErrorThresholdNode.getAttribute("unit");
            Distance.Unit unit;
            if("kilometers".equals(unitString)) {
                unit = Distance.Unit.KILOMETERS;
            }
            else if("miles".equals(unitString)) {
                unit = Distance.Unit.MILES;
            }
            else {
                Log.e(TAG, "No valid units found, defaulting to miles");
                unit = Distance.Unit.MILES;
            }
            odometerErrorThreshold = new Distance(distanceValue, unit);
        }
        catch (Exception e) {
            odometerErrorThreshold = new Distance(0f, Distance.Unit.MILES);
        }
    }

    private boolean isValidIpv4Address(String address) {
        String[] tokens = address.split("\\.");
        if(tokens.length != 4) return false;
        for (String token : tokens) {
            try {
                int i = Integer.parseInt(token);
                if(i < 0 || i > 255) return false;
            }
            catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    private void setServerFromDocument(Document document) {
        Element serverNode;
        try {
            serverNode = (Element) document.getElementsByTagName("server").item(0);
        }
        catch (NullPointerException e) {
            serverAddress = "127.0.0.1";
            serverPort = 8080;
            rxPort = 8080;
            return;
        }

        try {
            serverAddress = serverNode.getAttribute("address");
            if(!(URLUtil.isValidUrl(serverAddress) || isValidIpv4Address(serverAddress))) {
                Toast.makeText(applicationContext, "Server address invalid. Defaulting to localhost.", Toast.LENGTH_LONG).show();
                serverAddress = "127.0.0.1";
            }
        }
        catch (NullPointerException e) {
            Toast.makeText(applicationContext, "No server address. Defaulting to localhost.", Toast.LENGTH_LONG).show();
            serverAddress = "127.0.0.1";
        }

        try {
            serverPort = Integer.parseInt(serverNode.getAttribute("port"));
            if(serverPort < 0 || serverPort > 65535) {
                throw new IllegalArgumentException("Server port is not in range");
            }
        }
        catch (NullPointerException e) {
            serverPort = 8080;
        }
        catch (NumberFormatException e) {
            serverPort = 8080;
            Toast.makeText(applicationContext,"Server port number must be an integer between 1 and 65535.  Defaulting to 8080.", Toast.LENGTH_LONG).show();
        }
        catch (IllegalArgumentException e) {
            serverPort = 8080;
            Toast.makeText(applicationContext,"Server port number must be an integer between 1 and 65535.  Defaulting to 8080.", Toast.LENGTH_LONG).show();
        }

        try {
            rxPort = Integer.parseInt(serverNode.getAttribute("rx-port"));
        }
        catch (Exception e) {
            rxPort = 8080;
            Log.e(TAG, String.format("RX port does not exist, defaulting to %d", rxPort));
            Log.e(TAG, "RX port exception: " + e.getMessage());
        }
    }

    private Document xmlDocument() {
        try {
            File file = new File(SETTINGS_FILE_PATH);
            if(!file.exists()) {
                return null;
            }
            InputStream inputStream = new FileInputStream(file);
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = builderFactory.newDocumentBuilder();
            Document document = docBuilder.parse(inputStream);
            document.getDocumentElement().normalize();
            return document;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
        return null;
    }
}
