package com.micronet.bridgetechbusoccupancy.repository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.Nullable;

import com.micronet.bridgetechbusoccupancy.SharedPreferencesSingleton;

import java.util.Observable;

public class BusDriver extends Observable {
    private static BusDriver ourInstance;
    public MutableLiveData<Integer> breakType;
    public MutableLiveData<Integer> opsNumber;

    public static void initialize() {
        if(ourInstance == null) {
            ourInstance = new BusDriver();
        }
    }


    public static BusDriver getInstance() {
        if(ourInstance == null) {
            ourInstance = new BusDriver();
        }
        return ourInstance;
    }

    private BusDriver() {
        opsNumber = new MutableLiveData<>();
        int ops = SharedPreferencesSingleton.getInstance().getInt("opsNumber", -1);
        opsNumber.setValue(ops);
        opsNumber.observeForever(new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer opsNum) {
                SharedPreferencesSingleton.getInstance().updateInt("opsNumber", opsNum);
            }
        });
        breakType = new MutableLiveData<>();
        int currentBreakType = SharedPreferencesSingleton.getInstance().getInt("breakType", 99);
        breakType.observeForever(new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer breakType) {
                SharedPreferencesSingleton.getInstance().updateInt("breakType", breakType);
            }
        });
        breakType.setValue(currentBreakType);

    }
}
