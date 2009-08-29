package org.googlecode.vkontakte_android.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class UpdateService extends Service {
    private static final String TAG = "org.googlecode.vkontakte_android.service.UpdateService";

    public static final String SYNC_STARTED = "sync_started";
    public static final String SYNC_FINISHED = "sync_finished";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onStart(Intent intent, int i) {
        super.onStart(intent, i);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                sync();
            }
        });
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }


    public synchronized void sync() {
        sendBroadcast(new Intent(SYNC_STARTED));
        sendBroadcast(new Intent(SYNC_FINISHED));
    }
}