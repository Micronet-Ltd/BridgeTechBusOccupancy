package com.micronet.bridgetechbusoccupancy.viewmodel;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.util.SparseArray;

import com.micronet.bridgetechbusoccupancy.repository.Bus;
import com.micronet.bridgetechbusoccupancy.repository.BusDriver;
import com.micronet.bridgetechbusoccupancy.repository.Settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

public class MainViewModel extends ViewModel {
    private List<Integer> breakAdapter;

    public List<String> breakTypes() {
        List<String> routes = new ArrayList<>();
        SparseArray<String> sparseArray = Settings.getInstance().getBreakTypes();
        breakAdapter = new ArrayList<>();
        for (int i=0; i<sparseArray.size(); i++) {
            breakAdapter.add(sparseArray.keyAt(i));
            routes.add(sparseArray.get(sparseArray.keyAt(i)));
        }
        return routes;
    }

    public boolean canLogOut() {
        return !Settings.getInstance().getNoLogout();
    }

    public void setBreakType(int type) {
        BusDriver.getInstance().breakType.setValue(breakAdapter.get(type));
    }

    public void clockIn() {
        BusDriver.getInstance().breakType.setValue(99);
    }

    public void observeOccupancy(LifecycleOwner owner, Observer<Integer> observer) {
        Bus.getInstance().currentOccupancy.observe(owner, observer);
    }
}
