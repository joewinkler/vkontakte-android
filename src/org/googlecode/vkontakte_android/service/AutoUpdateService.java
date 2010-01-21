package org.googlecode.vkontakte_android.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import org.googlecode.vkontakte_android.utils.AppHelper;

/**
 *
 *
 * @author Ayzen
 */
public class AutoUpdateService extends Service {

    private static final String TAG = "VK:AutoUpdateService";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "Service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "Started command");

        String action = intent.getAction();
        if (AppHelper.ACTION_SET_AUTOUPDATE.equals(action))
            processAutoUpdate(intent.getIntExtra(AppHelper.EXTRA_AUTOUPDATE_TIME, -1));

        return START_STICKY;
    }

    private void processAutoUpdate(int time) {
        
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "Service destroyed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
