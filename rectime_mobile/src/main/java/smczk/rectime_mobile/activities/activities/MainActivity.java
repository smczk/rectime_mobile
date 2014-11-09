package smczk.rectime_mobile.activities.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.nfc.*;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import smczk.rectime_mobile.R;
import smczk.rectime_mobile.activities.models.Movement;
import smczk.rectime_mobile.activities.models.Point;
import smczk.rectime_mobile.activities.models.Record;

public class MainActivity extends ActionBarActivity {

    private NfcAdapter mNfcAdapter;
    private RestTemplate restTemplate = new RestTemplate();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume(){
        super.onResume();

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if(mNfcAdapter == null){
            Toast.makeText(getApplicationContext(), "no Nfc feature", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if(!mNfcAdapter.isEnabled()){
            Toast.makeText(getApplicationContext(), "off Nfc feature", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,getClass()), 0);

        IntentFilter[] intentFilter = new IntentFilter[]{
                new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)
        };

        String[][] techList = new String[][]{
                {
                        android.nfc.tech.NfcA.class.getName(),
                        android.nfc.tech.NfcB.class.getName(),
                        android.nfc.tech.IsoDep.class.getName(),
                        android.nfc.tech.MifareClassic.class.getName(),
                        android.nfc.tech.MifareUltralight.class.getName(),
                        android.nfc.tech.NdefFormatable.class.getName(),
                        android.nfc.tech.NfcV.class.getName(),
                        android.nfc.tech.NfcF.class.getName(),
                }
        };
        mNfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilter, techList);
    }

    @Override
    public void onPause(){
        super.onPause();

        mNfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent){
        super.onNewIntent(intent);

        String action = intent.getAction();
        if(TextUtils.isEmpty(action)){
            return;
        }

        if(!action.equals(NfcAdapter.ACTION_TAG_DISCOVERED)){
            return;
        }

        byte[] rawId = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
        //String id = "nothing";

        final String extra_id = bytesToString(rawId);
        final Integer user_id = 1;

        Toast.makeText(getApplicationContext(), extra_id, Toast.LENGTH_SHORT).show();

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        restTemplate.getMessageConverters().add(new StringHttpMessageConverter());

        Point point = isPointRegistered(user_id, extra_id);
        if(point.id.equals(0)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Register New point");
            builder.setMessage("Name");
            LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
            final View layout = inflater.inflate(R.layout.point_register,(ViewGroup)findViewById(R.id.edittext));
            builder.setView(layout);

            builder.setPositiveButton("REGISTER", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    EditText input = (EditText)layout.findViewById(R.id.edittext);

                    if(input.getText().toString() != null) {
                        Point point = registerNewPoint(user_id, extra_id, input.getText().toString());
                        Movement movement = registerNewMovement(user_id);
                        Record record = registerNewRecord(point.id, movement.id, "comment");
                    }
                }
            });

            builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            builder.create().show();

        }else{
            Movement movement = latestMovement(user_id);
            if(movement.id.equals(0)){
                movement = registerNewMovement(user_id);
                Record record = registerNewRecord(point.id, movement.id, "comment");
            }else{
                Record record = registerNewRecord(point.id, movement.id, "comment");
                finishMovement(movement.id);
            }
        }
    }

    public Point isPointRegistered(Integer user_id, String extra_id) {

        String url = getResources().getString(R.string.url) + "/points" + "/" + user_id.toString() + "/" + extra_id;
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        Point[] points = restTemplate.getForObject(url, Point[].class);

        if(points.length != 0){
            return points[0];
        }
        return new Point();
    }

    public Movement latestMovement(Integer user_id) {

        String url = getResources().getString(R.string.url) + "/movements" + "/" + user_id.toString() + "/latest";
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        Movement[] movements = restTemplate.getForObject(url, Movement[].class);

        if(movements.length != 0){
            return movements[0];
        }
        return new Movement();
    }

    public Movement finishMovement(Integer movement_id) {

        MultiValueMap<String, Object> values = new LinkedMultiValueMap<String, Object>();
        values.add("movement_id", movement_id.toString());
        values.add("completed", "true");

        String url = getResources().getString(R.string.url) + "/movements" + "/" + movement_id.toString();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        String result = new String();
        try {
          result = restTemplate.postForObject(url, values, String.class);
        } catch(RestClientException e) {
            e.printStackTrace();
        }

        ObjectMapper mapper = new ObjectMapper();
        Movement movement = null;
        try {
            movement = mapper.readValue(result, Movement.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return movement;
    }

    public Point registerNewPoint(Integer user_id, String extra_id, String name) {

        MultiValueMap<String, Object> values = new LinkedMultiValueMap<String, Object>();
        values.add("user_id", user_id.toString());
        values.add("extra_id", extra_id);
        values.add("name", name);

        String url = getResources().getString(R.string.url) + "/points";

        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        String result = restTemplate.postForObject(url, values, String.class);

        ObjectMapper mapper = new ObjectMapper();
        Point point = null;
        try {
            point = mapper.readValue(result, Point.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return point;
    }

    public Movement registerNewMovement(Integer user_id) {

        MultiValueMap<String, Object> values = new LinkedMultiValueMap<String, Object>();
        values.add("user_id", user_id.toString());

        String url = getResources().getString(R.string.url) + "/movements";

        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        String result = restTemplate.postForObject(url, values, String.class);

        ObjectMapper mapper = new ObjectMapper();
        Movement movement = null;
        try {
            movement = mapper.readValue(result, Movement.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return movement;
    }

    public Record registerNewRecord(Integer point_id, Integer movement_id, String comment) {

        MultiValueMap<String, Object> values = new LinkedMultiValueMap<String, Object>();
        values.add("point_id", point_id.toString());
        values.add("movement_id", movement_id.toString());
        values.add("comment", comment);

        String url = getResources().getString(R.string.url) + "/records";

        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        String result = restTemplate.postForObject(url, values, String.class);

        ObjectMapper mapper = new ObjectMapper();
        Record record = null;
        try {
            record = mapper.readValue(result, Record.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return record;
    }

    public String bytesToString(byte[] bytes){
        StringBuilder buffer = new StringBuilder();
        boolean isFirst = true;

        for(byte b : bytes){
            if(isFirst){
                isFirst = false;
            } else {
                buffer.append("-");
            }
            buffer.append(Integer.toString(b & 0xff));
        }
        return buffer.toString();
    }
}