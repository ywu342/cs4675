package com.example.yalingwu.internetservices;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MainActivity extends AppCompatActivity {

    final Context context = this;
    AutoCompleteTextView startLocTxt;
    AutoCompleteTextView endLocTxt;
    AutoCompleteTextView curTxt;
    EditText mileRangeTxt;
    final String SERVER = "http://128.61.64.156:8080/RouteService/Route/getRoute?";

    List<LatLng> stations_coord;
    List<String> prices_list;

    PlacesTask placesTask_start;
    PlacesTask placesTask_end;
    ParserTask parserTask;
    GasStationTask gasStationTask;
    GSParserTask gsParserTask;

    LatLng[] scArr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        long time= System.currentTimeMillis();
        android.util.Log.i("Time Class ", " At start of the first screen: Time value in milliseconds " + time);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        startLocTxt = (AutoCompleteTextView) findViewById(R.id.startLocTxt);
        endLocTxt = (AutoCompleteTextView) findViewById(R.id.endLocTxt);
        startLocTxt.setThreshold(1);
        endLocTxt.setThreshold(1);
        startLocTxt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                startLocTxt.showDropDown();
                return false;
            }
        });
        endLocTxt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                endLocTxt.showDropDown();
                return false;
            }
        });

        startLocTxt.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                curTxt = startLocTxt;
                placesTask_start = new PlacesTask();
                placesTask_start.execute(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        endLocTxt.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                curTxt = endLocTxt;
                placesTask_end = new PlacesTask();
                placesTask_end.execute(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mileRangeTxt = (EditText) findViewById(R.id.mileRangeTxt);
        Button submitBtn = (Button) findViewById(R.id.submitBtn);
        submitBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                long time= System.currentTimeMillis();
                android.util.Log.i("Time Class ", " When user clicks submit button: Time value in milliseconds "+time);
                String slt = startLocTxt.getText().toString();
                String elt = endLocTxt.getText().toString();
                String mrt = mileRangeTxt.getText().toString();
                if(slt.equals("")||elt.equals("")||mrt.equals("")){
                    //TODO: give a popup warning to complete all the fields
                    // custom dialog
                    final Dialog dialog = new Dialog(context);
                    dialog.setContentView(R.layout.not_complete_dial);
                    dialog.setTitle("Forgot something...");
                    Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
                    // if button is clicked, close the custom dialog
                    dialogButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                }
                else {
                    //float mileRange = Float.parseFloat(mrt);

                    String url = "";

                    //Using Async Task to query the server
                    gasStationTask = new GasStationTask();
                    gasStationTask.execute(slt,elt, mrt);

                    /*for TEST purposes, uncomment the above two lines when demo*/
//                    Intent goToResults = new Intent(MainActivity.this, RoutesDisplay.class);
//                    goToResults.putExtra("SOURCE_ADDR",startLocTxt.getText().toString());
//                    goToResults.putExtra("DEST_ADDR",endLocTxt.getText().toString());
//                    scArr = new LatLng[1];
//                    scArr[0] = new LatLng(33.771032, -84.389376);
//                    goToResults.putExtra("STATIONS_COORD", scArr);
//                    //goToResults.putExtra("PRICES_LIST", strArr);
//                    startActivity(goToResults);

                }
            }
        });
    }

    /** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);
            long time= System.currentTimeMillis();
//            android.util.Log.i("Time Class ", "When URL connection starts: Time value in milliseconds " + time);
            urlConnection = (HttpURLConnection)url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb  = new StringBuffer();
            String line = "";
            while((line = br.readLine())!= null){
                sb.append(line);
            }
            data = sb.toString();
            br.close();
        }catch(Exception e){
            Log.d("Exception while downloading url", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
            long time= System.currentTimeMillis();
//            android.util.Log.i("Time Class ", "When URL connection closes: Time value in milliseconds " + time);
        }
        return data;
    }

    // Fetches all places from GooglePlaces AutoComplete Web Service
    private class PlacesTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... place) {
            String data = "";
            // Obtain browser key from https://code.google.com/apis/console
            String key = "key=AIzaSyBbU5w64F7Rl2Dr_JPAWcw1tTr0WkROPHY";
            String input="";
            try {
                input = "input=" + URLEncoder.encode(place[0], "utf-8");
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }
            String types = "types=geocode";
            String parameters = input+"&"+types+"&"+key;
            String output = "json";
            String url = "https://maps.googleapis.com/maps/api/place/autocomplete/"+output+"?"+parameters;
            try{
                data = downloadUrl(url);
            }catch(Exception e){
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            parserTask = new ParserTask();
            parserTask.execute(result);
        }
    }

    /** A class to parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String,String>>>{

        JSONObject jObject;

        @Override
        protected List<HashMap<String, String>> doInBackground(String... jsonData) {
            List<HashMap<String, String>> places = null;
            PlaceJSONParser placeJsonParser = new PlaceJSONParser();
            try{
                jObject = new JSONObject(jsonData[0]);
                places = placeJsonParser.parse(jObject);
            }catch(Exception e){
                Log.d("Exception",e.toString());
            }
            return places;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, String>> result) {
            String[] from = new String[] {"description"};
            int[] to = new int[] {android.R.id.text1};
            SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), result, android.R.layout.simple_list_item_1, from, to);
            curTxt.setAdapter(adapter);
        }
    }


    // Fetches data from our server
    private class GasStationTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... address) {
            String data = "";
            String src="", dest="", miles = "";
            try {
                System.out.println("getting src and dest");
                src = "origin_address=" + URLEncoder.encode(address[0], "utf-8");
                dest= "destination_address=" + URLEncoder.encode(address[1], "utf-8");
                miles = "miles=" + URLEncoder.encode(address[2], "utf-8");
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }
            String url = SERVER + src + "&" + dest + "&" + miles;
            try{
                data = downloadUrl(url);
//                System.out.println(data);
            }catch(Exception e){
                Log.d("Gas station task Background Task", e.toString());
            }
//            System.out.println("returning data");
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
//            System.out.println("is before super.onpost happening?");
            super.onPostExecute(result);
//            System.out.println("is onpost for gasStnTask happening?");
//            System.out.println("==============================================\ndata from our server: " + result + "\n==============================================");
            gsParserTask = new GSParserTask();
            gsParserTask.execute(result);
        }
    }

    /** A class to parse the Gas stations in JSON format */
    private class GSParserTask extends AsyncTask<String, Integer, List<HashMap<String,String>>>{

        JSONArray jsonArray;

        @Override
        protected List<HashMap<String, String>> doInBackground(String... jsonData) {
            List<HashMap<String, String>> stations = null;
            GSJSONParser gsJsonParser = new GSJSONParser();
            try{
//                System.out.println("parsing SERVER json array");
//                System.out.println(jsonData[0]);
                jsonArray = new JSONArray(jsonData[0]);
                stations = gsJsonParser.parse(jsonArray);
            }catch(Exception e){
                Log.d("Exception",e.toString());
            }
//            System.out.println("returning stations list");
            return stations;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, String>> result) {
            if(result == null || result.isEmpty()) {
//                System.out.println("NO RESULTS OF STATIONS FOUND FOR THE ROUTE");
//                final Dialog dialog = new Dialog(context);
//                dialog.setContentView(R.layout.no_results);
//                dialog.setTitle("Forgot something...");
//                Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
//                // if button is clicked, close the custom dialog
//                dialogButton.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        dialog.dismiss();
//                    }
//                });
//                dialog.show();
//                return;
                Intent goToResults = new Intent(MainActivity.this, RoutesDisplay.class);
                goToResults.putExtra("SOURCE_ADDR",startLocTxt.getText().toString());
                goToResults.putExtra("DEST_ADDR",endLocTxt.getText().toString());
                scArr = new LatLng[1];
                scArr[0] = new LatLng(33.771032, -84.389376);
                goToResults.putExtra("STATIONS_COORD", scArr);
                //goToResults.putExtra("PRICES_LIST", strArr);
                startActivity(goToResults);
            } else {
//                System.out.println("creating stations coord");
                stations_coord = new ArrayList<LatLng>();
                for (int i = 0; i < result.size(); i++) {
                    HashMap<String, String> station = result.get(i);
                    double lat = Double.parseDouble(station.get("lat"));
                    double lng = Double.parseDouble(station.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    stations_coord.add(position);
                    //prices_list.add(station.get("price"));
                }
                scArr = new LatLng[stations_coord.size()];
                for (int i = 0; i < stations_coord.size(); i++) {
                    scArr[i] = stations_coord.get(i);
                }
                Intent goToResults = new Intent(MainActivity.this, RoutesDisplay.class);
                goToResults.putExtra("SOURCE_ADDR", startLocTxt.getText().toString());
                goToResults.putExtra("DEST_ADDR", endLocTxt.getText().toString());
                //scArr = new LatLng[1];
                //scArr[0] = new LatLng(33.771032, -84.389376);
                goToResults.putExtra("STATIONS_COORD", scArr);
                //goToResults.putExtra("PRICES_LIST", strArr);
                startActivity(goToResults);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

