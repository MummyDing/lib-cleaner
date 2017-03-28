package com.yzy.supercleanmaster.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.yzy.supercleanmaster.service.MemoryCleanService;

public class BootCompleteReceiver extends BroadcastReceiver {
    private static final String TAG = "BootCompleteReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, MemoryCleanService.class);
        context.startService(i);
    }
}
