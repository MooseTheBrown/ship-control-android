package com.github.moosethebrown.shipcontrol;

import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.github.moosethebrown.shipcontrol.data.ShipViewModel;

public class MainActivity extends AppCompatActivity
    implements StartFragment.OnConnectedListener,
               StartFragment.ConnectionSettingsProvider,
               ShipSelectFragment.OnListFragmentInteractionListener {

    public static final String LOG_TAG = "ship-control";

    private ShipViewModel viewModel = null;
    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = ViewModelProviders.of(this).get(ShipViewModel.class);

        setContentView(R.layout.activity_main);

        setupNavigationUI();
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_fragment);
        return (navController.navigateUp() || super.onSupportNavigateUp());
    }

    // StartFragment.OnConnectedListener implementation
    @Override
    public void onConnected(boolean already) {
        // connected to broker, navigate to ship selection
        if (already == true) {
            // immediately if we are already connected
            Navigation.findNavController(this, R.id.nav_fragment).
                    navigate(R.id.action_startFragment_to_shipSelectFragment);
        } else {
            // delayed navigation to allow splash screen to be seen
            new Handler().postDelayed(() -> {
                Navigation.findNavController(this, R.id.nav_fragment).
                        navigate(R.id.action_startFragment_to_shipSelectFragment);
            }, 1000);
        }
    }
    // end of StartFragment.OnConnectedListener implementation

    // StartFragment.ConnectionSettingsProvider implementation
    @Override
    public String getBroker() {
        return "ssl://wsmnn-291:8883";
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
