package com.example.yalingwu.internetservices;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
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
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    final Context context = this;
    AutoCompleteTextView startLocTxt;
    AutoCompleteTextView endLocTxt;
    AutoCompleteTextView curTxt;
    EditText mileRangeTxt;
    String SERVER = "http://128.61.76.21:8080/RouteService/Route/getRoute?";

    PlacesTask placesTask_start;
    PlacesTask placesTask_end;
    ParserTask parserTask;
    GasStationTask gasStationTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

                String slt = startLocTxt.getText().toString();
                String elt = endLocTxt.getText().toString();
                String mrt = mileRangeTxt.getText().toString();
//                System.out.println("start: "+slt+",end: "+elt+",milerange: "+mrt);
                if(slt.equals("")||elt.equals("")||mrt.equals("")){
                    //TODO: give a popup warning to complete all the fields
                    // custom dialog
                    final Dialog dialog = new Dialog(context);
                    dialog.setContentView(R.layout.not_complete_dial);
                    dialog.setTitle("Forgot something...");

                    // set the custom dialog components - text and button
//                    TextView text = (TextView) dialog.findViewById(R.id.dialbody);
//                    text.setText("Please complete all the fields!");
//                    ImageView image = (ImageView) dialog.findViewById(R.id.image);
//                    image.setImageResource(R.drawable.ic_launcher);

                    Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
                    // if button is clicked, close the custom dialog
                    dialogButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
//                    System.out.println("Please complete all the fields");
                }
                else {
                    float mileRange = Float.parseFloat(mrt);

//                    RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
//                    String charset = "UTF-8";
////                    String src = "3505 Wennington Trace, Alpharetta, GA 30004";
////                    String dest = "733 Techwood Drive Northwest, Atlanta, GA 30332";
//                    String src = slt;
//                    String dest = elt;
//                    String url = "";
//                    try {
//                        String query = String.format("origin_address=%s&destination_address=%s",
//                                URLEncoder.encode(src, charset),
//                                URLEncoder.encode(dest, charset));
//                        url = SERVER + query;
//                        System.out.println("Successfully composed url");
//                    } catch (Exception e) {
//                        System.out.println(e.getMessage());
//                    }

                    // Request a string response from the provided URL.
//                    StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
//                            new Response.Listener<String>() {
//                                @Override
//                                public void onResponse(String response) {
//                                    System.out.println(response);
//                                    System.out.println("Naveena");
//                                }
//                            }, new Response.ErrorListener() {
//                        @Override
//                        public void onErrorResponse(VolleyError error) {
//                            System.out.println(error);
//                            System.out.println("That didn't work!");
//                        }
//                    });
//                    System.out.println("requesting: ======================================");
//                    JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,url,null,new Response.Listener<JSONObject>() {
//                        @Override
//                        public void onResponse(JSONObject response) {
//                            Log.d("JsonObject Response",response.toString());
//                            System.out.println("======================================");
//                            try {
//                                JSONObject lat = response.getJSONObject("lat");
//                            } catch (JSONException e) {
//                                Log.d("Web Service Error",e.getMessage());
//                                System.out.println("======================================");
//                                e.printStackTrace();
//                            }
//                        }
//                    },new Response.ErrorListener() {
//                        @Override
//                        public void onErrorResponse(VolleyError volleyError) {
//                            Log.d("JsonObject Error",volleyError.toString());
//                            System.out.println("======================================");
//                        }
//                    });
//                    request.setRetryPolicy(new DefaultRetryPolicy(
//                            500000,
//                            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
//                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//                    request.setRetryPolicy(new RetryPolicy() {
//                        @Override
//                        public int getCurrentTimeout() {
//                            return 50000;
//                        }
//
//                        @Override
//                        public int getCurrentRetryCount() {
//                            return 50000;
//                        }
//
//                        @Override
//                        public void retry(VolleyError error) throws VolleyError {
//
//                        }
//                    });
                    // Add the request to the RequestQueue.
//                    queue.add(stringRequest);
//                    queue.add(request);

                    // Using Async Task to query the server
                    gasStationTask = new GasStationTask();
//                    gasStationTask.execute(slt,elt);
                    gasStationTask.execute("atlanta","Marietta");

                    //go to next screen on submit
                    Intent goToResults = new Intent(MainActivity.this, RoutesDisplay.class);
//                    goToResults.putExtra("STATION_LIST",);
                    goToResults.putExtra("SOURCE_ADDR",slt);
                    goToResults.putExtra("DEST_ADDR",elt);
                    startActivity(goToResults);
                }
                //TODO: process start and end locations
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
            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection)url.openConnection();
            // Connecting to url
            urlConnection.connect();
            // Reading data from url
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
            // Setting the adapter
//            startLocTxt.setAdapter(adapter);
//            endLocTxt.setAdapter(adapter);
            curTxt.setAdapter(adapter);
        }
    }


    // Fetches data from our server
    private class GasStationTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... address) {
            String data = "";
            String src="", dest="";
            try {
                src = "origin_address=" + URLEncoder.encode(address[0], "utf-8");
                dest= "destination_address=" + URLEncoder.encode(address[1], "utf-8");
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }
            String url = SERVER+src+"&"+dest;
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
            System.out.println("========================================\ndata from our server: "+result+"\n========================================");
//            parserTask = new ParserTask();
//            parserTask.execute(result);
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

