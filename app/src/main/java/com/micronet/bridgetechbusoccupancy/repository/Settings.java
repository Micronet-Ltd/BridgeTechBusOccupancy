package com.micronet.bridgetechbusoccupancy.repository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.Environment;
import android.util.Log;
import android.util.SparseArray;

import com.micronet.bridgetechbusoccupancy.utils.Distance;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class Settings {
    public static final String SETTINGS_FILE_PATH = Environment.getDataDirectory().getPath() + "/Bridgetech/configuration.xml";
    private static final Settings ourInstance = new Settings();
    private static final String TAG = "bridgetech-settings";

    private String serverAddress;
    private int port;
    private Distance odometerErrorThreshold;
    private SparseArray<String> breakTypes;
    private boolean noLogout = false;
    private SparseArray<String> routes;
    public MutableLiveData<Integer> currentRoute;

    public static Settings getInstance() {
        return ourInstance;
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

    public int getPort() {
        return port;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public boolean getNoLogout() {
        return noLogout;
    }

    private Settings() {
        currentRoute = new MutableLiveData<>();
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
        for(int i = 0; i < nodeList.getLength(); i++) {
            Element currentElement = (Element)nodeList.item(i);
            int id = Integer.parseInt(currentElement.getAttribute("id"));
            String name = currentElement.getAttribute("name");
            routes.append(id, name);
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
            NodeList nodeList = document.getElementsByTagName("entry");
            for(int i = 0; i < nodeList.getLength(); i++) {
                Element currentElement = (Element)nodeList.item(i);
                int id = Integer.parseInt(currentElement.getAttribute("id"));
                String name = currentElement.getAttribute("value");
                breakTypes.append(id, name);
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

    private void setServerFromDocument(Document document) {
        try {
            Element serverNode = (Element) document.getElementsByTagName("server").item(0);
            serverAddress = serverNode.getAttribute("address");
            port = Integer.parseInt(serverNode.getAttribute("port"));
        }
        catch (NullPointerException e) {
            serverAddress="127.0.0.1";
            port = 8080;
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
