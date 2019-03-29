package com.micronet.bridgetechbusoccupancy.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.util.SparseArray;

import com.micronet.bridgetechbusoccupancy.repository.Bus;
import com.micronet.bridgetechbusoccupancy.repository.Settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class BusOccupancyViewModel extends ViewModel {
    private MutableLiveData<Integer> occupancyLiveData;
    private List<Integer> breakAdapter;

    public BusOccupancyViewModel() {
        super();
        Bus.getInstance().addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {

            }
        });
    }


}