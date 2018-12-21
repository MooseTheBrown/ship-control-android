package com.github.moosethebrown.shipcontrol.data;

/**
 * ShipCallback interface.
 * Its methods are called by the ShipHandler on a non-UI thread
 */
public interface ShipCallback {
    void onBrokerConnected();
    void onAnnounce(final String shipId);
    void onCommandResponse(ShipCmdResponse response);
    void onQueryResponse(ShipQueryResponse response);
    void onError(Throwable exception);
}
