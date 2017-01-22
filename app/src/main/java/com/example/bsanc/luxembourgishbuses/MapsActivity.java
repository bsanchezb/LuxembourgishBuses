package com.example.bsanc.luxembourgishbuses;

import android.app.Activity;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.os.Build;

import com.google.android.gms.maps.CameraUpdate;
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
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
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

import android.content.Intent;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, ConnectionCallbacks, OnConnectionFailedListener, OnMarkerClickListener {

    private GoogleMap mMap;
    private ArrayList<Station> myStations = new ArrayList<Station>();
    private ArrayList<Marker> myMarkers = new ArrayList<Marker>();
    private ArrayList<Marker> myVeloh = new ArrayList<Marker>();
    private static final int MY_PERMISSION_ACCESS_COARSE_LOCATION = 11;
    GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation;
    protected double mLatitudeText;
    protected double mLongitudeText;
    //Initialize to a non-valid zoom value
    private float previousZoomLevel = -1.0f;
    private boolean isZooming = false;
    public Marker currentMarker;
    public int currentMarkerType;
    private static final int ID_BUS_STATIONS = 0;
    private static final int ID_VELOH_STATIONS = 1;

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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * On selecting action bar icons
     * */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Take appropriate action for each action item click
        switch (item.getItemId()) {
            case R.id.closest_station:
                getClosestStation();
                return true;
            case R.id.exit:
                return true;
            default:
                return super.onOptionsItemSelected(item);
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
                mMap.setOnMarkerClickListener(this);
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
                if(position.zoom >= 15 && previousZoomLevel < 15 || position.zoom <= 15 && previousZoomLevel > 15)
                {
                    isZooming = true;
                    for (Marker marker : myMarkers) {
                        marker.setVisible(position.zoom > 15);
                    }
                    for (Marker marker : myVeloh) {
                        marker.setVisible(position.zoom > 15);
                    }
                }
                previousZoomLevel = position.zoom;
            }
        };
    }

    public void getClosestStation() {
        Location target = new Location("target");
        Marker best_station = myMarkers.get(0);
        double best_distance = -1;
        for (Marker marker : myMarkers) {
            target.setLatitude(marker.getPosition().latitude);
            target.setLongitude(marker.getPosition().longitude);
            if(best_distance > target.distanceTo(mLastLocation) || best_distance == -1) {
                best_distance = target.distanceTo(mLastLocation);
                best_station = marker;
                //System.out.println(best_distance);
            }
        }
        openStation(best_station);
    }

    private void drawBusStations() {
        ArrayList<Station> formList = getBusStationsList("stations");
        for (int i = 0; i < formList.size(); i++) {
            addBusStopMarker(formList.get(i), ID_BUS_STATIONS);
        }
        ArrayList<Station> formList_veloh = getBusStationsList("veloh.json");
        for (int i = 0; i < formList_veloh.size(); i++) {
            addBusStopMarker(formList_veloh.get(i), ID_VELOH_STATIONS);
        }
    }

    private void addBusStopMarker(Station station, int id) {
        LatLng location = new LatLng(station.latitude,  station.longitude);
        switch (id) {
            case ID_BUS_STATIONS:
                myMarkers.add(mMap.addMarker(new MarkerOptions().position(location).title(station.name).icon(getBitmapDescriptor(R.drawable.ic_bus_station)).anchor(0.5f, 0.5f)));
                break;
            case ID_VELOH_STATIONS:
                myVeloh.add(mMap.addMarker(new MarkerOptions().position(location).title(station.name).icon(getBitmapDescriptor(R.drawable.ic_veloh_station)).anchor(0.5f, 0.5f)));
            break;
        }
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

    private ArrayList<Station> getBusStationsList(String filename) {
        ArrayList<Station> formList = new ArrayList<Station>();
        try {
            JSONObject obj = new JSONObject(loadJSONFromAsset(filename));
            JSONArray m_jArry = obj.getJSONArray("stations");

            for (int i = 0; i < m_jArry.length(); i++) {
                JSONObject jo_inside = m_jArry.getJSONObject(i);
                //Log.d("Details-->", jo_inside.getString("formule"));
                String station_name = jo_inside.getString("name");
                double longitude = Double.parseDouble(jo_inside.getString("longitude"));
                double latitude = Double.parseDouble(jo_inside.getString("latitude"));

                //Add your values in your `ArrayList` as below:
                Station new_station = new Station(station_name, longitude, latitude);
                formList.add(new_station);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return formList;
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        openStation(marker);
        return true;
    }

    public void openStation(Marker marker) {
        currentMarker = marker;
        if (myMarkers.contains(currentMarker)) {
            currentMarker.setIcon(getBitmapDescriptor(R.drawable.ic_bus_station_active));
            currentMarkerType = ID_BUS_STATIONS;
        }
        else {
            currentMarker.setIcon(getBitmapDescriptor(R.drawable.ic_veloh_station_active));
            currentMarkerType = ID_VELOH_STATIONS;
        }
        if (myMarkers.contains(marker) || myVeloh.contains(marker))
        {
            LatLng position = marker.getPosition();
            mMap.moveCamera(CameraUpdateFactory.newLatLng(position));
            Intent intent = new Intent(MapsActivity.this, BusStopPop.class);
            intent.putExtra("latitude", position.latitude);
            intent.putExtra("longitude", position.longitude);
            intent.putExtra("name", marker.getTitle());
            if (myMarkers.contains(marker)) {
                intent.putExtra("type", ID_BUS_STATIONS);
            }
            else {
                intent.putExtra("type", ID_VELOH_STATIONS);
            }
            startActivityForResult(intent, 0);
            //startActivity(intent);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (currentMarkerType == ID_BUS_STATIONS) {
            currentMarker.setIcon(getBitmapDescriptor(R.drawable.ic_bus_station));
        }
        else {
            currentMarker.setIcon(getBitmapDescriptor(R.drawable.ic_veloh_station));
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
