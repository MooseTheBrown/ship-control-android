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
    public static final String CMD_SET_SPEED = "set_speed";
    public static final String CMD_SET_STEERING = "set_steering";
    public static final String CMD_SET_WAYPOINTS = "set_waypoints";
    public static final String CMD_ADD_WAYPOINT = "add_waypoint";
    public static final String CMD_CLEAR_WAYPOINTS= "clear_waypoints";
    public static final String CMD_SET_HOME_WAYPOINT = "set_home_waypoint";
    public static final String CMD_NAV_START = "nav_start";
    public static final String CMD_NET_LOSS = "net_loss";
    public static final String CMD_START_CALIBRATION = "start_calibration";
    public static final String CMD_STOP_CALIBRATION = "stop_calibration";

    private final String type;
    private final String cmd;
    private final String data;

    // cmd argument is ignored for query type
    public ShipRequest(final String type, final String cmd, final String data) {
        this.type = type;
        this.cmd = cmd;
        this.data = data;
    }

    public String getType() { return type; }
    public String getCmd() { return cmd; }
}
