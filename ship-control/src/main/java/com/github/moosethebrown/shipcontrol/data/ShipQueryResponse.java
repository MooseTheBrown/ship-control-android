package com.github.moosethebrown.shipcontrol.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Query response message
 */
public class ShipQueryResponse {
    public static class PositionData {
        @SerializedName("num_satellites")
        private int numSatellites;
        private double latitude;
        private double longitude;
        @SerializedName("speed_knots")
        private double speedKnots;
        @SerializedName("speed_km")
        private double speedKm;
        private double angle;

        public int getNumSatellites() { return numSatellites; }
        public double getLatitude() { return latitude; }
        public double getLongitude() { return longitude; }
        public double getSpeedKnots() { return speedKnots; }
        public double getSpeedKm() { return speedKm; }
        public double getAngle() { return angle; }
    }

    public static class ShipData {
        private String speed;
        private String steering;

        public String getSpeed() { return speed; }
        public String getSteering() { return steering; }
    }

    private PositionData positionData;
    private ShipData shipData;
    private List<Waypoint> waypoints;
    private String error;

    public PositionData getPositionData() { return positionData; }
    public ShipData getShipData() { return shipData; }
    public List<Waypoint> getWaypoints() { return waypoints; }
    public String getError() { return error; }
}
