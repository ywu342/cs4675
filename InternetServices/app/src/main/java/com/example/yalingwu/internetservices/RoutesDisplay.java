package com.example.yalingwu.internetservices;

import android.app.Dialog;
import android.bluetooth.le.AdvertiseData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;
import org.w3c.dom.Text;

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

    final int NEW_ROUTE_COLOR = Color.rgb(22,125,145);
    ListView stationList;
    ArrayList<String> addrList = new ArrayList<String>();
    String source_addr;
    String dest_addr;
    GoogleMap map;
    ArrayList<LatLng> markerPoints;
    LatLng src;
    LatLng dst;
    LatLng[] stations_coord;
    String[] prices_list;
    int Line_color=Color.BLACK;
    Polyline selected_line;
    int selectedIndex = -1;
    int counter = 0;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        long time= System.currentTimeMillis();
        android.util.Log.i("Time Class ", " At start of the second screen: Time value in milliseconds " + time);
        setContentView(R.layout.activity_routes_display);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            source_addr = extras.getString("SOURCE_ADDR");
            dest_addr = extras.getString("DEST_ADDR");
            //prices_list = extras.getStringArray("PRICES_LIST");
            Parcelable[] pArr = extras.getParcelableArray("STATIONS_COORD");
            stations_coord = new LatLng[pArr.length];
            for (int i = 0; i < pArr.length; i++) {
                stations_coord[i] = (LatLng) pArr[i];
            }
        }

        stationList = (ListView) findViewById(R.id.stationList);
        stationList.setVerticalScrollBarEnabled(true);
        stationList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, addrList));
        stationList.setOnItemClickListener(new ListView.OnItemClickListener() {

            // updating map when clicking on station option
            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                long time= System.currentTimeMillis();
                android.util.Log.i("Time Class ", " After clicking on one list item: Time value in milliseconds "+time);
                selectedIndex = position;
                if(selected_line!=null) selected_line.remove();
                String stationAddr = (String) parent.getItemAtPosition(position);
                LatLng stationLoc = getLocationFromAddress(stationAddr);
                markerPoints.add(stationLoc);
                MarkerOptions options_station = new MarkerOptions();
                options_station.position(stationLoc);
                options_station.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                map.addMarker(options_station);
                Line_color = NEW_ROUTE_COLOR;
                String url = getDirectionsUrl(src, dst, stationLoc);
                long routetime_1= System.currentTimeMillis();
                android.util.Log.i("Time Class ", " About to ask for the src-gas-dst route: Time value in milliseconds " + routetime_1);
                DownloadTask downloadTask = new DownloadTask();
                downloadTask.execute(url);
            }

        });
