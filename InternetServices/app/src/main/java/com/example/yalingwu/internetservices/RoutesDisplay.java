package com.example.yalingwu.internetservices;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RoutesDisplay extends FragmentActivity {

    ListView stationList;
    ArrayList<String> addrList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routes_display);

        stationList = (ListView) findViewById(R.id.stationList);
//        final StableArrayAdapter adapter = new StableArrayAdapter(this,
//                android.R.layout.simple_list_item_1, addrList);
//        stationList.setAdapter(adapter);
        stationList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,addrList));
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
            System.out.println("-------------------------------------\nlocation geocoding: " + locationAddress + "-------------------------------------");
            addrList.add(locationAddress);
            ((BaseAdapter) stationList.getAdapter()).notifyDataSetChanged();
        }
    }
}


