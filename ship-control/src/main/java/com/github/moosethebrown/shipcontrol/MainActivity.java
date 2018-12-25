package com.github.moosethebrown.shipcontrol;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import com.github.moosethebrown.shipcontrol.data.ShipViewModel;

public class MainActivity extends AppCompatActivity
    implements StartFragment.Listener,
               StartFragment.ConnectionSettingsProvider,
               ShipSelectFragment.OnListFragmentInteractionListener,
               SharedPreferences.OnSharedPreferenceChangeListener,
               ControlFragment.ControlSettingsProvider {

    public static final String LOG_TAG = "ship-control";
    private static final String PREFS_BROKER_URI_KEY = "brokerURI";
    private static final String PREFS_QUERY_TIMEOUT_KEY = "queryTimeout";

    private ShipViewModel viewModel = null;
    private AppBarConfiguration appBarConfiguration;
    private Handler handler = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = ViewModelProviders.of(this).get(ShipViewModel.class);

        setContentView(R.layout.activity_main);

        setupNavigationUI();

        // subscribe to change of preferences to handle broker URI change
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);

        handler = new Handler();
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_fragment);
        return (navController.navigateUp() || super.onSupportNavigateUp());
    }

    // StartFragment.Listener implementation
    @Override
    public void onConnected(boolean already) {
        NavController controller = Navigation.findNavController(this, R.id.nav_fragment);
        // connected to broker, navigate to ship selection
        if (already == true) {
            // immediately if we are already connected
            controller.navigate(R.id.action_startFragment_to_shipSelectFragment);
        }
        else {
            // delayed navigation to allow splash screen to be seen
            handler.postDelayed(() -> {
                if (controller.getCurrentDestination().getId() == R.id.startFragment) {
                    controller.navigate(R.id.action_startFragment_to_shipSelectFragment);
                }
            }, 3000);
        }
    }

    @Override
    public void onSettings() {
        Navigation.findNavController(this, R.id.nav_fragment).
                navigate(R.id.action_startFragment_to_settingsFragment);
    }
    // end of StartFragment.Listener implementation

    // StartFragment.ConnectionSettingsProvider implementation
    @Override
    public String getBroker() {
        return PreferenceManager.getDefaultSharedPreferences(this)
                .getString(PREFS_BROKER_URI_KEY, "");
    }
    // end of StartFragment.ConnectionSettingsProvider implementation

    // ShipSelectFragment.OnListFragmentInteractionListener implementation
    @Override
    public void onListFragmentInteraction(String shipId) {
        // ship selected, connect to it
        viewModel.setCurrentShipId(shipId);

        // navigate to control fragment
        Navigation.findNavController(this, R.id.nav_fragment).
            navigate(R.id.action_shipSelectFragment_to_controlFragment);
    }
    // end of ShipSelectFragment.OnListFragmentInteractionListener implementation

    // ControlFragment.ControlSettingsProvider implementation
    public long getQueryTimeout() {
        long timeout = 1000;
        String strVal = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(PREFS_QUERY_TIMEOUT_KEY, "1000");
        try {
            timeout = Long.parseLong(strVal);
        }
        catch (NumberFormatException e) {
            Log.e(LOG_TAG, "Cannot parse timeout preference value: " + e.getMessage());
        }

        return timeout;
    }
    // end of ControlFragment.ControlSettingsProvider implementation

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (key.equals(PREFS_BROKER_URI_KEY)) {
            Log.i(LOG_TAG,"MainActivity received broker URI change notification");
            // disconnect from the old MQTT broker, start fragment will connect to the new one
            viewModel.disconnect();
        }
    }

    private void setupNavigationUI() {
        NavController navController = Navigation.findNavController(this, R.id.nav_fragment);
        appBarConfiguration = new AppBarConfiguration.Builder(R.id.startFragment, R.id.shipSelectFragment).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        navController.addOnDestinationChangedListener(
                (NavController controller, NavDestination destination, Bundle args) -> {
            if (destination.getId() == R.id.shipSelectFragment) {
                getSupportActionBar().setTitle(R.string.selectShip);
            }
            else if (destination.getId() == R.id.controlFragment) {
                getSupportActionBar().setTitle(viewModel.getCurrentShipId().getValue());
            }
            else if (destination.getId() == R.id.settingsFragment) {
                getSupportActionBar().setTitle(R.string.settingsTitle);
            }
        });
    }
}
