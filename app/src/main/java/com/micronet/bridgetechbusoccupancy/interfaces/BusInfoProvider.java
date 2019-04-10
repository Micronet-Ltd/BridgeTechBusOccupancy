package com.micronet.bridgetechbusoccupancy.interfaces;

public interface BusInfoProvider {
    public int gatherBusNumber();
    public int gatherMileage();
    public int gatherCurrentOccupancy();
}