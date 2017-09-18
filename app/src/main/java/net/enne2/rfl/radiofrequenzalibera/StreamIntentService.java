package net.enne2.rfl.radiofrequenzalibera;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.RemoteViews;

import java.io.IOException;
import java.net.MalformedURLException;

/**
 * Created by enne2 on 13/09/17.
 */

public class StreamIntentService extends IntentService
{
    boolean play = false;
    boolean loading = false;

    private int NOTIFICATION = R.string.local_service_started;
    MediaPlayer mediaPlayer;
    LocalBroadcastManager broadcaster;
    NotificationManager mNotifyMgr;

    public StreamIntentService()
    {
        super("LogService");
    }

    @Override
    protected void onHandleIntent(Intent i)
    {
        streamStart();
    }
    public void streamStart() {
        broadcaster = LocalBroadcastManager.getInstance(this);
        sendResult("LOADING");

        String url = "http://radio.frequenzalibera.it/radio.mp3";
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {

            mediaPlayer.setDataSource(url);
            mediaPlayer.prepare(); // might take long! (for buffering, etc)
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
            sendResult("STOP");
        }
        catch (IOException e) {
            e.printStackTrace();
            sendResult("STOP");
            return;
        }
        finally {
            mediaPlayer.start();
            play = true;
            Intent intent = new Intent(this, StreamIntentService.class);
            intent.putExtra("status", "STOP");
            PendingIntent contentIntent = PendingIntent.getService(this, 0,
                    intent, 0);
            mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.custom_push);
            contentView.setImageViewResource(R.id.image, R.mipmap.ic_launcher);
            contentView.setImageViewResource(R.id.image2, R.drawable.ic_uaofvv4txy);
            contentView.setTextViewText(R.id.title, "Radio Frequenza Libera");
            contentView.setTextViewText(R.id.text, "Format");        // Set the info for the views that show in the notification panel.
            Notification notification = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.ic_uaofvv4txy)  // the status icon
                    .setSmallIcon(R.drawable.ic_uaofvv4txy)
                    .setContent(contentView)
                    .setContentTitle("Radio Frequenza Libera")
                    .setContentIntent(contentIntent)
                    .setOngoing(true)
                    .build();

            // Send the notification.
            mNotifyMgr.notify(NOTIFICATION, notification);
            sendResult("PLAY");
        }

        while(true)

            try{

                Thread.sleep(10000);
            }
            catch (InterruptedException e)
            { }

    }
    public void sendResult(String message) {
        Intent intent = new Intent("RFLSTATUS");
        if(message != null)
            intent.putExtra("STATUS", message);
        broadcaster.sendBroadcast(intent);
    }
    @Override
    public void onDestroy()
    {
        Log.i("RFLStream", "Distruzione Service");
        mediaPlayer.release();
        mNotifyMgr.cancel(NOTIFICATION);
        sendResult("STOP");
    }

}