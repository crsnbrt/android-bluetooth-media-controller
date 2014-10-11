package com.cbritt.btmediacontroler;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Carson on 9/4/2014.
 * helper class for broadcasting media playback intents.
 */
public class PlaybackManager {

    private final String TAG = "Playback Manager";
    private final Timer timer = new Timer();
    private Context context;
    private int key_command;

    public PlaybackManager(Context c){
        context = c;
    }

    private int getKeyCommand(){
        return this.key_command;
    }

    private void setKeyCommand(int k){
        this.key_command = k;
    }

    public void togglePlayback(){
        Log.d(TAG, "Broadcasting Media Playback Intent: Play / Pause");
        setKeyCommand(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
        this.broadcastMediaIntent();
    }

    public void nextTrack(){
        Log.d(TAG, "Broadcasting Media Playback Intent: Next Track");
        setKeyCommand(KeyEvent.KEYCODE_MEDIA_NEXT);
        this.broadcastMediaIntent();
    }

    public void previousTrack(){
        Log.d(TAG, "Broadcasting Media Playback Intent: Previous Track");
        setKeyCommand(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
        this.broadcastMediaIntent();
    }

    private void broadcastMediaIntent(){
        //fire keydown
        Intent mediaEvent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, getKeyCommand());
        mediaEvent.putExtra(Intent.EXTRA_KEY_EVENT, event);
        context.sendBroadcast(mediaEvent);

        //fire keyup 100ms later
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                Intent mediaEvent = new Intent(Intent.ACTION_MEDIA_BUTTON);
                KeyEvent event = new KeyEvent(KeyEvent.ACTION_UP,getKeyCommand());
                mediaEvent.putExtra(Intent.EXTRA_KEY_EVENT, event);
                context.sendBroadcast(mediaEvent);
            }
        }, 100);
    }
}
