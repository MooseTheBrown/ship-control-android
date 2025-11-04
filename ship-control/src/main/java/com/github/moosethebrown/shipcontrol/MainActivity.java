package com.github.moosethebrown.shipcontrol;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import com.github.moosethebrown.shipcontrol.data.ShipViewModel;

import org.maplibre.android.MapLibre;

public class MainActivity extends AppCompatActivity
    implements StartFragment.Listener,
               StartFragment.ConnectionSettingsProvider,
               ShipSelectFragment.OnListFragmentInteractionListener,
               SharedPreferences.OnSharedPreferenceChangeListener,
               ControlFragment.ControlSettingsProvider,
               ControlFragment.ControlFragmentListener,
               RestartFragment.Listener,
               VideoControlFragment.VideoSettingsProvider {

    public static final String LOG_TAG = "ship-control.MainActivity";
    private static final String PREFS_BROKER_URI_KEY = "brokerURI";
    private static final String PREFS_BROKER_USER_KEY = "brokerUsername";
    private static final String PREFS_BROKER_PASSWORD_KEY = "brokerPassword";
    private static final String PREFS_QUERY_TIMEOUT_KEY = "queryTimeout";
    private static final String PREFS_USE_TWO_JOYSTICKS_KEY = "controllerTwoJoysticks";
    private static final String PREFS_MAX_JOYSTICK_SPEED_KEY = "maxJoystickSpeed";
    private static final String PREFS_VIDEO_STREAM_URI_KEY = "videoStreamUri";

    private ShipViewModel viewModel = null;
    private Handler handler = null;
    private ControllerHandler controllerHandler = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MapLibre.getInstance(this);

        viewModel = ViewModelProviders.of(this).get(ShipViewModel.class);

        initControllerHandler();

        setContentView(R.layout.activity_main);

        setupNavigationUI();

        // subscribe to change of preferences to handle broker URI change
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);

        handler = new Handler();

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.d(LOG_TAG, "landscape orientation, hiding action bar");
            getSupportActionBar().hide();
        }
        else {
            Log.d(LOG_TAG, "portrait orientation, showing action bar");
            getSupportActionBar().show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return (navController.navigateUp() || super.onSupportNavigateUp());
    }

    // StartFragment.Listener implementation
    @Override
    public void onConnected(boolean already) {
        NavController controller = Navigation.findNavController(this, R.id.nav_host_fragment);

        initControllerHandler();

        // connected to broker, navigate to ship selection
        if (already) {
            // immediately if we are already connected
            controller.navigate(R.id.action_startFragment_to_shipSelectFragment);
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
    // end of StartFragment.Listener implementation

    // StartFragment.ConnectionSettingsProvider implementation
    @Override
    public String getBroker() {
        return PreferenceManager.getDefaultSharedPreferences(this)
                .getString(PREFS_BROKER_URI_KEY, "");
    }

    @Override
    public String getUsername() {
        return PreferenceManager.getDefaultSharedPreferences(this)
                .getString(PREFS_BROKER_USER_KEY, "");
    }

    @Override
    public String getPassword() {
        return PreferenceManager.getDefaultSharedPreferences(this)
                .getString(PREFS_BROKER_PASSWORD_KEY, "");
    }
    // end of StartFragment.ConnectionSettingsProvider implementation

    // ShipSelectFragment.OnListFragmentInteractionListener implementation
    @Override
    public void onListFragmentInteraction(String shipId) {
        // ship selected, connect to it
        viewModel.setCurrentShipId(shipId);

        // navigate to control fragment
        Navigation.findNavController(this, R.id.nav_host_fragment).
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

    // ControlFragment.ControlFragmentListener implementation
    @Override
    public void onVideoButtonClicked() {
        // navigate to video control fragment
        Navigation.findNavController(this, R.id.nav_host_fragment).
                navigate(R.id.action_controlFragment_to_videoControlFragment);
    }

    @Override
    public void onMapButtonClicked() {
        // navigate to map fragment
        Navigation.findNavController(this, R.id.nav_host_fragment).
                navigate(R.id.action_controlFragment_to_mapFragment);
    }
    // end of ControlFragment.ControlFragmentListener implementation

    // VideoControlFragment.VideoSettingsProvider implementation
    @Override
    public String getVideoStreamUri() {
        return PreferenceManager.getDefaultSharedPreferences(this)
                .getString(PREFS_VIDEO_STREAM_URI_KEY, "");
    }
    // end of VideoControlFragment.VideoSettingsProvider implementation

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if ((key.equals(PREFS_BROKER_URI_KEY)) ||
            (key.equals(PREFS_BROKER_USER_KEY)) ||
            (key.equals(PREFS_BROKER_PASSWORD_KEY))) {
            Log.i(LOG_TAG,"MainActivity received broker URI change notification");
            new RestartFragment().show(getSupportFragmentManager(), "restartDialog");
        }
        else if (key.equals(PREFS_USE_TWO_JOYSTICKS_KEY)) {
            boolean useTwoJoysticks = prefs.getBoolean(PREFS_USE_TWO_JOYSTICKS_KEY, true);
            Log.d(LOG_TAG, "useTwoJoysticks changed, reinitializing controllerHandler, new value: " + useTwoJoysticks);
            controllerHandler = new ControllerHandler(viewModel.getShipControl(), useTwoJoysticks,
                    prefs.getInt(PREFS_MAX_JOYSTICK_SPEED_KEY, 10));
        }
        else if (key.equals(PREFS_MAX_JOYSTICK_SPEED_KEY)) {
            int maxJoystickSpeed = prefs.getInt(PREFS_MAX_JOYSTICK_SPEED_KEY, 10);
            Log.d(LOG_TAG, "maxJoystickSpeed changed, reinitializing controllerHandler, new value: " + maxJoystickSpeed);
            controllerHandler = new ControllerHandler(viewModel.getShipControl(),
                    prefs.getBoolean(PREFS_USE_TWO_JOYSTICKS_KEY, true), maxJoystickSpeed);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.settingsMenuItem) {
            Navigation.findNavController(this, R.id.nav_host_fragment).
                    navigate(R.id.settingsFragment);
        }

        return super.onOptionsItemSelected(item);
    }

    // RestartFragment.Listener implementation
    @Override
    public void onRestartNow() {
        Log.i(LOG_TAG, "MainActivity: restarting application");
        Intent restartIntent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        PendingIntent intent = PendingIntent.getActivity(this, 0,
                restartIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmMgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, intent);
        System.exit(0);
    }
    // end of RestartFragment.Listener implementation

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        Log.d(LOG_TAG, "dispatchGenericMotionEvent, controllerHandler=" + controllerHandler);
        if ((controllerHandler != null) && (controllerHandler.handleMotionEvent(event))) {
            return true;
        }
        else {
            return super.dispatchGenericMotionEvent(event);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Log.d(LOG_TAG, "dispatchKeyEvent, controllerHandler=" + controllerHandler);
        if ((controllerHandler != null) && (controllerHandler.handleKeyEvent(event))) {
            return true;
        }
        else {
            return super.dispatchKeyEvent(event);
        }
    }

    private void setupNavigationUI() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        final Toolbar toolbar = findViewById(R.id.app_toolbar);
        setSupportActionBar(toolbar);
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
        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);
    }

    private void initControllerHandler() {
        if (viewModel == null) {
            Log.i(LOG_TAG, "initControllerHandler, viewModel is null");
            return;
        }
        if (controllerHandler != null) {
            Log.i(LOG_TAG, "initControllerHandler, controllerHandler is not null");
            return;
        }
        if (Boolean.FALSE.equals(viewModel.getConnected().getValue())) {
            Log.i(LOG_TAG, "initControllerHandler, not connected to broker");
            return;
        }

        boolean useTwoJoysticks = PreferenceManager.getDefaultSharedPreferences(this).
                getBoolean(PREFS_USE_TWO_JOYSTICKS_KEY, true);
        int maxJoystickSpeed = PreferenceManager.getDefaultSharedPreferences(this).
                getInt(PREFS_MAX_JOYSTICK_SPEED_KEY, 10);
        Log.i(LOG_TAG, "initializing controller handler, useTwoJoysticks = " + useTwoJoysticks +
                ", maxJoystickSpeed = " + maxJoystickSpeed);
        controllerHandler = new ControllerHandler(viewModel.getShipControl(), useTwoJoysticks, maxJoystickSpeed);
    }
}
