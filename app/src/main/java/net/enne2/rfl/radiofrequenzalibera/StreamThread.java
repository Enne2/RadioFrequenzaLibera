/*
Matteo Benedetto Copyright 2017

 */

package net.enne2.rfl.radiofrequenzalibera;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.IOException;
import java.net.MalformedURLException;

/**
 * Created by enne2 on 05/09/17.
 */

public class StreamThread  implements Runnable {
    public LongOperation LOp;
    private static MainActivity parent;
    boolean play = false;
    boolean loading = false;
    public StreamThread(MainActivity parent) {
        this.parent = parent;
    }
    public void toastIt(final String intext){
        parent.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(parent.getBaseContext(), intext, Toast.LENGTH_LONG).show();
                parent.mTextMessage.setText(intext);                       }
        });
    }
    public void run() {
        if(!play && !loading) {

            parent.mNotifyMgr.notify(001, parent.mBuilder.build());
            loading = true;
            toastIt("Caricamento...");
            LOp = new LongOperation();
            LOp.execute("");
        }

    }
    public void stop(){
        if(!loading && play){
            LOp.mediaPlayer.stop();
            toastIt("Stop");
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
                toastIt("Errore di connessione!");
                return "Error";
            }
            mediaPlayer.start();
            toastIt("In riproduzione!");
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