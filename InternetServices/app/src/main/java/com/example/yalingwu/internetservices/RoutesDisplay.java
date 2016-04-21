package com.example.yalingwu.internetservices;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RoutesDisplay extends FragmentActivity {

    ListView stationList;
    ArrayList<String> addrList = new ArrayList<String>();
    String source_addr;
    String dest_addr;
    GoogleMap map;
    ArrayList<LatLng> markerPoints;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routes_display);

        markerPoints = new ArrayList<LatLng>();
        SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        map = fm.getMap();
        if (map != null) {
            map.setMyLocationEnabled(true);
            // Getting URL to the Google Directions API
            String url = getDirectionsUrl(source_addr, dest_addr);

            DownloadTask downloadTask = new DownloadTask();

            // Start downloading json data from Google Directions API
            downloadTask.execute(url);

        }

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            source_addr = extras.getString("SOURCE_ADDR");
            dest_addr = extras.getString("DEST_ADDR");
//            System.out.println(source_addr+"------------------->"+dest_addr);
        }
//        Polyline line = map.addPolyline(new PolylineOptions()
//                .add(source_addr,dest_addr)
//                .geodesic(true));

        stationList = (ListView) findViewById(R.id.stationList);
//        final StableArrayAdapter adapter = new StableArrayAdapter(this,
//                android.R.layout.simple_list_item_1, addrList);
//        stationList.setAdapter(adapter);
        stationList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, addrList));
//        stationList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView<?> parent, final View view,
//                                    int position, long id) {
////                final String item = (String) parent.getItemAtPosition(position);
////                System.out.println("SELECTED ITEM IN THE LIST: "+item);
////                view.animate().setDuration(2000).alpha(0)
////                        .withEndAction(new Runnable() {
////                            @Override
////                            public void run() {
////                                addrList.remove(item);
////                                adapter.notifyDataSetChanged();
////                                view.setAlpha(1);
////                            }
////                        });
//            }
//
//        });

        double latitude = 40.714224;
        double longitude = -73.961452; //Grand St/Bedford Av, Brooklyn, NY 11211, USA
        LocationAddress locationAddress = new LocationAddress();
        locationAddress.getAddressFromLocation(latitude, longitude,
                getApplicationContext(), new GeocoderHandler());
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

//    private class StableArrayAdapter extends ArrayAdapter<String> {
//
//        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();
//
//        public StableArrayAdapter(Context context, int textViewResourceId,
//                                  List<String> objects) {
//            super(context, textViewResourceId, objects);
//            for (int i = 0; i < objects.size(); ++i) {
//                mIdMap.put(objects.get(i), i);
//            }
//        }
//
//        @Override
//        public long getItemId(int position) {
//            String item = getItem(position);
//            return mIdMap.get(item);
//        }
//
//        @Override
//        public boolean hasStableIds() {
//            return true;
//        }
//
//    }

    private String getDirectionsUrl(String origin, String dest) {

        // Origin of route
        String str_origin = "origin=" + origin;

        // Destination of route
        String str_dest = "destination=" + dest;


        // Sensor enabled
        String sensor = "sensor=false";
        String key = "key=AIzaSyCxqmNu0izsRuTzS0ykD1gLhLZEgCdk00I";
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor+"&"+key;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception while downloading url", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "RoutesDisplay Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.example.yalingwu.internetservices/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "RoutesDisplay Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.example.yalingwu.internetservices/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }


    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            System.out.println("-------------------------------------\n" + "Route request response: "+result + "\n-------------------------------------");
            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();
                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();
            if(result.isEmpty()) {
                System.out.println("NO RESULTS FOUND FOR THE ROUTE");
                return;
            }
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                lineOptions.addAll(points);
                lineOptions.width(2);
                lineOptions.color(Color.RED);

            }

            // Drawing polyline in the Google Map for the i-th route
            if(lineOptions!=null) map.addPolyline(lineOptions);
        }
    }

    private class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            String locationAddress;
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    locationAddress = bundle.getString("address");
                    break;
                default:
                    locationAddress = null;
            }
//            System.out.println("-------------------------------------\nlocation geocoding: " + locationAddress + "\n-------------------------------------");
            addrList.add(locationAddress);
            ((BaseAdapter) stationList.getAdapter()).notifyDataSetChanged();
        }
    }
}


