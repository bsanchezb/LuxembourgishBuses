package com.example.bsanc.luxembourgishbuses;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Console;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.lang.Long;
import java.lang.Object;
import java.util.zip.Inflater;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * Created by bsanc on 11/01/2017.
 */


public class WebRequest extends AsyncTask<ArrayList<Bus>,Void,ArrayList<Bus>> {
    static String response = null;
    public final static int GETRequest = 1;
    public final static int POSTRequest = 2;
    public static String urladdress;
    public static ArrayList<Bus> busList;
    public static Activity mActivity;
    public static Context mContext;
    public  static LayoutInflater mInflater;

    private static final String TAG_BUS_INFO = "Departure";
    private static final String TAG_NAME = "name";
    private static final String TAG_DATE = "date";
    private static final String TAG_TIME = "time";
    private static final String TAG_RTDATE = "rtDate";
    private static final String TAG_RTTIME = "rtTime";
    private static final String TAG_DIRECTION = "direction";

    //Constructor with no parameter
    public WebRequest() {}

    public WebRequest(Activity mActivity) {
        this.mActivity = mActivity;
    }

    public WebRequest(String urladdress, Activity mActivity) {
        this.urladdress = urladdress;
        this.mActivity = mActivity;
    }

    public WebRequest(String urladdress, Activity mActivity, Context context, LayoutInflater inflater) {
        this.urladdress = urladdress;
        this.mActivity = mActivity;
        this.mContext = context;
        this.mInflater = inflater;
    }
    /**
     * Making web service call
     *
     * @url – url to make web request
     * @requestmethod – http request method
     */
    public String makeWebServiceCall(int requestmethod) {
        return this.makeWebServiceCall(requestmethod, null);
    }

    @Override
    protected ArrayList<Bus> doInBackground(ArrayList<Bus>... params) {
        String bus_station = makeWebServiceCall(GETRequest);
        //System.out.println(bus_station);
        this.urladdress = "http://travelplanner.mobiliteit.lu/restproxy/departureBoard?";
        HashMap<String, String> my_params = new HashMap<String, String>();
        my_params.put("accessId", "cdt");
        my_params.put("id", bus_station.replace("; ", "").replace("id=", "").replace(" ", "%20"));
        my_params.put("format", "json");
        String json = makeWebServiceCall(GETRequest, my_params);
        //System.out.println(json);
        busList = ParseJSON(json);
        return busList;
    }

    @Override
    protected void onPostExecute(ArrayList<Bus> requestresult) {
        if (requestresult != null) {
            super.onPostExecute(requestresult);
            ListView busesList = (ListView) this.mActivity.findViewById(R.id.listView_buses);
            // This is the array adapter, it takes the context of the activity as a
            // first parameter, the type of list view as a second parameter and your
            // array as a third parameter.
        /*
        ArrayList<String> bus_names = new ArrayList<String>();
        for (Bus bus : busList) {
            bus_names.add(bus.name);
        }
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this.mActivity,
                android.R.layout.simple_list_item_1,
                bus_names );

        busesList.setAdapter(arrayAdapter);
        */
            try {
                BusAdapter adapter = new BusAdapter(mContext, R.layout.layout_row_bus, requestresult);
                busesList.setAdapter(adapter);
            } catch (NullPointerException e) {
                View thisView = mInflater.inflate(R.layout.layout_row_bus, null);
                TextView error_text = (TextView) mActivity.findViewById(R.id.textView_ErrorText);
                error_text.setText("Sorry, we can not load data. Please check your Internet connection.");
                error_text.setVisibility(thisView.VISIBLE);
            }
        }
        else
        {
            View thisView = mInflater.inflate(R.layout.layout_row_bus, null);
            TextView error_text = (TextView) mActivity.findViewById(R.id.textView_ErrorText);
            error_text.setText("Sorry, we can not load data. Please check your Internet connection.");
            error_text.setVisibility(thisView.VISIBLE);
        }
    }

