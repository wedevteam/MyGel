package com.wedev.mygel.functions;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MessaggiBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Util.scheduleJob(context);
    }
}