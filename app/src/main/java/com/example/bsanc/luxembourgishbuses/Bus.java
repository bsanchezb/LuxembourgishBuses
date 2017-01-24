package com.example.bsanc.luxembourgishbuses;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by bsanc on 12/01/2017.
 */

public class Bus {
    String name;
    Date datetime;
    Date rtDatetime;
    String direction;
    String station;
    String user_station;

    public Bus() {};

    public Bus(String name, Date datetime, Date rtDatetime, String direction) {
        this.name = name;
        this.datetime = datetime;
        this.rtDatetime = rtDatetime;
        this.direction = direction;
    };

}
