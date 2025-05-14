package com.github.moosethebrown.shipcontrol.data;

public class Waypoint {
    private double latitude;
    private double longitude;

    public Waypoint(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
}
