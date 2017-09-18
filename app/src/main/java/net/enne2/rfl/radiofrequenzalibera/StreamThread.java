/*
Matteo Benedetto Copyright 2017

 */

package net.enne2.rfl.radiofrequenzalibera;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.IOException;
import java.net.MalformedURLException;

/**
 * Created by enne2 on 05/09/17.
 */

public class StreamThread implements Runnable {
    public LongOperation LOp;
    private static StreamService parent;
    boolean play = false;
    boolean loading = false;
    public StreamThread(StreamService parent) {
        this.parent = parent;
    }

    public void run() {
        if(!play && !loading) {
            loading = true;
            LOp = new LongOperation();
            LOp.execute("");
        }

    }
    public void stop(){
        if(!loading && play){
            LOp.mediaPlayer.stop();
            play=false;
        }
    }
    private class LongOperation extends AsyncTask<String, Void, String> {
        MediaPlayer mediaPlayer;
        @Override
        protected String doInBackground(String... params) {

            String url = "http://radio.frequenzalibera.it/radio.mp3";
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {

                mediaPlayer.setDataSource(url);
                mediaPlayer.prepare(); // might take long! (for buffering, etc)
            }
            catch (MalformedURLException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
                return "Error";
            }
            mediaPlayer.start();
            play = true;
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            loading = false;
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }
}