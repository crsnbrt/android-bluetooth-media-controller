package com.cbritt.btmediacontroler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.cbritt.btmediacontroler.PlaybackManager;

import java.util.Locale;

/**
 * Created by Carson on 9/4/2014.
 */
public class BluetoothListener extends BroadcastReceiver{

    private final String TAG = "Bluetooth Listener";

    @Override
    public void onReceive(final Context context, final Intent intent)
    {
        Log.d(TAG, "Received Intent");
        final PlaybackManager pm = new PlaybackManager(context);
        int value = intent.getIntExtra("serial_value", -1);

        if(value >= 0){
            switch (value){
                case 0:
                    pm.togglePlayback();
                    break;
                case 1:
                    pm.nextTrack();
                    break;
                case 2:
                    pm.previousTrack();
                    break;
                default:
                    Log.d(TAG, "value does not correspond to a valid command");
                    break;
            }
        }
    }
}
