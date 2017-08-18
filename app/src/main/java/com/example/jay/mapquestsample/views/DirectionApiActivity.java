package com.example.jay.mapquestsample.views;

import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapquest.mapping.maps.MapboxMap;
import com.mapquest.mapping.maps.OnMapReadyCallback;
import com.mapquest.mapping.MapQuestAccountManager;
import com.mapquest.mapping.maps.MapView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import com.example.jay.mapquestsample.R;

public class DirectionApiActivity extends AppCompatActivity {

    private MapView mapView;
    private MapboxMap map;
    private static final String TAG = "DirectionApiActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapQuestAccountManager.start(getApplicationContext());
        setContentView(R.layout.activity_direction_api);
        mapView = (MapView) findViewById(R.id.mapquestMapView);
        mapView.onCreate(savedInstanceState);
        // Log.d(TAG, "here");

        Bundle bundle = getIntent().getExtras();
        final String source = bundle.getString("source");
        final String destination = bundle.getString("destination");
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                map = mapboxMap;
                Route route = new Route();
                route.execute(
                        source, // origin
                        destination, // destination
                        "pedestrian" // type
                );
            }
        });
    }

    private class Route extends AsyncTask <String, Void, String> {
        protected String doInBackground(String... args){
            JSONObject postData = new JSONObject();
            try {
                // JSONArray of start and finish
                JSONArray locations = new JSONArray();
                locations.put(URLEncoder.encode(args[0], "UTF-8"));
                locations.put(URLEncoder.encode(args[1], "UTF-8"));
                postData.put("locations", locations); // put array inside main object

                // JSONObject options
                JSONObject options = new JSONObject();
                options.put("routeType", args[2]);
                options.put("generalize", "0");
                postData.put("options", options);
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            // create the api request string
            String urlstring = "http://www.mapquestapi.com/directions/v2/route" +
                    "?key=kHnW9DZmvxqSwIGg9FZhUj3tm5kCbF69&json=" +
                    postData.toString();

            // make the GET request and prep the response string
            StringBuilder json = new StringBuilder();
            try {
                URL url = new URL(urlstring);
                HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader rd = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
                    String line;
                    while ((line = rd.readLine()) != null) {
                        json.append(line);
                    }
                } catch (Exception e) {
                    System.out.println("catch B: " + e.toString());
                } finally {
                    urlConn.disconnect();
                }
            } catch (Exception e) {
                System.out.println("catch C: " + e.toString());
            }
            return json.toString();
        }

        protected void onPostExecute(String json){
            try {
                // get shape points from response
                JSONArray points = new JSONObject(json)
                        .getJSONObject("route")
                        .getJSONObject("shape")
                        .getJSONArray("shapePoints");

                // get every other shape point
                int pointcount = points.length() / 2;

                // create a shape point list
                List<LatLng> shapePoints = new ArrayList<>();

                // fill list with every even value as lat and odd value as lng
                for (int point = 0; point < pointcount; point = point + 1) {
                    shapePoints.add(new LatLng(
                            (double) points.get(point * 2),
                            (double) points.get(point * 2 + 1)
                    ));
                }

                // create polyline options
                PolylineOptions polyline = new PolylineOptions();
                polyline.addAll(shapePoints)
                        .width(5)
                        .color(Color.BLUE)
                        .alpha((float)0.75);

                // add the polyline to the map
                map.addPolyline(polyline);

                // get map bounds
                JSONObject bounds = new JSONObject(json)
                        .getJSONObject("route")
                        .getJSONObject("boundingBox");

                // create bounds for animating map
                LatLngBounds latLngBounds = new LatLngBounds.Builder()
                        .include(new LatLng(
                                (double) bounds.getJSONObject("ul").get("lat"),
                                (double) bounds.getJSONObject("ul").get("lng")
                        ))
                        .include(new LatLng(
                                (double) bounds.getJSONObject("lr").get("lat"),
                                (double) bounds.getJSONObject("lr").get("lng")
                        ))
                        .build();

                // animate to map bounds
                map.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 50), 5000);
            } catch (Exception e) {
                System.out.println("catch D: " + e.toString());
            }
        }
    }

    @Override
    public void onResume()
    { super.onResume(); mapView.onResume(); }

    @Override
    public void onPause()
    { super.onPause(); mapView.onPause(); }

    @Override
    protected void onDestroy()
    { super.onDestroy(); mapView.onDestroy(); }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    { super.onSaveInstanceState(outState); mapView.onSaveInstanceState(outState); }

}
