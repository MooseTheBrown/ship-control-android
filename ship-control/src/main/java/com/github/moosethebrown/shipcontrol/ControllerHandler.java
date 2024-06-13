package com.github.moosethebrown.shipcontrol;

import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
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

    public boolean handleKeyEvent(KeyEvent event) {
        if ((event.getSource() & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD &&
                event.getAction() == KeyEvent.ACTION_DOWN) {
            if (event.getRepeatCount() == 0) {
                Log.d(LOG_TAG, "KeyEvent: " + event.getKeyCode());
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_BUTTON_Y:
                        shipControl.speedUp();
                        return true;
                    case KeyEvent.KEYCODE_BUTTON_A:
                        shipControl.speedDown();
                        return true;
                    case KeyEvent.KEYCODE_BUTTON_X:
                        shipControl.turnLeft();
                        return true;
                    case KeyEvent.KEYCODE_BUTTON_B:
                        shipControl.turnRight();
                        return true;
                }
            }
        }

        return false;
    }

    private void processJoystickInput(MotionEvent event, int historyPos) {
        InputDevice inputDevice = event.getDevice();

        // speed is changed by AXIS_Y
        float y = getCenteredAxis(event, inputDevice, MotionEvent.AXIS_Y, historyPos);
        y *= -1;
        if (y > 0) {
            float max = inputDevice.getMotionRange(MotionEvent.AXIS_Y, event.getSource()).getMax();
            Log.d(LOG_TAG, "y = " + y + ", max = " + max);
            int speed = (int)(((y * 10) / max) + 0.5);
            Log.d(LOG_TAG, "setting speed to " + speed);
            shipControl.setSpeed(speed);
        }
        else if (y < 0) {
            float min = inputDevice.getMotionRange(MotionEvent.AXIS_Y, event.getSource()).getMin();
            Log.d(LOG_TAG, "y = " + y + ", min = " + min);
            int speed = (int)(((y * 10) / Math.abs(min)) - 0.5);
            Log.d(LOG_TAG, "setting speed to " + speed);
            shipControl.setSpeed(speed);
        }
        else {
            shipControl.setSpeed(0);
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
            Log.d(LOG_TAG, "x = " + x + ", max = " + max);
            int steering = (int)(((x * 10) / max) + 0.5);
            Log.d(LOG_TAG, "setting steering to " + steering);
            shipControl.setSteering(steering);
        }
        else if (x < 0) {
            float min = inputDevice.getMotionRange(steeringAxis, event.getSource()).getMin();
            Log.d(LOG_TAG, "x = " + x + ", min = " + min);
            int steering = (int)(((x * 10) / Math.abs(min)) - 0.5);
            Log.d(LOG_TAG, "setting steering to " + steering);
            shipControl.setSteering(steering);
        }
        else {
            shipControl.setSteering(0);
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
