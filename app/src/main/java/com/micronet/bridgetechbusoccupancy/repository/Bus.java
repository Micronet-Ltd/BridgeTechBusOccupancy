package com.micronet.bridgetechbusoccupancy.repository;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import com.micronet.bridgetechbusoccupancy.SharedPreferencesSingleton;
import com.micronet.bridgetechbusoccupancy.interfaces.BusInfoProvider;
import com.micronet.bridgetechbusoccupancy.utils.Log;

import java.util.Observable;

public class Bus extends Observable implements BusInfoProvider  {
    private static final String TAG = "Bus";
    private static Bus ourInstance;

    private BusInfoProvider provider;
    private int mileage = 0;
    public MutableLiveData<Integer> busNumber;
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
        currentOccupancy.postValue(SharedPreferencesSingleton.getInstance().getInt("occupancy", 0));
        odometerReading = new MutableLiveData<>();
        odometerReading.postValue(SharedPreferencesSingleton.getInstance().getInt("odometer", 0));
        busNumber.setValue(gatherBusNumber());
        Log.d(TAG, "Bus number: " + busNumber.getValue());
        currentOccupancy.observeForever(new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer occupancy) {
                SharedPreferencesSingleton.getInstance().updateInt("occupancy", occupancy);
            }
        });
        busNumber.observeForever(new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer busNumber) {
                SharedPreferencesSingleton.getInstance().updateInt("busNumber", busNumber);
            }
        });
        odometerReading.observeForever(new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer odometerReading) {
                SharedPreferencesSingleton.getInstance().updateInt("odometer", odometerReading);
            }
        });
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
    public int gatherBusNumber() {
//        return busNumber.getValue();
        return SharedPreferencesSingleton.getInstance().getInt("busNumber", 1);
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
