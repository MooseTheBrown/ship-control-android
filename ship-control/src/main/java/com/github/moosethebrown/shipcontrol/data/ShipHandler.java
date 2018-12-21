package com.github.moosethebrown.shipcontrol.data;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * ShipHandler manages communications with a ship via MQTT protocol.
 * It uses ShipWorker to perform MQTT operations in a background thread.
 * Notifications to the main activity are sent using ShipCallback interface.
 * ShipCallback methods are always called from a background thread.
 */
public class ShipHandler implements ShipControl {

    private ShipWorker worker;
    private LinkedBlockingQueue<ShipWorkerCommand> workerQueue =
            new LinkedBlockingQueue<>(10);

    public ShipHandler(final String broker, ShipCallback callback) throws MqttException {
        worker = new ShipWorker(broker, callback, workerQueue);
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
        ShipRequest rq = new ShipRequest(ShipRequest.TYPE_CMD, ShipRequest.CMD_SPEED_UP);
        workerQueue.add(new ShipWorkerCommand(ShipWorkerCommand.CMD_REQUEST, rq));
    }

    public void speedDown() throws IllegalStateException {
        ShipRequest rq = new ShipRequest(ShipRequest.TYPE_CMD, ShipRequest.CMD_SPEED_DOWN);
        workerQueue.add(new ShipWorkerCommand(ShipWorkerCommand.CMD_REQUEST, rq));
    }

    public void turnLeft() throws IllegalStateException {
        ShipRequest rq = new ShipRequest(ShipRequest.TYPE_CMD, ShipRequest.CMD_TURN_LEFT);
        workerQueue.add(new ShipWorkerCommand(ShipWorkerCommand.CMD_REQUEST, rq));
    }

    public void turnRight() throws IllegalStateException {
        ShipRequest rq = new ShipRequest(ShipRequest.TYPE_CMD, ShipRequest.CMD_TURN_RIGHT);
        workerQueue.add(new ShipWorkerCommand(ShipWorkerCommand.CMD_REQUEST, rq));
    }

    public void query() throws IllegalStateException {
        ShipRequest rq = new ShipRequest(ShipRequest.TYPE_QUERY, "");
        workerQueue.add(new ShipWorkerCommand(ShipWorkerCommand.CMD_REQUEST, rq));
    }
}
