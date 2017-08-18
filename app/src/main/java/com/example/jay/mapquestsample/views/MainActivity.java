package com.example.jay.mapquestsample.views;

import com.example.jay.mapquestsample.R;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.constants.MyLocationTracking;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapquest.mapping.MapQuestAccountManager;
import com.mapquest.mapping.maps.OnMapReadyCallback;
import com.mapquest.mapping.maps.MapboxMap;
import com.mapquest.mapping.maps.MapView;


public class MainActivity extends Activity {

    private MapboxMap mMapboxMap;
    private MapView mMapView;
    private final LatLng SAN_FRAN = new LatLng(37.7749, -122.4194);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapQuestAccountManager.start(getApplicationContext());

        setContentView(R.layout.activity_main);

        mMapView = (MapView) findViewById(R.id.mapquestMapView);
        mMapView.onCreate(savedInstanceState);

        final EditText etSourcePoint = (EditText) findViewById(R.id.etSourcePoint);
        final EditText etDestinationPoint = (EditText) findViewById(R.id.etDestinationPoint);

        Button btnSearch = (Button) findViewById(R.id.btnSearch);

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                mMapboxMap = mapboxMap;
                // mMapView.setStyleUrl(Style.MAPQUEST_SATELLITE);
                // create and add marker
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(SAN_FRAN);
                markerOptions.title("San Francisco");
                markerOptions.snippet("Welcome to Frisco!");
                mMapboxMap.addMarker(markerOptions);

                // set map center and zoom
                mMapboxMap.moveCamera(CameraUpdateFactory.newLatLngZoom(SAN_FRAN, 14));

                // turn on traffic
                mMapboxMap.setTrafficFlowLayerOn();
                mMapboxMap.setTrafficIncidentLayerOn();

            }
        });

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), DirectionApiActivity.class);
                intent.putExtra("source",etSourcePoint.getText().toString());
                intent.putExtra("destination",etDestinationPoint.getText().toString());
                startActivity(intent);
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onDestroy()
    { super.onDestroy(); mMapView.onDestroy(); }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    { super.onSaveInstanceState(outState); mMapView.onSaveInstanceState(outState); }
}
