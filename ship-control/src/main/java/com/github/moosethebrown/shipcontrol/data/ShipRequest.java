package com.github.moosethebrown.shipcontrol.data;

/**
 * Request message for the ship-control module
 */
public class ShipRequest {
    // request types
    public static final String TYPE_CMD = "cmd";
    public static final String TYPE_QUERY = "query";

    // commands
    public static final String CMD_SPEED_UP = "speed_up";
    public static final String CMD_SPEED_DOWN = "speed_down";
    public static final String CMD_TURN_LEFT = "turn_left";
    public static final String CMD_TURN_RIGHT = "turn_right";

    private final String type;
    private final String cmd;

    // cmd argument is ignored for query type
    public ShipRequest(final String type, final String cmd) {
        this.type = type;
        this.cmd = cmd;
    }

    public String getType() { return type; }
    public String getCmd() { return cmd; }
}
