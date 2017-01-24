package com.example.bsanc.luxembourgishbuses;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * Created by bsanc on 24/01/2017.
 */

public class QueryHistory extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_busstop_popup);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = (int)(width*0.8);
        params.height = (int)(height*0.7);
        params.y = (int)(height);

        this.getWindow().setAttributes(params);

        String query = getQueriesList();
        String[] queries = query.split("\n");

        String reverse = "";
        for (int i= queries.length - 1; i >= 0; i--) {
            reverse += queries[i] + "\n";
        }

        TextView header = (TextView)findViewById(R.id.textView_station_name);
        header.setText("Query history");

        TextView queries_text = (TextView)findViewById(R.id.textView_ErrorText);
        queries_text.setText(reverse);
        queries_text.setVisibility(View.VISIBLE);
        queries_text.setMovementMethod(new ScrollingMovementMethod());
    }

    private String getQueriesList() {
        try {
            FileInputStream fileIn = openFileInput("query_history");
            InputStreamReader InputRead = new InputStreamReader(fileIn);

            char[] inputBuffer = new char[100];
            String s="";
            int charRead;

            while ((charRead=InputRead.read(inputBuffer))>0) {
                // char to string conversion
                String readstring = String.copyValueOf(inputBuffer,0,charRead);
                s += readstring;
            }
            InputRead.close();
            //s = s.replace(".!", "\n");
            return s;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
