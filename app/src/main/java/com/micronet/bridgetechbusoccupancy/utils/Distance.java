package com.micronet.bridgetechbusoccupancy.utils;

public class Distance {
    public enum Unit {
        MILES,
        KILOMETERS
    };
    private float value;
    private Unit unit;

    public Distance(float value, Unit unit) {
        this.value = value;
        this.unit = unit;
    }
}
