package com.example.bsanc.luxembourgishbuses;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.Inflater;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by bsanc on 22/01/2017.
 */

public class AsyncRouteFindingRequest extends AsyncTask<Bus,Void,Bus> {
    static String response = null;
    public static String urladdress;
    public double point_latitude;
    public double point_longitude;
    public double user_latitude;
    public double user_longitude;

    private static final String TAG_BUS_INFO = "Departure";
    private static final String TAG_NAME = "name";
    private static final String TAG_DATE = "date";
    private static final String TAG_TIME = "time";
    private static final String TAG_RTDATE = "rtDate";
    private static final String TAG_RTTIME = "rtTime";
    private static final String TAG_DIRECTION = "direction";

    public final static int GETRequest = 1;
    public final static int POSTRequest = 2;

    public static Activity mActivity;
    public static Context mContext;
    public  static LayoutInflater mInflater;

    public AsyncRouteFindingRequest() {}

    public AsyncRouteFindingRequest(double latitude, double longitude) {
        this.point_latitude = latitude;
        this.point_longitude = longitude;
    }

    public AsyncRouteFindingRequest(double latitude, double longitude, Activity activity, Context context, LayoutInflater inflater) {
        this.point_latitude = latitude;
        this.point_longitude = longitude;
        mActivity = activity;
        mContext = context;
        mInflater = inflater;
    }

    public AsyncRouteFindingRequest(double point_latitude, double point_longitude, double user_latitude, double user_longitude, Activity activity, Context context, LayoutInflater inflater) {
        this.point_latitude = point_latitude;
        this.point_longitude = point_longitude;
        this.user_latitude = user_latitude;
        this.user_longitude = user_longitude;
        mActivity = activity;
        mContext = context;
        mInflater = inflater;
    }

    private final Handler mHandler = new Handler();

    private final Runnable mUpdateUI = new Runnable() {
        int i = 0;
        public void run() {
            displayData(i);
            mHandler.postDelayed(mUpdateUI, 300); // 0.3 second
            i = (i+1) % 4;
        }
    };

    private void displayData(int i) {
        TextView start_station = (TextView) mActivity.findViewById(R.id.textView_startStation);
        switch (i) {
            case 0:
                start_station.setText("Please, wait. Loading");
                break;
            case 1:
                start_station.setText("Please, wait. Loading.");
                break;
            case 2:
                start_station.setText("Please, wait. Loading..");
                break;
            default:
                start_station.setText("Please, wait. Loading...");
                break;
        }
    }

    @Override
    protected void onPreExecute(){
        super.onPreExecute();
        //TextView start_station = (TextView) mActivity.findViewById(R.id.textView_startStation);
        //start_station.setText("Loading... Please, wait.");
        mHandler.post(mUpdateUI);
    }

    @Override
    protected Bus doInBackground(Bus... params) {
        List<Bus> near_buses = new ArrayList<Bus>();
        near_buses = getBusesAround(this.point_latitude, this.point_longitude, 350);
        Bus finding_bus = new Bus();
        if (near_buses != null) {
            finding_bus = findBusesNear(near_buses);
        }
        //System.out.println(finding_bus.station);
        //System.out.println(finding_bus.user_station);
        return finding_bus;
    }

    @Override
    protected void onPostExecute(Bus requestresult) {
        if (requestresult != null) {
            super.onPostExecute(requestresult);
            mHandler.removeCallbacks(mUpdateUI);
            TextView start_station = (TextView) mActivity.findViewById(R.id.textView_startStation);
            TextView finish_station = (TextView) mActivity.findViewById(R.id.textView_finishStation);
            TextView bus_number = (TextView) mActivity.findViewById(R.id.textView_busNumber);
            TextView bus_destination = (TextView) mActivity.findViewById(R.id.textView_busDestination);
            TextView bus_time = (TextView) mActivity.findViewById(R.id.textView_busTime);
            start_station.setText("From: " + requestresult.station.replace("id=A=1@O=", "").split("@")[0]);
            finish_station.setText("To: " + requestresult.user_station.replace("id=A=1@O=", "").split("@")[0]);
            bus_number.setText(requestresult.name);
            bus_destination.setText(requestresult.direction);
            String new_time;
            if (requestresult.rtDatetime != null) {
                new_time = new SimpleDateFormat("kk:mm").format(requestresult.rtDatetime);
            } else {
                new_time = new SimpleDateFormat("kk:mm").format(requestresult.datetime);
            }
            bus_time.setText(new_time);
        }
    }

    private List<Bus> getBusesAround(double latitude, double longitude, int distance) {
        List<Bus> near_buses = new ArrayList<Bus>();
        this.urladdress = "http://travelplanner.mobiliteit.lu/hafas/query.exe/dot?performLocating=2&tpl=stop2csv&stationProxy=yes&look_maxdist=" + distance + "&look_x=" + String.format("%.6f", longitude).replace(".","") + "&look_y="  +  String.format("%.6f", latitude).replace(".","");
        //System.out.println(this.urladdress);
        String closest_stations = makeWebServiceCall(GETRequest); // list of stations
        //System.out.println(closest_stations);
        String[] stations = closest_stations.split("; ");
        for(String station : stations) {
            this.urladdress = "http://travelplanner.mobiliteit.lu/restproxy/departureBoard?";
            HashMap<String, String> my_params = new HashMap<String, String>();
            my_params.put("accessId", "cdt");
            my_params.put("id", station.replace("; ", "").replace("id=", "").replace(" ", "%20"));
            my_params.put("format", "json");
            String json = makeWebServiceCall(GETRequest, my_params);
            List<Bus> buses = ParseJSON(json);
            if(buses != null) {
                for (Bus bus : buses) {
                    bus.station = station;
                    near_buses.add(bus);
                    //System.out.println(bus.name + ": " + bus.station);
                }
            }
        }
        return near_buses;
    }

    private Bus findBusesNear(List<Bus> busesAroundPoint) {
        Bus findingBus = null;
        int distance = 50;
        List<Bus> userCloseBuses = new ArrayList<Bus>();
        while (findingBus == null) {
            distance += distance;
            //System.out.println(distance);
            userCloseBuses = getBusesAround(this.user_latitude, this.user_longitude, distance);
            for (Bus point_bus : busesAroundPoint) {
                //System.out.println("Current bus: " + point_bus.name);
                for(Bus user_bus: userCloseBuses) {
                    try {
                        int value_user = Integer.parseInt(user_bus.name.replaceAll("[^\\d.]", ""));
                        int value_point = Integer.parseInt(point_bus.name.replaceAll("[^\\d.]", ""));
                        String direction_user = user_bus.direction.split(",")[0];
                        String direction_point = point_bus.direction.split(",")[0];
                        //System.out.println(value_point + " : " + value_user);

                        if (value_point == value_user && direction_user.compareTo(direction_point) == 0 && findingBus == null) {
                            //System.out.println("Equal");
                            findingBus = user_bus;
                            findingBus.user_station = point_bus.station;
                            break;
                        }
                    }
                    catch (Exception e) {}
                }
            }
        }
        return findingBus;
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
