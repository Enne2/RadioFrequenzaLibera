/*
Matteo Benedetto Copyright 2017
 */

package net.enne2.rfl.radiofrequenzalibera;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.prof.rssparser.Article;
import com.prof.rssparser.Parser;

import java.util.ArrayList;
import android.os.Binder;
public class MainActivity extends AppCompatActivity {
    public Context root;
    String status;

    private RecyclerView mRecyclerView;
    private ArticleAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ProgressBar progressBar;
    private String urlString = "http://blog.frequenzalibera.it/feed/";
    public void startService(View v)
    {
        startService(new Intent(this,StreamIntentService.class));
        Toast.makeText(this, "Caricamento...", Toast.LENGTH_SHORT).show();
    }

    public void stopService(View v)
    {
        stopService(new Intent(this,StreamIntentService.class));
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:


                    mSwipeRefreshLayout.setVisibility(View.GONE);
                    findViewById(R.id.homelayout).setVisibility(View.VISIBLE);

                    return true;
                case R.id.navigation_dashboard:
                    mSwipeRefreshLayout.setVisibility(View.VISIBLE);
                    findViewById(R.id.homelayout).setVisibility(View.GONE);
                    loadblog();
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
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(this, "landscape"+status, Toast.LENGTH_SHORT).show();
            this.status = status;
            checkStatus();

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            Toast.makeText(this, "portrait"+status, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState != null) {
            status = savedInstanceState.getString("status", "STOP");
            checkStatus();
        }

        root = this.getApplicationContext();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("RFLSTATUS"));
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        mRecyclerView = (RecyclerView) findViewById(R.id.swipelist);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);
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
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    public void onNewIntent(Intent intent){
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            // Get extra data included in the Intent
            String message = intent.getStringExtra("STATUS");
            status = message;
            checkStatus();
            Log.d("receiver", "STATUS: " + message);
        }

    };
    public void checkStatus(){
        if(status=="PLAY"){
            ((ImageButton) findViewById(R.id.homeload)).setVisibility(View.GONE);
            ((ImageButton) findViewById(R.id.homeplay)).setVisibility(View.GONE);
            ((ImageButton) findViewById(R.id.homestop)).setVisibility(View.VISIBLE);
        }
        else if(status=="STOP"){
            ((ImageButton) findViewById(R.id.homeload)).setVisibility(View.GONE);
            ((ImageButton) findViewById(R.id.homestop)).setVisibility(View.GONE);
            ((ImageButton) findViewById(R.id.homeplay)).setVisibility(View.VISIBLE);
        }else if(status=="LOADING"){
            ((ImageButton) findViewById(R.id.homeplay)).setVisibility(View.GONE);
            ((ImageButton) findViewById(R.id.homestop)).setVisibility(View.GONE);
            ((ImageButton) findViewById(R.id.homeload)).setVisibility(View.VISIBLE);
        }
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
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Make sure to call the super method so that the states of our views are saved
        super.onSaveInstanceState(outState);
        // Save our own state now
        outState.putString("status", status);
    }
}