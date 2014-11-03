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
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import smczk.rectime_mobile.R;
import smczk.rectime_mobile.activities.models.Movement;
import smczk.rectime_mobile.activities.models.Point;

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

        if(!isPointRegistered(user_id, extra_id)) {
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
                        registerNewPoint(user_id, extra_id, input.getText().toString());
                    }
                }
            });

            builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            builder.create().show();
        }

        if(registerNewMovement(user_id, extra_id)) {
            Log.d("RestTemplate result", "");
        }
    }

    public boolean isPointRegistered(Integer user_id, String extra_id) {

        MultiValueMap<String, Object> values = new LinkedMultiValueMap<String, Object>();
        values.add("user_id", user_id.toString());
        values.add("extra_id", extra_id);

        String url = getResources().getString(R.string.url) + "/points" + "/" + user_id.toString() + "/" + extra_id;

        Point[] res;
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

        try {
            ResponseEntity<Point[]> responseEntity = restTemplate.exchange(url, HttpMethod.GET, null, Point[].class);
            res = responseEntity.getBody();

        } catch (Exception e) {
            Log.d("Error", e.toString());
            return false;
        }

        if(res.length != 0) {
            return true;
        }
        return false;
    }

    public boolean registerNewPoint(Integer user_id, String extra_id, String name) {

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

        if (point == null) {
            return false;
        } 
        return true;
    }

    public boolean registerNewMovement(Integer user_id, String extra_id) {

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

        if (movement == null) {
            return false;
        }

        return true;
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
