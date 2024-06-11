package com.github.moosethebrown.shipcontrol.data;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * ShipHandler manages communications with a ship via MQTT protocol.
 * It uses ShipWorker to perform MQTT operations in a background thread.
 * Notifications to the main activity are sent using ShipCallback interface.
 * ShipCallback methods are always called from a background thread.
 */
public class ShipHandler implements ShipControl {

    private ShipWorker worker;
    private final Map<Integer, String> steeringMap;
    private final Map<Integer, String> speedMap;

    private LinkedBlockingQueue<ShipWorkerCommand> workerQueue =
            new LinkedBlockingQueue<>(10);

    public ShipHandler(final String broker, final String username, final String password, ShipCallback callback) throws MqttException {
        worker = new ShipWorker(broker, username, password, callback, workerQueue);

        steeringMap = new HashMap<Integer, String>();
        steeringMap.put(0, "straight");
        steeringMap.put(1, "right10");
        steeringMap.put(2, "right20");
        steeringMap.put(3, "right30");
        steeringMap.put(4, "right40");
        steeringMap.put(5, "right50");
        steeringMap.put(6, "right60");
        steeringMap.put(7, "right70");
        steeringMap.put(8, "right80");
        steeringMap.put(9, "right90");
        steeringMap.put(10, "right100");
        steeringMap.put(-1, "left10");
        steeringMap.put(-2, "left20");
        steeringMap.put(-3, "left30");
        steeringMap.put(-4, "left40");
        steeringMap.put(-5, "left50");
        steeringMap.put(-6, "left60");
        steeringMap.put(-7, "left70");
        steeringMap.put(-8, "left80");
        steeringMap.put(-9, "left90");
        steeringMap.put(-10, "left100");

        speedMap = new HashMap<Integer, String>();
        speedMap.put(0, "stop");
        speedMap.put(1, "fwd10");
        speedMap.put(2, "fwd20");
        speedMap.put(3, "fwd30");
        speedMap.put(4, "fwd40");
        speedMap.put(5, "fwd50");
        speedMap.put(6, "fwd60");
        speedMap.put(7, "fwd70");
        speedMap.put(8, "fwd80");
        speedMap.put(9, "fwd90");
        speedMap.put(10, "fwd100");
        speedMap.put(-1,"rev10");
        speedMap.put(-2, "rev20");
        speedMap.put(-3, "rev30");
        speedMap.put(-4, "rev40");
        speedMap.put(-5, "rev50");
        speedMap.put(-6, "rev60");
        speedMap.put(-7, "rev70");
        speedMap.put(-8, "rev80");
        speedMap.put(-9, "rev90");
        speedMap.put(-10, "rev100");
    }

    public void connectToBroker() throws IllegalStateException {
        worker.start();
        workerQueue.add(new ShipWorkerCommand(ShipWorkerCommand.CMD_CONNECT));
    }

    public void disconnectFromBroker() throws IllegalStateException {
        workerQueue.add(new ShipWorkerCommand(ShipWorkerCommand.CMD_DISCONNECT));
    }

    public void connectShip(String shipId) throws IllegalStateException {
        workerQueue.add(new ShipWorkerCommand(ShipWorkerCommand.CMD_CONNECT_SHIP, shipId));
    }

    public void speedUp() throws IllegalStateException {
        ShipRequest rq = new ShipRequest(ShipRequest.TYPE_CMD, ShipRequest.CMD_SPEED_UP, "");
        workerQueue.add(new ShipWorkerCommand(ShipWorkerCommand.CMD_REQUEST, rq));
    }

    public void speedDown() throws IllegalStateException {
        ShipRequest rq = new ShipRequest(ShipRequest.TYPE_CMD, ShipRequest.CMD_SPEED_DOWN, "");
        workerQueue.add(new ShipWorkerCommand(ShipWorkerCommand.CMD_REQUEST, rq));
    }

    public void turnLeft() throws IllegalStateException {
        ShipRequest rq = new ShipRequest(ShipRequest.TYPE_CMD, ShipRequest.CMD_TURN_LEFT, "");
        workerQueue.add(new ShipWorkerCommand(ShipWorkerCommand.CMD_REQUEST, rq));
    }

    public void turnRight() throws IllegalStateException {
        ShipRequest rq = new ShipRequest(ShipRequest.TYPE_CMD, ShipRequest.CMD_TURN_RIGHT, "");
        workerQueue.add(new ShipWorkerCommand(ShipWorkerCommand.CMD_REQUEST, rq));
    }

    public void setSpeed(String speed) throws IllegalStateException {
        ShipRequest rq = new ShipRequest(ShipRequest.TYPE_CMD, ShipRequest.CMD_SET_SPEED, speed);
        workerQueue.add(new ShipWorkerCommand(ShipWorkerCommand.CMD_REQUEST, rq));
    }

    public void setSpeed(int speed) throws IllegalStateException {
        String strSpeed = Optional.ofNullable(speedMap.get(speed)).orElse("stop");
        setSpeed(strSpeed);
    }

    public void setSteering(String steering) throws IllegalStateException {
        ShipRequest rq = new ShipRequest(ShipRequest.TYPE_CMD, ShipRequest.CMD_SET_STEERING, steering);
        workerQueue.add(new ShipWorkerCommand(ShipWorkerCommand.CMD_REQUEST, rq));
    }

    public void setSteering(int steering) throws IllegalStateException {
        String strSteering = Optional.ofNullable(steeringMap.get(steering)).orElse("straight");
        setSteering(strSteering);
    }

    public void query() throws IllegalStateException {
        ShipRequest rq = new ShipRequest(ShipRequest.TYPE_QUERY, "", "");
        workerQueue.add(new ShipWorkerCommand(ShipWorkerCommand.CMD_REQUEST, rq));
    }

    public Map<Integer, String> getSpeedMap() { return speedMap; }
    public Map<Integer, String> getSteeringMap() { return steeringMap; }
}
