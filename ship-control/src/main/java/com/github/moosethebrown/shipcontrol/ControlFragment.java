package com.github.moosethebrown.shipcontrol;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import com.github.moosethebrown.shipcontrol.data.ShipControl;
import com.github.moosethebrown.shipcontrol.data.ShipViewModel;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


/**
 * Fragment for controlling individual ship
 */
public class ControlFragment extends Fragment {

    private static final String LOG_TAG = "ControlFragment";

    private ShipControl shipControl = null;
    private Handler handler = null;
    private ControlSettingsProvider settingsProvider = null;
    private final Map<Integer, String> steeringMap;
    private final Map<Integer, String> speedMap;

    public ControlFragment() {
        steeringMap = new HashMap<Integer, String>();
        steeringMap.put(0, "straight");
        steeringMap.put(1, "right10");
        steeringMap.put(2, "right20");
        steeringMap.put(3, "right30");
        steeringMap.put(4, "right40");
        steeringMap.put(5, "right50");
        steeringMap.put(6, "right60");
        steeringMap.put(7, "right70");
        steeringMap.put(8, "right80");
        steeringMap.put(9, "right90");
        steeringMap.put(10, "right100");
        steeringMap.put(-1, "left10");
        steeringMap.put(-2, "left20");
        steeringMap.put(-3, "left30");
        steeringMap.put(-4, "left40");
        steeringMap.put(-5, "left50");
        steeringMap.put(-6, "left60");
        steeringMap.put(-7, "left70");
        steeringMap.put(-8, "left80");
        steeringMap.put(-9, "left90");
        steeringMap.put(-10, "left100");

        speedMap = new HashMap<Integer, String>();
        speedMap.put(0, "stop");
        speedMap.put(1, "fwd10");
        speedMap.put(2, "fwd20");
        speedMap.put(3, "fwd30");
        speedMap.put(4, "fwd40");
        speedMap.put(5, "fwd50");
        speedMap.put(6, "fwd60");
        speedMap.put(7, "fwd70");
        speedMap.put(8, "fwd80");
        speedMap.put(9, "fwd90");
        speedMap.put(10, "fwd100");
        speedMap.put(-1,"rev10");
        speedMap.put(-2, "rev20");
        speedMap.put(-3, "rev30");
        speedMap.put(-4, "rev40");
        speedMap.put(-5, "rev50");
        speedMap.put(-6, "rev60");
        speedMap.put(-7, "rev70");
        speedMap.put(-8, "rev80");
        speedMap.put(-9, "rev90");
        speedMap.put(-10, "rev100");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_control, container, false);

        // turn left button
        ImageButton btnLeft = view.findViewById(R.id.turnLeftButton);
        btnLeft.setOnClickListener(v -> {
            if (shipControl != null) {
                shipControl.turnLeft();
                adjustSteeringBar(false);
            }
        });
        // turn right button
        ImageButton btnRight = view.findViewById(R.id.turnRightButton);
        btnRight.setOnClickListener(v -> {
            if (shipControl != null) {
                shipControl.turnRight();
                adjustSteeringBar(true);
            }
        });
        // speed up button
        ImageButton btnSpeedUp = view.findViewById(R.id.speedUpButton);
        btnSpeedUp.setOnClickListener(v -> {
            if (shipControl != null) {
                shipControl.speedUp();
                adjustSpeedBar(true);
            }
        });
        // speed down button
        ImageButton btnSpeedDown = view.findViewById(R.id.speedDownButton);
        btnSpeedDown.setOnClickListener(v -> {
            if (shipControl != null) {
                shipControl.speedDown();
                adjustSpeedBar(false);
            }
        });

        // steering bar
        SeekBar steeringBar = view.findViewById(R.id.steeringBar);
        steeringBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.d(LOG_TAG, "steering bar onProgressChanged(), progress=" + progress);
                if (shipControl != null) {
                    String steering = Optional.ofNullable(steeringMap.get(progress)).orElse("straight");
                    shipControl.setSteering(steering);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // speed bar
        SeekBar speedBar = view.findViewById(R.id.speedBar);
        speedBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.d(LOG_TAG, "speed bar onProgressChanged, progress=" + progress);
                if (shipControl != null) {
                    String speed = Optional.ofNullable(speedMap.get(progress)).orElse("stop");
                    shipControl.setSpeed(speed);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        // get current data from the view model and subscribe to its updates
        ShipViewModel viewModel = ViewModelProviders.of(getActivity()).get(ShipViewModel.class);
        shipControl = viewModel.getShipControl();
        // current speed
        final String curSpeed = viewModel.getCurrentSpeed().getValue();
        setCurrentSpeed(curSpeed);
        setSpeedBar(curSpeed);
        viewModel.getCurrentSpeed().observe(this, this::setCurrentSpeed);
        // current steering
        final String curSteering = viewModel.getCurrentSteering().getValue();
        setCurrentSteering(curSteering);
        setSteeringBar(curSteering);
        viewModel.getCurrentSteering().observe(this, this::setCurrentSteering);

        // start periodic queries
        startQueryTimer();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopQueryTimer();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ControlSettingsProvider) {
            settingsProvider = (ControlSettingsProvider) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement ControlSettingsProvider");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public interface ControlSettingsProvider {
        long getQueryTimeout();
    }

    private void setCurrentSpeed(final String currentSpeed) {
        TextView v = getView().findViewById(R.id.speed);
        v.setText(currentSpeed);
    }

    private void adjustSpeedBar(boolean up) {
        SeekBar speedBar = getView().findViewById(R.id.speedBar);
        int currentProgress = speedBar.getProgress();
        int targetProgress = up ? currentProgress + 1 : currentProgress - 1;
        speedBar.setProgress(targetProgress, true);
    }

    private void setSpeedBar(final String speed) {
        SeekBar speedBar = getView().findViewById(R.id.speedBar);
        for (Map.Entry<Integer, String> speedEntry : speedMap.entrySet()) {
            if (speedEntry.getValue().equals(speed)) {
                speedBar.setProgress(speedEntry.getKey().intValue(), false);
            }
        }
    }

    private void setCurrentSteering(final String currentSteering) {
        TextView v = getView().findViewById(R.id.steering);
        v.setText(currentSteering);
    }

    private void adjustSteeringBar(boolean right) {
        SeekBar steeringBar = getView().findViewById(R.id.steeringBar);
        int currentProgress = steeringBar.getProgress();
        int targetProgress = right ? currentProgress + 1 : currentProgress - 1;
        steeringBar.setProgress(targetProgress, true);
    }

    private void setSteeringBar(final String steering) {
        SeekBar steeringBar = getView().findViewById(R.id.steeringBar);
        for (Map.Entry<Integer, String> steeringEntry : steeringMap.entrySet()) {
            if (steeringEntry.getValue().equals(steering)) {
                steeringBar.setProgress(steeringEntry.getKey().intValue(), false);
            }
        }
    }

    private void startQueryTimer() {
        if (handler == null) {
            handler = new Handler();
        }
        handler.postDelayed(() -> {
            if (shipControl != null) {
                shipControl.query();
                startQueryTimer();
            }
        }, settingsProvider.getQueryTimeout());
    }

    private void stopQueryTimer() {
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}
