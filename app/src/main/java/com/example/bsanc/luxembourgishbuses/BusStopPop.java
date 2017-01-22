package com.example.bsanc.luxembourgishbuses;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View;
import android.view.View.OnTouchListener;

import com.google.android.gms.maps.model.LatLng;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by bsanc on 17/12/2016.
 */
public class BusStopPop extends Activity {

    private static int station_type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_busstop_popup);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        double latitude = getIntent().getDoubleExtra("latitude", 0);
        double longitude = getIntent().getDoubleExtra("longitude", 0);
        TextView stationName = (TextView)findViewById(R.id.textView_station_name);
        stationName.setText(getIntent().getStringExtra("name"));

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = (int)(width*0.8);
        params.height = (int)(height*0.2);
        params.y = (int)(height);

        this.getWindow().setAttributes(params);

        station_type = getIntent().getIntExtra("type", 0);
        if (station_type == 0) {
            getBusStationsList(latitude, longitude);
        }
        else {
            TextView veloh_text = (TextView)findViewById(R.id.textView_ErrorText);
            veloh_text.setText("Veloh Station. There are bikes.");
            veloh_text.setVisibility(View.VISIBLE);
        }

        final LinearLayout main_layout = (LinearLayout)findViewById(R.id.main_layout);
        main_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WindowManager.LayoutParams params = getWindow().getAttributes();
                DisplayMetrics dm = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(dm);
                int height = dm.heightPixels;
                params.height = (int)(height*0.7);
                BusStopPop.this.getWindow().setAttributes(params);
            }
        });
    }

    private void getBusStationsList(double latitude, double longitude) {
        String url = "http://travelplanner.mobiliteit.lu/hafas/query.exe/dot?performLocating=2&tpl=stop2csv&stationProxy=yes&look_maxdist=1&look_x=" + String.format("%.6f", longitude).replace(".","") + "&look_y="  +  String.format("%.6f", latitude).replace(".","");
        WebRequest webrequest = new WebRequest(url, BusStopPop.this, getApplicationContext(), (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE));
        webrequest.execute();
        //System.out.println(webrequest.busList);
    }

    //@Override
    protected void onStop() {
        Intent resultIntent = new Intent();
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
        super.onStop();
    }
}
