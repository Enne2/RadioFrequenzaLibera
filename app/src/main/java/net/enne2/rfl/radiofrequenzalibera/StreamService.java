package net.enne2.rfl.radiofrequenzalibera;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.IOException;
import java.net.MalformedURLException;

/**
 * Created by enne2 on 12/09/17.
 */

public class StreamService extends Service {
    private NotificationManager mNM;
    public StreamThread mediaPlayer;

    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private int NOTIFICATION = R.string.local_service_started;

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        StreamService getService() {
            return StreamService.this;
        }
    }

    @Override
    public void onCreate() {
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        // Display a notification about us starting.  We put an icon in the status bar.
        showNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        mNM.cancel(NOTIFICATION);
        mediaPlayer.stop();
        // Tell the user we stopped.
        Toast.makeText(this, R.string.stream_stopped, Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();

    public String play(){

        mediaPlayer = new StreamThread(this);
        mediaPlayer.run();
        return "Executed";
    }
    private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.local_service_started);

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);
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
                .setContentIntent(pIntent)
                .setOngoing(true)
                .build();

        // Send the notification.
        mNM.notify(NOTIFICATION, notification);
    }
}
