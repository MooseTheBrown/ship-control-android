package com.github.moosethebrown.shipcontrol;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import com.github.moosethebrown.shipcontrol.data.ShipControl;
import com.github.moosethebrown.shipcontrol.data.ShipViewModel;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;


/**
 * Fragment for controlling individual ship
 */
public class ControlFragment extends Fragment {

    private ShipControl shipControl = null;
    private Handler handler = null;
    private ControlSettingsProvider settingsProvider = null;

    public ControlFragment() {
        // Required empty public constructor
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

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        // get current data from the view model and subscribe to its updates
        ShipViewModel viewModel = ViewModelProviders.of(getActivity()).get(ShipViewModel.class);
        shipControl = viewModel.getShipControl();
        // current speed
        setCurrentSpeed(viewModel.getCurrentSpeed().getValue());
        viewModel.getCurrentSpeed().observe(this, this::setCurrentSpeed);
        // current steering
        setCurrentSteering(viewModel.getCurrentSteering().getValue());
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

    private void setCurrentSteering(final String currentSteering) {
        TextView v = getView().findViewById(R.id.steering);
        v.setText(currentSteering);
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
