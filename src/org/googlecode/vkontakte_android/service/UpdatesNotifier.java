package org.googlecode.vkontakte_android.service;

import android.content.Context;
import android.os.Looper;
import android.widget.Toast;

//TODO toast => notifier
class UpdatesNotifier {
    public static void notify(final Context ctx, final String mess, boolean hasSound) {
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                Toast t = Toast.makeText(ctx, mess, Toast.LENGTH_SHORT);
                t.show();
                Looper.loop();
            }
        }.start();
    }

}