package com.example.yalingwu.internetservices;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by yalingwu on 4/21/16.
 */
public class GSJSONParser {
    /** Receives a JSONArray and returns a list of lists containing latitude, price and longitude */
    public List<HashMap<String,String>> parse(JSONArray jArray){

        List<HashMap<String, String>> stations = new ArrayList<HashMap<String,String>>() ;
        try {
            for(int i=0;i<jArray.length();i++) {
                HashMap<String, String> hm = new HashMap<String, String>();
                hm.put("lat", Double.toString(((JSONObject) jArray.get(i)).getDouble("lat")));
                hm.put("lng", Double.toString(((JSONObject) jArray.get(i)).getDouble("lng")));
                hm.put("price", Double.toString(((JSONObject) jArray.get(i)).getDouble("price")));
                stations.add(hm);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }catch (Exception e){
        }
        return stations;
    }
}
