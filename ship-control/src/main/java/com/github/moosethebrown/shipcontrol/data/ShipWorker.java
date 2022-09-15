package com.github.moosethebrown.shipcontrol.data;

import android.net.SSLCertificateSocketFactory;
import android.util.Log;

import com.google.gson.Gson;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.LinkedBlockingQueue;

import javax.net.ssl.SSLContext;

/**
 * ShipWorker performs MQTT communications with a ship in a background thread
 */
public class ShipWorker extends Thread implements MqttCallback {
    private static final String ANNOUNCE_TOPIC = "Announce";
    private static final String LOG_TAG = "ship-control";

    private String brokerUsername;
    private String brokerPassword;
    private String shipId = "";
    private String rqTopic;
    private String respTopic;
    private ShipCallback callback;
    private LinkedBlockingQueue<ShipWorkerCommand> workerQueue;
    private MqttAsyncClient mqttClient;
    private String clientId;
    private Gson gson = new Gson();

    public ShipWorker(final String broker, final String brokerUsername, final String brokerPassword,
                      ShipCallback callback, LinkedBlockingQueue<ShipWorkerCommand> workerQueue) throws MqttException {
        this.callback = callback;
        this.workerQueue = workerQueue;
        this.brokerUsername = brokerUsername;
        this.brokerPassword = brokerPassword;
        this.clientId = MqttAsyncClient.generateClientId();
        this.mqttClient = new MqttAsyncClient(broker, this.clientId, new MemoryPersistence());
        this.mqttClient.setCallback(this);

        Log.i(LOG_TAG, "ShipWorker initialized");
    }

    @Override
    public void run() {
        boolean stop = false;

        while (true) {
            ShipWorkerCommand cmd;
            try {
                cmd = workerQueue.take();
            }
            catch (InterruptedException e) {
                break;
            }

            switch (cmd.getCmd()) {
                case ShipWorkerCommand.CMD_CONNECT:
                    connect();
                    break;
                case ShipWorkerCommand.CMD_DISCONNECT:
                    disconnect();
                    stop = true;
                    break;
                case ShipWorkerCommand.CMD_CONNECT_SHIP:
                    connectToShip(cmd.getShipId());
                    break;
                case ShipWorkerCommand.CMD_REQUEST:
                    sendRequest(cmd.getRq());
                    break;
                default:
                    break;
            }

            if (stop) {
                break;
            }
        }
    }

    // MqttCallback implementation
    @Override
    public void connectionLost(Throwable cause) {
        Log.i(LOG_TAG, "ShipWorker.connectionLost");
        callback.onError(cause);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // do nothing
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        // announce message
        if (topic.equals(ANNOUNCE_TOPIC)) {
            Log.i(LOG_TAG, "ShipWorker received announce message");
            callback.onAnnounce(message.toString());
        }
        // response message
        else if (topic.equals(respTopic)) {
            Log.i(LOG_TAG, "ShipWorker received response message");

            String msg = message.toString();

            // either command or query response
            if (msg.contains("status")) {
                ShipCmdResponse resp = gson.fromJson(msg, ShipCmdResponse.class);
                callback.onCommandResponse(resp);
            }
            else if (msg.contains("speed")) {
                ShipQueryResponse resp = gson.fromJson(msg, ShipQueryResponse.class);
                callback.onQueryResponse(resp);
            }
        }
    }
    // end of MqttCallback implementation

    // Connect to MQTT broker
    private void connect() {
        MqttConnectOptions connOpts = new MqttConnectOptions();
        try {
            connOpts.setUserName(this.brokerUsername);
            connOpts.setPassword(this.brokerPassword.toCharArray());
            connOpts.setAutomaticReconnect(true);
            connOpts.setCleanSession(true);
            connOpts.setSocketFactory(SSLContext.getDefault().getSocketFactory());
        }
        catch (Exception e) {
           Log.e(LOG_TAG, "Failed to initialize MqttConnectOptions: ", e);
        }

        try {
            IMqttToken connToken = mqttClient.connect(connOpts);
            connToken.waitForCompletion();
            callback.onBrokerConnected();
            IMqttToken subscribeToken = mqttClient.subscribe(ANNOUNCE_TOPIC, 2);
            subscribeToken.waitForCompletion();

            Log.i(LOG_TAG, "ShipWorker connected to MQTT broker");
        }
        catch (MqttException e) {
            Log.e(LOG_TAG, "ShipWorker failed to connect to broker: ", e);

            callback.onError(e);
        }
    }

    // Disconnect from MQTT broker
    private void disconnect() {
        try {
            mqttClient.disconnect(0);
            Log.i(LOG_TAG, "ShipWorker disconnected from broker");
        }
        catch (Exception e) {
            // just log it, app is unlikely to be interested
            Log.e(LOG_TAG, "ShipWorker failed to disconnect from broker: " + e.getMessage());
        }
    }

    private void connectToShip(final String shipId) {
        try {
            // if we are already connected to a ship, unsubscribe from its response topic first
            if ((!this.shipId.equals("")) && (!this.shipId.equals(shipId))) {
                IMqttToken token = mqttClient.unsubscribe(respTopic);
                token.waitForCompletion();
            }

            this.shipId = shipId;
            rqTopic = new StringBuilder("ship/").append(shipId).append("/request").toString();
            respTopic = new StringBuilder("ship/").append(shipId).append("/response").toString();
            mqttClient.subscribe(respTopic, 2);

            Log.i(LOG_TAG, "ShipWorker connected to ship " + shipId);
        }
        catch (Exception e) {
            Log.e(LOG_TAG, "ShipWorker failed to connect to ship " + shipId);
            callback.onError(e);
        }
    }

    private void sendRequest(ShipRequest rq) {
        try {
            if (shipId.equals("")) {
                throw new Exception("Attempted to send request before connecting to a ship");
            }
            String msg = gson.toJson(rq);
            byte[] payload = msg.getBytes(Charset.forName("UTF-8"));
            mqttClient.publish(rqTopic, payload, 2, false);

            Log.i(LOG_TAG, "ShipWorker sent request to ship");
        }
        catch (Exception e) {
            Log.e(LOG_TAG, "ShipWorker failed to send request to ship: " + e.getMessage());
            callback.onError(e);
        }
    }
}
