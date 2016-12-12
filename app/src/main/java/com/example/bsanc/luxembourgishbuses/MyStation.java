package com.example.bsanc.luxembourgishbuses;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by bsanc on 12/12/2016.
 */

public class MyStation implements ClusterItem {
    private final LatLng mPosition;

    public MyStation(double lat, double lng) {
        mPosition = new LatLng(lat, lng);
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }
}