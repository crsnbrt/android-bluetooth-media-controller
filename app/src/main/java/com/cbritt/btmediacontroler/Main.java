package com.cbritt.btmediacontroler;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ToggleButton;

import com.cbritt.btmediacontroler.PlaybackManager;


public class Main extends Activity {

    boolean bluetoothServiceRunning = false;
    final String TAG = "BT controller activity";
    //final BluetoothChatService cs = new BluetoothChatService(getApplicationContext());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button button_toggle = (Button) findViewById(R.id.button_toggle);
        final Button button_next = (Button) findViewById(R.id.button_next);
        final Button button_prev = (Button) findViewById(R.id.button_prev);
        final String INTENT_TAG = "com.cbritt.BLUETOOTH_SERIAL_INPUT";
        final PlaybackManager pm = new PlaybackManager(getApplicationContext());

        //bind buttons
        button_toggle.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                BroadcastIntentWithInt(INTENT_TAG, 0);
            }
        });

        button_next.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                BroadcastIntentWithInt(INTENT_TAG, 1);
            }
        });

        button_prev.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                BroadcastIntentWithInt(INTENT_TAG, 2);
            }
        });
    }

    public void onServiceToggle(View view) {
        boolean on = ((ToggleButton) view).isChecked();
        if (on) {
            if(!bluetoothServiceRunning) {
                bluetoothServiceRunning = true;
                Log.d(TAG, "service running");
                //cs.start();
                startService(new Intent(this, BluetoothService.class));
            }
        } else {
            if(bluetoothServiceRunning) {
                bluetoothServiceRunning = false;
                Log.d(TAG, "service stopped");
                //cs.stop();
                stopService(new Intent(this, BluetoothService.class));
            }
        }
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
    public void onDestroy(){
        if(this.bluetoothServiceRunning) {
            bluetoothServiceRunning = false;
            stopService(new Intent(getApplicationContext(), BluetoothService.class));
        }
    }

    private void BroadcastIntentWithInt(String action, int value){
        Intent i = new Intent();
        i.putExtra("serial_value", value);
        i.setAction(action);
        sendBroadcast(i);
    }

}
