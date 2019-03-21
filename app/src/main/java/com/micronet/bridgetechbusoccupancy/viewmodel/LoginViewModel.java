package com.micronet.bridgetechbusoccupancy.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.util.SparseArray;

import com.micronet.bridgetechbusoccupancy.repository.Bus;
import com.micronet.bridgetechbusoccupancy.repository.BusDriver;
import com.micronet.bridgetechbusoccupancy.repository.Settings;
import com.micronet.bridgetechbusoccupancy.utils.OutgoingMessage;

import java.sql.Driver;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class LoginViewModel extends ViewModel {

    private ArrayList<Integer> routeAdapter;

    public String getBusNumber() {
        return Bus.getInstance().busNumber.getValue();
    }

    public void onLogin(String opsNumber, int route, int odometerReading) {
        BusDriver.getInstance().opsNumber.setValue(opsNumber);
        Settings.getInstance().currentRoute.setValue(route);
        Bus.getInstance().odometerReading.setValue(odometerReading);
        OutgoingMessage.sendData();
    }

    public List<String> routesList() {
        List<String> routes = new ArrayList<>();
        SparseArray<String> sparseArray = Settings.getInstance().getRoutes();
        routeAdapter = new ArrayList<>();
        for (int i=0; i<sparseArray.size(); i++) {
            routeAdapter.add(sparseArray.keyAt(i));
            routes.add(sparseArray.get(sparseArray.keyAt(i)));
        }
        return routes;
    }

    public int routeForElement(int i) {
        return routeAdapter.get(i);
    }
}
