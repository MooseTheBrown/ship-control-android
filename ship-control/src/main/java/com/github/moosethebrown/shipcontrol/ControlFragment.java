package com.github.moosethebrown.shipcontrol;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import com.github.moosethebrown.shipcontrol.data.ShipControl;
import com.github.moosethebrown.shipcontrol.data.ShipViewModel;
import com.github.moosethebrown.shipcontrol.data.Waypoint;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;


/**
 * Fragment for controlling individual ship
 */
public class ControlFragment extends Fragment {

    private static final String LOG_TAG = "ControlFragment";

    private ShipControl shipControl = null;
    private Handler handler = null;
    private ControlSettingsProvider settingsProvider = null;
    private ControlFragmentListener listener = null;

    public ControlFragment() {
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
            }
        });
        // turn right button
        ImageButton btnRight = view.findViewById(R.id.turnRightButton);
        btnRight.setOnClickListener(v -> {
            if (shipControl != null) {
                shipControl.turnRight();
            }
        });
        // speed up button
        ImageButton btnSpeedUp = view.findViewById(R.id.speedUpButton);
        btnSpeedUp.setOnClickListener(v -> {
            if (shipControl != null) {
                shipControl.speedUp();
            }
        });
        // speed down button
        ImageButton btnSpeedDown = view.findViewById(R.id.speedDownButton);
        btnSpeedDown.setOnClickListener(v -> {
            if (shipControl != null) {
                shipControl.speedDown();
            }
        });

        // steering bar
        SeekBar steeringBar = view.findViewById(R.id.steeringBar);
        steeringBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.d(LOG_TAG, "steering bar onProgressChanged(), progress=" + progress);
                if ((fromUser) && (shipControl != null)) {
                    shipControl.setSteering(progress);
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
                if ((fromUser) && (shipControl != null)) {
                    shipControl.setSpeed(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // video mode button
        Button videoModeButton = view.findViewById(R.id.videoModeButton);
        videoModeButton.setOnClickListener(v -> {
            if (listener!= null) {
                listener.onVideoButtonClicked();
            }
        });

        // map button
        Button mapButton = view.findViewById(R.id.mapButton);
        mapButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMapButtonClicked();
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
        viewModel.getCurrentSpeed().observe(this, this::setCurrentSpeed);
        // current steering
        final String curSteering = viewModel.getCurrentSteering().getValue();
        setCurrentSteering(curSteering);
        viewModel.getCurrentSteering().observe(this, this::setCurrentSteering);
        // num satellites
        final Integer numSatellites = viewModel.getNumSatellites().getValue();
        setNumSatellites(numSatellites);
        viewModel.getNumSatellites().observe(this, this::setNumSatellites);
        // gps speed in kmh
        final Double gpsSpeed = viewModel.getSpeedKm().getValue();
        setGpsSpeed(gpsSpeed);
        viewModel.getSpeedKm().observe(this, this::setGpsSpeed);
        // gps speed in knots
        final Double gpsSpeedKnots = viewModel.getSpeedKnots().getValue();
        setGpsSpeedKnots(gpsSpeedKnots);
        viewModel.getSpeedKnots().observe(this, this::setGpsSpeedKnots);
        // ship position
        final Waypoint shipPosition = viewModel.getShipPosition().getValue();
        setShipPosition(shipPosition);
        viewModel.getShipPosition().observe(this, this::setShipPosition);
        // angle
        final Double angle = viewModel.getAngle().getValue();
        setAngle(angle);
        viewModel.getAngle().observe(this, this::setAngle);

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
        if (context instanceof ControlFragmentListener) {
            listener = (ControlFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement ControlFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public interface ControlSettingsProvider {
        long getQueryTimeout();
    }

    public interface ControlFragmentListener {
        void onVideoButtonClicked();
        void onMapButtonClicked();
    }

    private void setCurrentSpeed(final String currentSpeed) {
        TextView v = getView().findViewById(R.id.speed);
        v.setText(currentSpeed);
        setSpeedBar(currentSpeed);
    }

    private void setSpeedBar(final String speed) {
        SeekBar speedBar = getView().findViewById(R.id.speedBar);
        for (Map.Entry<Integer, String> speedEntry : shipControl.getSpeedMap().entrySet()) {
            if (speedEntry.getValue().equals(speed)) {
                speedBar.setProgress(speedEntry.getKey().intValue(), false);
            }
        }
    }

    private void setCurrentSteering(final String currentSteering) {
        TextView v = getView().findViewById(R.id.steering);
        v.setText(currentSteering);
        setSteeringBar(currentSteering);
    }

    private void setSteeringBar(final String steering) {
        SeekBar steeringBar = getView().findViewById(R.id.steeringBar);
        for (Map.Entry<Integer, String> steeringEntry : shipControl.getSteeringMap().entrySet()) {
            if (steeringEntry.getValue().equals(steering)) {
                steeringBar.setProgress(steeringEntry.getKey().intValue(), false);
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void setGpsSpeed(final Double gpsSpeed) {
        if (gpsSpeed == null) {
            return;
        }
        Double roundedGpsSpeed = round(gpsSpeed, 1);
        TextView gpsSpeedView = getView().findViewById(R.id.gpsSpeed);
        gpsSpeedView.setText(roundedGpsSpeed.toString() + " " + getResources().getString(R.string.kmh));
    }

    @SuppressLint("SetTextI18n")
    private void setGpsSpeedKnots(final Double gpsSpeed) {
        if (gpsSpeed == null) {
            return;
        }
        Double roundedGpsSpeed = round(gpsSpeed, 1);
        TextView gpsSpeedView = getView().findViewById(R.id.gpsSpeedKnots);
        gpsSpeedView.setText(roundedGpsSpeed.toString() + " " + getResources().getString(R.string.knots));
    }

    @SuppressLint("SetTextI18n")
    private void setNumSatellites(final Integer numSatellites) {
        if (numSatellites == null) {
            return;
        }
        TextView numSatellitesView = getView().findViewById(R.id.numSatellites);
        numSatellitesView.setText(numSatellites.toString());
    }

    @SuppressLint("SetTextI18n")
    private void setShipPosition(final Waypoint newPosition) {
        if (newPosition == null) {
            return;
        }
        double roundedLatitude = round(newPosition.getLatitude(), 6);
        TextView latitudeView = getView().findViewById(R.id.latitude);
        latitudeView.setText(roundedLatitude + getResources().getString(R.string.degrees));

        double roundedLongitude = round(newPosition.getLongitude(), 6);
        TextView longitudeView = getView().findViewById(R.id.longitude);
        longitudeView.setText(roundedLongitude + getResources().getString(R.string.degrees));
    }

    @SuppressLint("SetTextI18n")
    private void setAngle(final Double angle) {
        if (angle == null) {
            return;
        }
        Double roundedAngle = round(angle, 4);
        TextView angleView = getView().findViewById(R.id.angle);
        angleView.setText(roundedAngle.toString() + getResources().getString(R.string.degrees));
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

    private double round(double value, int precision) {
        BigDecimal bigDecimal = BigDecimal.valueOf(value);
        bigDecimal = bigDecimal.setScale(precision, RoundingMode.HALF_UP);
        return bigDecimal.doubleValue();
    }
}
