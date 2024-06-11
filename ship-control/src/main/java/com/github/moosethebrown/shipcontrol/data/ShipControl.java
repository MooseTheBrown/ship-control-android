package com.github.moosethebrown.shipcontrol.data;

import java.util.Map;

public interface ShipControl {
    void speedUp() throws IllegalStateException;
    void speedDown() throws IllegalStateException;
    void turnLeft() throws IllegalStateException;
    void turnRight() throws IllegalStateException;
    void setSpeed(String speed) throws IllegalStateException;
    // value range: -10:10, 0 means "stop"
    void setSpeed(int speed) throws IllegalStateException;
    void setSteering(String steering) throws IllegalStateException;
    // value range: -10:10, 0 means "straight"
    void setSteering(int steering) throws IllegalStateException;
    void query() throws IllegalStateException;
    Map<Integer, String> getSpeedMap();
    Map<Integer, String> getSteeringMap();
}
