package com.example.bsanc.luxembourgishbuses;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.VectorDrawable;
import android.os.Build;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.support.v4.content.ContextCompat;

/**
 * Created by bsanc on 11/12/2016.
 */

public class Station{
    String name;
    double longitude;
    double latitude;
    double distance;
    Marker marker;

    public Station() {

    }

    public Station(String name, double longitude, double latitude) {
        this.name = name;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public Station(String name, double distance){
        this.name=name;
        this.distance=distance;
    }
    @Override
    public  String toString(){
        return (this.name + " " + this.distance);
    }
}
