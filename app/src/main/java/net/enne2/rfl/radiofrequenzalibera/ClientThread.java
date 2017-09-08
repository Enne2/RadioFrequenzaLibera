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

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;


public class ClientThread implements Runnable {
    public ClientThread(MainActivity parent) {
        this.parent = parent;
    }
    private static MainActivity parent;
    private boolean connected = false;
    public void run() {
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                upDate();
            }
        }, 0, 10000);
    }
    public void upDate(){
        try {
            InetAddress serverAddr = InetAddress.getByName("master.frequenzalibera.it");
            Log.d("ClientActivity", "C: Connecting...");
            Socket socket = new Socket(serverAddr, 6660);
            connected = true;
//            while (connected) {
                try {
                    Log.d("ClientActivity", "C: Sending command.");
                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                    BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    // WHERE YOU ISSUE THE COMMANDS
                    out.println("Hey Server!");
                    final String text = inFromServer.readLine();

                    parent.runOnUiThread(new Runnable() {
                        public void run() {

                            parent.mTextMessage.setText(text);
                            parent.contentView.setTextViewText(R.id.text, text);
                            if(parent.SThread.play)
                                parent.mNotifyMgr.notify(001, parent.mBuilder.build());
                        }
                    });
                    Log.d("ClientActivity", "C: Sent.");

                } catch (Exception e) {

                    Log.e("ClientActivity", "S: Error", e);
                }
 //           }
            socket.close();
            Log.d("ClientActivity", "C: Closed.");
        } catch (Exception e) {
            Log.e("ClientActivity", "C: Error", e);
            connected = false;
        }
    }
}
