/*
Matteo Benedetto Copyright 2017
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
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