package com.micronet.bridgetechbusoccupancy.viewmodel;

import android.arch.lifecycle.ViewModel;

import com.micronet.bridgetechbusoccupancy.repository.Bus;

public class OdometerReadingViewModel extends ViewModel {
    public void setOdometerReading(int odometerReading) {
        Bus.getInstance().odometerReading.setValue(odometerReading);
    }

    public int getOdometerReading() {
        try {
            return Bus.getInstance().odometerReading.getValue();
        }
        catch (NullPointerException e) {
            return 0;
        }
    }
}
