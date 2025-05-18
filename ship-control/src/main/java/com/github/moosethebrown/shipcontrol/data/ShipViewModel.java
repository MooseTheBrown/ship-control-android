package com.github.moosethebrown.shipcontrol.data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ShipViewModel accumulates ship data to be used in various UI components (fragments and their views)
 */
public class ShipViewModel extends ViewModel implements ShipCallback {
    // broker connection indicator
    private MutableLiveData<Boolean> connected = new MutableLiveData<>();
    // set of available ship ids
    private Set<String> shipIds = Collections.synchronizedSet(new HashSet<>());
    // last announced ship id
    private MutableLiveData<String> lastAnnounced = new MutableLiveData<>();
    // currently connected ship
    private MutableLiveData<String> currentShipId = new MutableLiveData<>();
    // connected ship data
    private MutableLiveData<Integer> numSatellites = new MutableLiveData<>();
    private MutableLiveData<Waypoint> shipPosition = new MutableLiveData<>();
    private MutableLiveData<Double> speedKnots = new MutableLiveData<>();
    private MutableLiveData<Double> speedKm = new MutableLiveData<>();
    private MutableLiveData<Double> angle = new MutableLiveData<>();
    private MutableLiveData<String> currentSpeed = new MutableLiveData<>();
    private MutableLiveData<String> currentSteering = new MutableLiveData<>();
    // last error reported by ShipHandler
    private MutableLiveData<Throwable> lastError = new MutableLiveData<>();

    // ShipHandler instance
    private ShipHandler shipHandler = null;

    public ShipViewModel() {
        numSatellites.setValue(0);
        speedKnots.setValue(0.0);
        speedKm.setValue(0.0);
        angle.setValue(0.0);
        connected.setValue(false);
    }

    public LiveData<Boolean> getConnected() {
        return connected;
    }

    public LiveData<String> getLastAnnounced() {
        return lastAnnounced;
    }

    public List<String> getAvailableShips() {
        synchronized (shipIds) {
            return new ArrayList<>(shipIds);
        }
    }

    // to be called from UI thread only
    public void setCurrentShipId(final String shipId) {
        shipHandler.connectShip(shipId);
        currentShipId.setValue(shipId);
    }

    public LiveData<String> getCurrentShipId() {
        return currentShipId;
    }

    public LiveData<Integer> getNumSatellites() { return numSatellites; }

    public LiveData<Waypoint> getShipPosition() { return shipPosition; }

    public LiveData<Double> getSpeedKm() { return speedKm; }

    public LiveData<Double> getSpeedKnots() { return speedKnots; }

    public LiveData<Double> getAngle() { return angle; }

    public LiveData<String> getCurrentSpeed() {
        return currentSpeed;
    }

    public LiveData<String> getCurrentSteering() {
        return currentSteering;
    }

    public void connect(final String broker, final String username, final String password) throws Exception {
        shipHandler = new ShipHandler(broker, username, password, this);
        shipHandler.connectToBroker();
    }

    public void disconnect() {
        if (shipHandler != null) {
            shipHandler.disconnectFromBroker();
        }
        connected.setValue(Boolean.valueOf(false));
    }

    public LiveData<Throwable> getLastError() {
        return lastError;
    }

    // provide interface for communicating with the ship
    public ShipControl getShipControl() {
        return shipHandler;
    }

    // ShipCallback implementation
    @Override
    public void onBrokerConnected() {
        connected.postValue(Boolean.valueOf(true));
    }

    @Override
    public void onAnnounce(final String shipId) {
        // add new ship to the set
        shipIds.add(shipId);
        lastAnnounced.postValue(shipId);
    }

    @Override
    public void onCommandResponse(ShipCmdResponse response) {
        // store error in case if failure
        if (response.getStatus() == ShipCmdResponse.STATUS_FAIL) {
            lastError.postValue(new Throwable(response.getError()));
        }
        // nothing to do in case of success
    }

    @Override
    public void onQueryResponse(ShipQueryResponse response) {
        numSatellites.postValue(response.getPositionData().getNumSatellites());
        shipPosition.postValue(new Waypoint(response.getPositionData().getLatitude(),
                response.getPositionData().getLongitude()));
        speedKnots.postValue(response.getPositionData().getSpeedKnots());
        speedKm.postValue(response.getPositionData().getSpeedKm());
        angle.postValue(response.getPositionData().getAngle());
        currentSpeed.postValue(response.getShipData().getSpeed());
        currentSteering.postValue(response.getShipData().getSteering());
    }

    @Override
    public void onError(Throwable exception) {
        lastError.postValue(exception);
    }
    // end of ShipCallback implementation

    @Override
    protected void onCleared() {
        // view model is about to be destroyed, disconnect from the broker
        disconnect();
    }
}