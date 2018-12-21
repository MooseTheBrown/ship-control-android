package com.github.moosethebrown.shipcontrol.data;

/**
 * Command message used by ShipHandler to control ShipWorker
 */
public class ShipWorkerCommand {
    public static final byte CMD_CONNECT = 1;
    public static final byte CMD_DISCONNECT = 2;
    public static final byte CMD_CONNECT_SHIP = 3;
    public static final byte CMD_REQUEST = 4;

    private final byte cmd;
    private ShipRequest rq;
    private String shipId;

    public ShipWorkerCommand(byte cmd, ShipRequest rq) {
        this.cmd = cmd;
        this.rq = rq;
        this.shipId = "";
    }

    public ShipWorkerCommand(byte cmd, final String shipId) {
        this.cmd = cmd;
        this.rq = null;
        this.shipId = shipId;
    }

    public ShipWorkerCommand(byte cmd) {
        this.cmd = cmd;
        this.rq = null;
        this.shipId = "";
    }

    public byte getCmd() { return cmd; }
    public ShipRequest getRq() { return rq; }
    public String getShipId() { return shipId; }
}
