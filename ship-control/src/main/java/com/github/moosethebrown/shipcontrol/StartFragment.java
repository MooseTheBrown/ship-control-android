package com.github.moosethebrown.shipcontrol;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProviders;
import com.github.moosethebrown.shipcontrol.data.ShipViewModel;


/**
 * Splash screen fragment
 */
public class StartFragment extends Fragment {

    Listener listener;
    ConnectionSettingsProvider connSettingsProvider;

    public StartFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_start, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        ShipViewModel viewModel = ViewModelProviders.of(getActivity()).get(ShipViewModel.class);
        if (viewModel.getConnected().getValue().booleanValue() == true) {
            // already connected, report immediately
            listener.onConnected(true);
        }
        else {
            // connect to MQTT broker
            try {
                viewModel.connect(connSettingsProvider.getBroker());
            }
            catch (Exception e) {
                Log.e(MainActivity.LOG_TAG, e.getMessage());
                for (StackTraceElement element : e.getStackTrace()) {
                    Log.e(MainActivity.LOG_TAG, element.toString());
                }
                showLastError(e);
            }
            // get notified about connection to broker being established and report to activity
            viewModel.getConnected().observe(this, (Boolean connected) -> {
                if (connected.booleanValue() == true) {
                    listener.onConnected(false);
                }
            });
            // get notified about connection issues
            viewModel.getLastError().observe(this, this::showLastError);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Listener) {
            listener = (Listener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement StartFragment.Listener");
        }

        if (context instanceof ConnectionSettingsProvider) {
            connSettingsProvider = (ConnectionSettingsProvider) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement ConnectionSettingsProvider");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public interface Listener {
        void onConnected(boolean already);
    }

    public interface ConnectionSettingsProvider {
        String getBroker();
    }

    private void showLastError(Throwable error) {
        TextView status = getView().findViewById(R.id.textView);
        status.setText(error.getMessage());
    }
}
