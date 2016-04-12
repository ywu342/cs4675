package com.example.yalingwu.internetservices;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    AutoCompleteTextView startLocTxt;
    AutoCompleteTextView endLocTxt;
    EditText mileRangeTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        startLocTxt = (AutoCompleteTextView) findViewById(R.id.startLocTxt);
        endLocTxt = (AutoCompleteTextView) findViewById(R.id.startLocTxt);
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
                    //TODO: give a popup warning to complete
                    System.out.println("Please complete all fields");
                }
                else {
                    float mileRange = Float.parseFloat(mrt);
                }
                //TODO: process start and end locations
            }
        });
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
