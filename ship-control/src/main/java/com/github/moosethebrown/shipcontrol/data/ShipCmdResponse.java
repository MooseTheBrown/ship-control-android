package com.github.moosethebrown.shipcontrol.data;

/**
 * Command response message
 */
public class ShipCmdResponse {
    public static final String STATUS_OK = "ok";
    public static final String STATUS_FAIL = "fail";

    private final String status;
    private final String error;

    public ShipCmdResponse(final String status, final String error) {
        this.status = status;
        this.error = error;
    }

    public String getStatus() { return status; }
    public String getError() { return error; }
}
