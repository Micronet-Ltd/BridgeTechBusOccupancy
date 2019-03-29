package com.micronet.bridgetechbusoccupancy.repository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import java.util.Observable;

public class BusDriver extends Observable {
    private static BusDriver ourInstance;
    public MutableLiveData<Integer> breakType;
    public MutableLiveData<String> opsNumber;


    public static BusDriver getInstance() {
        if(ourInstance == null) {
            ourInstance = new BusDriver();
        }
        return ourInstance;
    }

    private BusDriver() {
        opsNumber = new MutableLiveData<>();
        opsNumber.setValue("not entered");
        breakType = new MutableLiveData<>();
        breakType.setValue(0xFF);
    }
}
