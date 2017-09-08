/*
Matteo Benedetto Copyright 2017
 */

package net.enne2.rfl.radiofrequenzalibera;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.prof.rssparser.Article;
import com.prof.rssparser.Parser;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    public  NotificationManager mNotifyMgr;
    public TextView mTextMessage;
    public RemoteViews contentView;
    public Notification notification;
    public StreamThread SThread;
    public Intent intent;
    public PendingIntent pIntent;
    public LinearLayout homelayout;
    public Button playhome;
    public ImageButton playpush;
    public View.OnTouchListener play, stop;
    private RecyclerView mRecyclerView;
    private ArticleAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ProgressBar progressBar;
    private String urlString = "http://blog.frequenzalibera.it/feed/";

    public NotificationCompat.Builder mBuilder;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    mSwipeRefreshLayout.setVisibility(View.GONE);
                    homelayout.setVisibility(View.VISIBLE);
                    return true;
                case R.id.navigation_dashboard:
                    mSwipeRefreshLayout.setVisibility(View.VISIBLE);
                    homelayout.setVisibility(View.GONE);
                    loadblog();
                    /*SThread.run();
                    contentView.setTextViewText(R.id.text, "Caricamento...");
                    */
                    return true;
                case R.id.navigation_notifications:
                    //SThread.stop();
                    return true;
            }
            return false;
        }

    };
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        intent = new Intent(this, MainActivity.class);
        pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);
        contentView = new RemoteViews(getPackageName(), R.layout.custom_push);
        contentView.setImageViewResource(R.id.image, R.mipmap.ic_launcher);
        contentView.setImageViewResource(R.id.image2, R.drawable.ic_uaofvv4txy);
        contentView.setTextViewText(R.id.title, "Radio Frequenza Libera");
        contentView.setTextViewText(R.id.text, "Format");
       // contentView.setOnClickPendingIntent(R.id.playpush,);

        play = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                SThread.run();
                playhome.setText("Stop");
                playhome.setOnTouchListener(stop);

                return false;
            }
        };
        stop = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                SThread.stop();
                playhome.setText("Play");
                playhome.setOnTouchListener(play);
                return false;
            }
        };
        playhome = (Button) findViewById(R.id.playhome);
        playhome.setOnTouchListener(play);

        mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_uaofvv4txy)
                .setContent(contentView)
                .setContentTitle("Radio Frequenza Libera")
                .setContentText("In riproduzione - Clicca per interrompere")
                .setContentIntent(pIntent)
                .setOngoing(true);

        notification = mBuilder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.defaults |= Notification.DEFAULT_SOUND;
        notification.defaults |= Notification.DEFAULT_VIBRATE;

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        ClientThread ClThread = new ClientThread(this);
        SThread = new StreamThread(this);
        Thread cThread = new Thread(ClThread);
        cThread.start();


        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        mRecyclerView = (RecyclerView) findViewById(R.id.swipelist);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);
        homelayout = (LinearLayout) findViewById(R.id.homelayout);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipecontainer);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark);
        mSwipeRefreshLayout.canChildScrollUp();
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {

                mAdapter.clearData();
                mAdapter.notifyDataSetChanged();
                mSwipeRefreshLayout.setRefreshing(true);
                loadFeed();


            }
        });



    }
    public void loadblog(){
        if (!isNetworkAvailable()) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Connessione non disponibile")
                .setTitle("Offline")
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                finish();
                            }
                        });

        AlertDialog alert = builder.create();
        alert.show();

    } else if (isNetworkAvailable()) {

        loadFeed();
    }}

    public void loadFeed() {

        if (!mSwipeRefreshLayout.isRefreshing())
            progressBar.setVisibility(View.VISIBLE);

        Parser parser = new Parser();
        parser.execute(urlString);
        parser.onFinish(new Parser.OnTaskCompleted() {
            //what to do when the parsing is done
            @Override
            public void onTaskCompleted(ArrayList<Article> list) {
                //list is an Array List with all article's information
                //set the adapter to recycler view
                mAdapter = new ArticleAdapter(list, R.layout.row, MainActivity.this);
                mRecyclerView.setAdapter(mAdapter);
                progressBar.setVisibility(View.GONE);
                mSwipeRefreshLayout.setRefreshing(false);

            }

            //what to do in case of error
            @Override
            public void onError() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        mSwipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(MainActivity.this, "Unable to load data. Swipe down to retry.",
                                Toast.LENGTH_SHORT).show();
                        Log.i("Unable to load ", "articles");
                    }
                });
            }
        });
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    public void onNewIntent(Intent intent){
        SThread.stop();
        mNotifyMgr.cancel(001);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog.Builder(MainActivity.this).create();
            alertDialog.setTitle(R.string.app_name);
            alertDialog.setMessage(Html.fromHtml("https://github.com/Enne2/RadioFrequenzaLibera'>GitHub.</a>"));
            alertDialog.setButton(android.support.v7.app.AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();

            ((TextView) alertDialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
        }

        return super.onOptionsItemSelected(item);
    }
}