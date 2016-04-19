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

    PlacesTask placesTask_start;
    PlacesTask placesTask_end;
    ParserTask parserTask;

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
                System.out.println("start: "+slt+",end: "+elt+",milerange: "+mrt);
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
                    System.out.println("Please complete all the fields");
                }
                else {
                    float mileRange = Float.parseFloat(mrt);
                    //go to next screen on submit
                    Intent goToResults = new Intent(MainActivity.this, RoutesDisplay.class);
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

