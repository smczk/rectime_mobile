package smczk.rectime_mobile;

import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.nfc.*;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;

import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

public class MainActivity extends ActionBarActivity {

    private NfcAdapter mNfcAdapter;
    RestTemplate restTemplate = new RestTemplate();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
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

        //▼▼▼▼ここから
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

        Toast.makeText(getApplicationContext(), "Read successful!", Toast.LENGTH_SHORT).show();

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());

        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("user_id", "1");

        String url = "https://mysterious-retreat-9693.herokuapp.com/movements";
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
        //String result = restTemplate.getForObject(url, String.class, "SpringSource");
        String result = restTemplate.postForObject(url, map, String.class);
        Log.d("RestTemplate result", result);
    }
}
