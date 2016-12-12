package com.example.bsanc.luxembourgishbuses;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.os.Build;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.BitmapDescriptor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.location.Location;
import android.util.Log;
import android.widget.TextView;
import android.graphics.drawable.VectorDrawable;

import android.support.v4.content.ContextCompat;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnCameraMoveStartedListener;
import com.google.android.gms.maps.GoogleMap.OnCameraMoveListener;
import com.google.android.gms.maps.GoogleMap.OnCameraIdleListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;

import java.io.InputStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONArray;
import org.json.JSONObject;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, ConnectionCallbacks, OnConnectionFailedListener {

    private GoogleMap mMap;
    private ArrayList<Marker> myMarkers = new ArrayList<Marker>();
    private static final int MY_PERMISSION_ACCESS_COARSE_LOCATION = 11;
    GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation;
    protected double mLatitudeText;
    protected double mLongitudeText;
    //Initialize to a non-valid zoom value
    private float previousZoomLevel = -1.0f;
    private boolean isZooming = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        // We are now connected!
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                mLatitudeText = mLastLocation.getLatitude();
                mLongitudeText = mLastLocation.getLongitude();
                LatLng user_location = new LatLng(mLatitudeText, mLongitudeText);
                //addBusStopMarker(49.603412, 6.118454);
                int zoomLevel = 16; //This goes up to 21
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(user_location, zoomLevel));
                mMap.setOnCameraChangeListener(getCameraChangeListener());
            }
            drawBusStations();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSION_ACCESS_COARSE_LOCATION);
        }
    }

    public OnCameraChangeListener getCameraChangeListener()
    {
        return new OnCameraChangeListener()
        {
            @Override
            public void onCameraChange(CameraPosition position)
            {
                if(previousZoomLevel != position.zoom)
                {
                    isZooming = true;
                    for (Marker marker : myMarkers) {
                        marker.setVisible(position.zoom > 15);
                    }
                }

                previousZoomLevel = position.zoom;
            }
        };
    }

    public void onCameraMove() {
        CameraPosition cameraPosition = mMap.getCameraPosition();
        for (Marker marker : myMarkers) {
            marker.setVisible(cameraPosition.zoom > 15);
        }
        System.out.println(cameraPosition.zoom);
    }


    private void drawBusStations() {
        ArrayList<Station> formList = getBusStationsList();
        for (int i = 0; i < formList.size(); i++) {
            addBusStopMarker(formList.get(i).name, formList.get(i).longitude, formList.get(i).latitude);
        }
    }

    private ArrayList<Station> getBusStationsList() {
        ArrayList<Station> formList = new ArrayList<Station>();
        try {
            JSONObject obj = new JSONObject(loadJSONFromAsset("stations"));
            JSONArray m_jArry = obj.getJSONArray("stations");

            for (int i = 0; i < m_jArry.length(); i++) {
                JSONObject jo_inside = m_jArry.getJSONObject(i);
                //Log.d("Details-->", jo_inside.getString("formule"));
                String station_name = jo_inside.getString("name");
                double longitude = Double.parseDouble(jo_inside.getString("longitude"));
                double latitude = Double.parseDouble(jo_inside.getString("latitude"));

                //Add your values in your `ArrayList` as below:
                formList.add(new Station(station_name, longitude, latitude));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return formList;
    }

    private void addBusStopMarker(String name, double latitude, double longitude) {
        LatLng location = new LatLng(longitude, latitude);
        myMarkers.add(mMap.addMarker(new MarkerOptions().position(location).title(name).icon(getBitmapDescriptor(R.drawable.ic_bus_station)).anchor(0.5f, 0.5f)));
    }

    private BitmapDescriptor getBitmapDescriptor(int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            VectorDrawable vectorDrawable = (VectorDrawable) getDrawable(id);

            int h = vectorDrawable.getIntrinsicHeight();
            int w = vectorDrawable.getIntrinsicWidth();

            vectorDrawable.setBounds(0, 0, w, h);

            Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bm);
            vectorDrawable.draw(canvas);

            return BitmapDescriptorFactory.fromBitmap(bm);

        } else {
            return BitmapDescriptorFactory.fromResource(id);
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // We are not connected anymore!
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // We tried to connect but failed!
    }


    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        getCurrentLocation(googleMap);

        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    public void getCurrentLocation(GoogleMap googleMap) {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSION_ACCESS_COARSE_LOCATION);
        }
    }

    public String loadJSONFromAsset(String file_name) {
        String json = null;
        try {
            InputStream is = getAssets().open(file_name);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

}
