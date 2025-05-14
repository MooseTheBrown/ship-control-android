package com.github.moosethebrown.shipcontrol;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.github.moosethebrown.shipcontrol.data.ShipViewModel;
import com.github.moosethebrown.shipcontrol.data.Waypoint;

import org.maplibre.android.camera.CameraPosition;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.MapView;

public class MapFragment extends Fragment {
    private static final String LOG_TAG = "MapFragment";

    private MapView mapView = null;
    private MapLibreMap map = null;
    private Waypoint shipPosition = null;
    private boolean cameraSet = false;

    public MapFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        mapView = view.findViewById(R.id.mapView);
        mapView.getMapAsync(map -> {
            this.map = map;
            map.setStyle("https://tiles.openfreemap.org/styles/liberty");
            updateCameraPosition();
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();

        ShipViewModel viewModel = ViewModelProviders.of(getActivity()).get(ShipViewModel.class);

        setShipPosition(viewModel.getShipPosition().getValue());
        viewModel.getShipPosition().observe(this, this::setShipPosition);

        Log.d(LOG_TAG, "onStart()");
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onPause();

        Log.d(LOG_TAG, "onStop()");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    private void updateCameraPosition() {
        if ((map == null) || (shipPosition == null)) {
            return;
        }
        Log.d(LOG_TAG, "updatePosition(), latitude = " +
                shipPosition.getLatitude() + "; longitude = " +
                shipPosition.getLongitude());
        map.setCameraPosition(new CameraPosition.Builder().
                target(new LatLng(shipPosition.getLatitude(), shipPosition.getLongitude())).
                zoom(12).build());
    }

    private void setShipPosition(Waypoint newShipPosition) {
        if (newShipPosition == null) {
            return;
        }
        shipPosition = newShipPosition;
        if (!cameraSet) {
            updateCameraPosition();
            cameraSet = true;
        }
    }
}