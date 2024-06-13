package com.github.moosethebrown.shipcontrol;

import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;

import com.github.moosethebrown.shipcontrol.data.ShipControl;

public class ControllerHandler {
    private static final String LOG_TAG = "ControllerHandler";
    private final ShipControl shipControl;
    private final boolean useTwoJoysticks;

    public ControllerHandler(final ShipControl shipControl, boolean useTwoJoysticks) {
        this.shipControl = shipControl;
        this.useTwoJoysticks = useTwoJoysticks;
    }

    public boolean handleMotionEvent(MotionEvent event) {
        if ((event.getSource() & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK &&
                event.getAction() == MotionEvent.ACTION_MOVE) {
            final int historySize = event.getHistorySize();
            for (int i = 0; i < historySize; i++) {
                processJoystickInput(event, i);
            }
            processJoystickInput(event, -1);
            return true;
        }
        return false;
    }

    private void processJoystickInput(MotionEvent event, int historyPos) {
        InputDevice inputDevice = event.getDevice();
        // speed is changed by AXIS_Y
        float y = getCenteredAxis(event, inputDevice, MotionEvent.AXIS_Y, historyPos);
        if (y > 0) {
            float max = inputDevice.getMotionRange(MotionEvent.AXIS_Y, event.getSource()).getMax();
            int speed = (int)(((y * 10) / max) + 0.5);
            Log.d(LOG_TAG, "setting speed to " + speed);
            shipControl.setSpeed(speed);
        }
        else if (y < 0) {
            float min = inputDevice.getMotionRange(MotionEvent.AXIS_Y, event.getSource()).getMin();
            int speed = (int)(((y * 10) / min) - 0.5);
            Log.d(LOG_TAG, "setting speed to " + speed);
            shipControl.setSpeed(speed);
        }

        float x;
        int steeringAxis;
        if (useTwoJoysticks) {
            steeringAxis = MotionEvent.AXIS_Z;
        }
        else {
            steeringAxis = MotionEvent.AXIS_X;
        }
        x = getCenteredAxis(event, inputDevice, steeringAxis, historyPos);
        if (x > 0) {
            float max = inputDevice.getMotionRange(steeringAxis, event.getSource()).getMax();
            int steering = (int)(((x * 10) / max) + 0.5);
            Log.d(LOG_TAG, "setting steering to " + steering);
            shipControl.setSteering(steering);
        }
        else if (x < 0) {
            float min = inputDevice.getMotionRange(steeringAxis, event.getSource()).getMin();
            int steering = (int)(((x * 10) / min) + 0.5);
            Log.d(LOG_TAG, "setting steering to " + steering);
            shipControl.setSteering(steering);
        }
    }

    private float getCenteredAxis(MotionEvent event,
                                  InputDevice device, int axis, int historyPos) {
        final InputDevice.MotionRange range =
                device.getMotionRange(axis, event.getSource());

        // A joystick at rest does not always report an absolute position of
        // (0,0). Use the getFlat() method to determine the range of values
        // bounding the joystick axis center.
        if (range != null) {
            final float flat = range.getFlat();
            final float value =
                    historyPos < 0 ? event.getAxisValue(axis):
                            event.getHistoricalAxisValue(axis, historyPos);

            // Ignore axis values that are within the 'flat' region of the
            // joystick axis center.
            if (Math.abs(value) > flat) {
                return value;
            }
        }
        return 0;
    }
}