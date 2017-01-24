package com.example.bsanc.luxembourgishbuses;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

/**
 * Created by bsanc on 22/01/2017.
 */

public class RouteFinding extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = (int)(width*0.8);
        params.height = (int)(height*0.2);
        params.y = (int)(height);

        final double point_latitude = getIntent().getDoubleExtra("point_latitude", 0);
        final double point_longitude = getIntent().getDoubleExtra("point_longitude", 0);
        final double user_latitude = getIntent().getDoubleExtra("user_latitude", 0);
        final double user_longitude = getIntent().getDoubleExtra("user_longitude", 0);

        this.getWindow().setAttributes(params);

        final LinearLayout main_layout = (LinearLayout)findViewById(R.id.layout_routeFinding);
        main_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WindowManager.LayoutParams params = getWindow().getAttributes();
                DisplayMetrics dm = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(dm);
                int height = dm.heightPixels;
                params.height = (int)(height*0.7);
                RouteFinding.this.getWindow().setAttributes(params);

                getAsyncRequest(point_latitude, point_longitude, user_latitude, user_longitude);
            }
        });

    }

    private void getAsyncRequest(double point_latitude, double point_longitude, double user_latitude,  double user_longitude) {
        AsyncRouteFindingRequest asyncRequest = new AsyncRouteFindingRequest(point_latitude, point_longitude, user_latitude, user_longitude, RouteFinding.this, getApplicationContext(), (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE));
        asyncRequest.execute();
        //System.out.println(webrequest.busList);
    }
}
