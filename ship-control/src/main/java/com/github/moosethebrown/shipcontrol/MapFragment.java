package com.github.moosethebrown.shipcontrol;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.github.moosethebrown.shipcontrol.data.ShipViewModel;
import com.github.moosethebrown.shipcontrol.data.Waypoint;

import org.maplibre.android.camera.CameraUpdateFactory;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.Style;
import org.maplibre.android.style.expressions.Expression;
import org.maplibre.android.style.layers.Property;
import org.maplibre.android.style.layers.PropertyFactory;
import org.maplibre.android.style.layers.SymbolLayer;
import org.maplibre.android.style.sources.GeoJsonSource;
import org.maplibre.geojson.Feature;
import org.maplibre.geojson.Point;

public class MapFragment extends Fragment {
    private static final String LOG_TAG = "MapFragment";
    private static final String SHIP_POSITION_SOURCE = "ship-position-source";

    private MapView mapView = null;
    private MapLibreMap map = null;
    private Waypoint shipPosition = null;
    private double shipAngle = 0.0;
    private GeoJsonSource shipLocationSource = null;

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
            map.setStyle("https://tiles.openfreemap.org/styles/liberty", style -> {
                initShipLocationSource(style);
                addShipLocationLayer(style);
                updateShipPosition();
            });
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

        shipPosition = viewModel.getShipPosition().getValue();
        viewModel.getShipPosition().observe(this, newShipPosition -> {
            shipPosition = newShipPosition;
            updateShipPosition();
        });

        if (viewModel.getAngle().getValue() != null) {
            shipAngle = viewModel.getAngle().getValue();
        }
        viewModel.getAngle().observe(this, newAngle -> {
            shipAngle = newAngle;
            updateShipPosition();
        });

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

    private void initShipLocationSource(Style style) {
        shipLocationSource = new GeoJsonSource(SHIP_POSITION_SOURCE);
        style.addSource(shipLocationSource);
    }

    private void addShipLocationLayer(Style style) {
        style.addImage("ship-position-icon",
                BitmapFactory.decodeResource(getResources(), R.drawable.navigation));
        SymbolLayer shipLocationLayer = new SymbolLayer("ship-position-layer",
                SHIP_POSITION_SOURCE).withProperties(PropertyFactory.iconImage("ship-position-icon"),
                PropertyFactory.iconSize(0.05f),
                PropertyFactory.iconRotate(Expression.get("rotation")),
                PropertyFactory.iconAnchor(Property.ICON_ANCHOR_CENTER),
                PropertyFactory.iconAllowOverlap(true),
                PropertyFactory.iconIgnorePlacement(true));
        style.addLayer(shipLocationLayer);
    }

    private void updateShipPosition() {
        if (shipPosition == null) {
            Log.d(LOG_TAG, "updateShipPosition(), shipPosition = null");
            return;
        }
        if (map == null) {
            Log.d(LOG_TAG, "updateShipPosition(), map = null");
            return;
        }

        Log.d(LOG_TAG, "updateShipPosition(), latitude = " + shipPosition.getLatitude() +
                ", longitude = " + shipPosition.getLongitude());

        map.getStyle(style -> {
           Point point = Point.fromLngLat(shipPosition.getLongitude(), shipPosition.getLatitude());
           Feature feature = Feature.fromGeometry(point);

           feature.addNumberProperty("rotation", shipAngle);

           shipLocationSource = style.getSourceAs(SHIP_POSITION_SOURCE);
           if (shipLocationSource != null) {
               shipLocationSource.setGeoJson(feature);
           }

           map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(shipPosition.getLatitude(),
                   shipPosition.getLongitude()), 15));
        });
    }
}