    public class BusAdapter extends ArrayAdapter {

        private ArrayList<Bus> busList;
        private int resource;

        public BusAdapter(Context context, int resource, ArrayList<Bus> objects) {
            super(context, resource, objects);
            busList = objects;
            this.resource = resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = mInflater.inflate(this.resource, null);
            }
            //System.out.println(busList.get(position).name);

            TextView bus_name = (TextView) convertView.findViewById(R.id.textView_busNumber);
            TextView bus_destination = (TextView) convertView.findViewById(R.id.textView_busDestination);
            TextView bus_time = (TextView) convertView.findViewById(R.id.textView_busTime);

            bus_name.setText(busList.get(position).name);
            bus_destination.setText(busList.get(position).direction);
            String new_time;
            if (busList.get(position).rtDatetime != null) {
                new_time = new SimpleDateFormat("kk:mm").format(busList.get(position).rtDatetime);
            } else {
                new_time = new SimpleDateFormat("kk:mm").format(busList.get(position).datetime);
            }
            bus_time.setText(new_time);

            return convertView;
        }
    }

    /**
     * Making web service call
     *
     * @url – url to make web request
     * @requestmethod – http request method
     * @params – http request params
     */
    public String makeWebServiceCall(int requestmethod,
                                     HashMap<String, String> params) {
        URL url;
        response = "";
        try {
            url = new URL(urladdress);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15001);
            conn.setConnectTimeout(15001);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            if (requestmethod == POSTRequest) {
                conn.setRequestMethod("POST");
            } else if (requestmethod == GETRequest) {
                conn.setRequestMethod("GET");
            }

            if (params != null) {
                OutputStream ostream = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(ostream, "UTF-8"));
                StringBuilder requestresult = new StringBuilder();
                boolean first = true;
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    if (first)
                        first = false;
                    else
                        requestresult.append("&");
                    requestresult.append(entry.getKey());
                    requestresult.append("=");
                    requestresult.append(entry.getValue());
                }
                writer.write(requestresult.toString());
                //System.out.println(url + requestresult.toString());

                writer.flush();
                writer.close();
                ostream.close();
            }
            int reqresponseCode = conn.getResponseCode();

            if (reqresponseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response += line;
                }
            } else {
                InputStream is = conn.getErrorStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                //System.out.println(br);
                //response = "";
                String nachricht;
                while ((nachricht = br.readLine()) != null){
                    response += nachricht;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }
    private ArrayList<Bus> ParseJSON(String json) {
        if (json != null) {
            ArrayList<Bus> busList = new ArrayList<Bus>();
            try {
                JSONObject jsonObj = new JSONObject(json);
                // Getting JSON Array node
                JSONArray buses = jsonObj.getJSONArray(TAG_BUS_INFO);

                // looping through All Students
                for (int i = 0; i < buses.length(); i++) {
                    Bus bus = new Bus();

                    JSONObject c = buses.getJSONObject(i);

                    bus.name = c.getString(TAG_NAME);
                    bus.direction = c.getString(TAG_DIRECTION);
                    try {
                        String datetime = c.getString(TAG_DATE) + " " + c.getString(TAG_TIME);
                        SimpleDateFormat dt = new SimpleDateFormat("yyyy-mm-dd kk:mm:ss");
                        bus.datetime = dt.parse(datetime);
                        try  {
                            String rtDatetime = c.getString(TAG_RTDATE) + " " + c.getString(TAG_RTTIME);
                            bus.rtDatetime = dt.parse(rtDatetime);
                        }
                        catch (JSONException e) {
                        }
                    }
                    catch (ParseException e) {
                    }

                    // adding student to students list
                    busList.add(bus);
                }
                return busList;
            } catch (JSONException e) {
                return null;
            }
        } else {
            Log.e("ServiceHandler", "No data received from HTTP Request");
            return null;
        }
    }
}