//        long listTime;
        for (int i = 0; i < stations_coord.length; i++) {
            LocationAddress locationAddress = new LocationAddress();
            locationAddress.getAddressFromLocation(stations_coord[i].latitude, stations_coord[i].longitude,
                    getApplicationContext(), new GeocoderHandler());

        }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        markerPoints = new ArrayList<LatLng>();
        SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_frag);
        map = fm.getMap();
        if (map != null) {
            map.setMyLocationEnabled(true);
            src = getLocationFromAddress(source_addr);
            dst = getLocationFromAddress(dest_addr);
//            System.out.println("src: " + src.toString() + "\ndst: " + dst.toString());
            markerPoints.add(src);
            markerPoints.add(dst);
            MarkerOptions options_src = new MarkerOptions();
            MarkerOptions options_dst = new MarkerOptions();
            options_src.position(src);
            options_src.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            options_dst.position(dst);
            options_dst.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            try {
                map.addMarker(options_src);
                map.addMarker(options_dst);
            }
            catch (Exception e) {
                Log.d("Exception while adding markers to the map", e.getMessage());
            }
            Line_color = Color.rgb(153, 51, 204);
            String url = getDirectionsUrl(src, dst, null);
            long routetime= System.currentTimeMillis();
            android.util.Log.i("Time Class ", " About to ask for the src-dst route: Time value in milliseconds " + routetime);
            DownloadTask downloadTask = new DownloadTask();
            downloadTask.execute(url);
        }

        Button dirBtn = (Button) findViewById(R.id.dirBtn);
        dirBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                long time= System.currentTimeMillis();
                android.util.Log.i("Time Class ", " After clicking direction button: Time value in milliseconds "+time);
                if (selectedIndex == -1) {
                    final Dialog dialog = new Dialog(RoutesDisplay.this);
                    dialog.setContentView(R.layout.not_complete_dial);
                    dialog.setTitle("Forgot something...");
                    Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
                    TextView customDialogBody = (TextView) dialog.findViewById(R.id.customdialbody);
                    customDialogBody.setText("Please click on one gas station to continue");
                    // if button is clicked, close the custom dialog
                    dialogButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                } else {
                    String address = (String) stationList.getItemAtPosition(selectedIndex);
                    openGoogleService(address);
                }
            }
        });
    }

    private void openGoogleService(String stationAddr) {
        Uri anyAddress = Uri.parse("google.navigation:q=" + Uri.encode(stationAddr) + "&mode=d");
        Intent mapI = new Intent(Intent.ACTION_VIEW, anyAddress);
        mapI.setPackage("com.google.android.apps.maps");
        if (mapI.resolveActivity(getPackageManager()) != null) {
            startActivity(mapI);
        }

    }

    // turns address into latitude and longitude
    private LatLng getLocationFromAddress(String strAddress) {
        Geocoder coder = new Geocoder(this);
        List<Address> address=null;
        LatLng loc = null;
        try {
            /**
             * for performance analysis purpose: commenting out calling the server part and return a random LatLng object to mark on the map
             */
//            address = coder.getFromLocationName(strAddress,1);
            if (address == null) {
                return new LatLng(33.777554, -84.388116); //biltmore midtown
            }
            Address location=address.get(0);
            loc = new LatLng(location.getLatitude(), location.getLongitude());
        } catch (Exception e) {
            Log.d("Exception while getting coordinates", e.getMessage());
        }
        return loc;
    }

    private String getDirectionsUrl(LatLng origin,LatLng dest, LatLng station){

        String str_origin = "origin="+origin.latitude+","+origin.longitude;
        String str_dest = "destination="+dest.latitude+","+dest.longitude;

        String str_waypoint = "";
        // Waypoints of route
        if (station != null) {
            str_waypoint = "waypoints=optimize:true|" + station.latitude + "," + station.longitude + "&";
        }
        String sensor = "sensor=false";
        String key = "key=AIzaSyCxqmNu0izsRuTzS0ykD1gLhLZEgCdk00I";
        String parameters = str_origin + "&" + str_dest + "&" + str_waypoint + sensor+"&"+key;
        String output = "json";
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
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
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

        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                /**
                 * for performance analysis purpose: commenting out calling the server part
                 */
//                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            /**
             * for performance analysis purpose: commenting out printing system out
             */
//            System.out.println("-------------------------------------\n" + "Route request response: " + result + "\n-------------------------------------");
            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);

        }
    }

    /**
     * A class to parse the Google Directions in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();
                routes = parser.parse(jObject);
            } catch (Exception e) {
                /**
                 * for performance analysis purpose: commenting out printing e.stack
                 */
//                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();
            if(result==null||result.isEmpty()) {
                System.out.println("NO RESULTS FOUND FOR THE ROUTE");
                /**
                 * for performance analysis purpose: generating a random route
                 */
                Polyline line = map.addPolyline(new PolylineOptions()
                        .add(new LatLng(51.5, -0.1), new LatLng(40.7, -74.0))
                        .width(5)
                        .color(Color.RED));
                long time= System.currentTimeMillis();
                android.util.Log.i("Time Class ", " After the test polyline is drawn: Time value in milliseconds " + time);
                return;
            }
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = result.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Line_color);
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(src);
                builder.include(dst);
                map.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 17));
            }
            if(lineOptions!=null) {
                if(lineOptions.getColor()==NEW_ROUTE_COLOR) selected_line = map.addPolyline(lineOptions);
                else map.addPolyline(lineOptions);
            }
            long time= System.currentTimeMillis();
            android.util.Log.i("Time Class ", " After the polyline: Time value in milliseconds "+time);
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
            addrList.add(locationAddress);
            ((BaseAdapter) stationList.getAdapter()).notifyDataSetChanged();
            long listTime= System.currentTimeMillis();
            android.util.Log.i("Time Class ", " After the " + counter++ + "th list item update: Time value in milliseconds " + listTime);
        }
    }
}


