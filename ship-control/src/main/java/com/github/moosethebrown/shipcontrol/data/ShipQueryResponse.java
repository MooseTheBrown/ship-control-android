package com.github.moosethebrown.shipcontrol.data;

/**
 * Query response message
 */
public class ShipQueryResponse {
    private final String speed;
    private final String steering;

    public ShipQueryResponse(final String speed, final String steering) {
        this.speed = speed;
        this.steering = steering;
    }

    public String getSpeed() { return speed; }
    public String getSteering() { return steering; }
}
