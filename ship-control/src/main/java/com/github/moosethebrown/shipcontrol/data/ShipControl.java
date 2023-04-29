package com.github.moosethebrown.shipcontrol.data;

public interface ShipControl {
    void speedUp() throws IllegalStateException;
    void speedDown() throws IllegalStateException;
    void turnLeft() throws IllegalStateException;
    void turnRight() throws IllegalStateException;
    void setSpeed(String speed) throws IllegalStateException;
    void setSteering(String steering) throws IllegalStateException;
    void query() throws IllegalStateException;
}
