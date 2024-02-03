package com.insatc.sir_scanner.datatools;

import org.osmdroid.util.GeoPoint;

public class CustomPoint {
    private String id;
    private GeoPoint geoPoint;
    private String time;
    private double precision;

    public CustomPoint(String id, GeoPoint geoPoint, String time, double precision) {
        this.id = id;
        this.geoPoint = geoPoint;
        this.time = time;
        this.precision = precision;
    }

    public String getId() {
        return id;
    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public String getTime(){
        return time;
    }

    public double getPrecision() {
        return precision;
    }
}