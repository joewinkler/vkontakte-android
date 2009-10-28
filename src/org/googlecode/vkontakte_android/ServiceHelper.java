package org.googlecode.vkontakte_android;

import org.googlecode.vkontakte_android.service.IVkontakteService;

import android.os.IBinder;
import android.util.Log;

public class ServiceHelper {
	
     private final static String TAG = "org.googlecode.vkontakte_android.ServiceHelper";

	 public static IVkontakteService mVKService;

	 public static void connect(IBinder service){
         ServiceHelper.mVKService = IVkontakteService.Stub.asInterface(service);
         Log.d(TAG, "Service has been connected");
	 }
	 
	 public static void disconnect(){
         Log.d(TAG, "Service has been disconnected");
	 }
}
