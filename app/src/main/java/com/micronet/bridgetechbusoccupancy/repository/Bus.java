package com.micronet.bridgetechbusoccupancy.repository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.micronet.bridgetechbusoccupancy.interfaces.BusInfoProvider;
import com.micronet.bridgetechbusoccupancy.utils.Log;

import java.util.Observable;

public class Bus extends Observable implements BusInfoProvider  {
    private static final String TAG = "Bus";
    private static Bus ourInstance;

    private BusInfoProvider provider;
    private int mileage = 0;
    public MutableLiveData<String> busNumber;
    public MutableLiveData<Integer> currentOccupancy;
    public MutableLiveData<Integer> odometerReading;

    public static Bus getInstance() {
        if(ourInstance == null) {
            ourInstance = new Bus();
        }
        return ourInstance;
    }

    private Bus() {
        super();
        provider = this;
        if(busNumber== null) {
            busNumber = new MutableLiveData<>();
        }
        currentOccupancy = new MutableLiveData<>();
        currentOccupancy.postValue(0);
        odometerReading = new MutableLiveData<>();
        busNumber.setValue(gatherBusNumber());
        Log.d(TAG, "Bus number: " + busNumber.getValue());
    }

    public int getCurrentOccupancy() {
        return currentOccupancy.getValue();
    }

    public int getMileage() {
        return mileage;
    }

    public void setMileage(int mileage) {
        this.mileage = mileage;
        notifyObservers();
    }

    @Override
    public String gatherBusNumber() {
//        return busNumber.getValue();
        return "112";
    }

    @Override
    public int gatherMileage() {
        return odometerReading.getValue();
    }

    @Override
    public int gatherCurrentOccupancy() {
        return currentOccupancy.getValue();
    }
}
