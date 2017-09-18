package net.enne2.rfl.radiofrequenzalibera;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by enne2 on 09/09/17.
 */

public class ControlBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("TAG", "test");
    }

}