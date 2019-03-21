package com.micronet.bridgetechbusoccupancy.interfaces;

public interface BusInfoProvider {
    public String gatherBusNumber();
    public int gatherMileage();
    public int gatherCurrentOccupancy();
}