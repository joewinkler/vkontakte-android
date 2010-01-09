package org.googlecode.vkontakte_android.utils;

import android.os.IBinder;
import android.util.Log;
import org.googlecode.vkontakte_android.service.IVkontakteService;

public class ServiceHelper {

    private final static String TAG = "org.googlecode.vkontakte_android.ServiceHelper";

    private static IVkontakteService mVKService;

    public static IVkontakteService getService() {
        if (ServiceHelper.mVKService == null) {
            Log.e(TAG, "Service binder is null");
        }
        return ServiceHelper.mVKService;
    }

    public static void connect(IBinder service) {
        ServiceHelper.mVKService = IVkontakteService.Stub.asInterface(service);
        Log.d(TAG, "Service has been connected");
    }

    public static void disconnect() {
        Log.d(TAG, "Service has been disconnected");
    }
}
