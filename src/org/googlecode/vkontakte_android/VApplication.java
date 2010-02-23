package org.googlecode.vkontakte_android;

import java.util.concurrent.Semaphore;

import org.googlecode.vkontakte_android.service.CheckingService;
import org.googlecode.vkontakte_android.ui.LoginActivity;
import org.googlecode.vkontakte_android.utils.PreferenceHelper;
import org.googlecode.vkontakte_android.utils.ServiceHelper;
import org.googlecode.vkontakte_android.utils.VLog;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class VApplication extends Application implements ServiceConnection {
	private static final String TAG = "VK:VApplication";
	
	public static Semaphore s_bindingSem = new Semaphore(0);
	
	private static VApplication s_instance;
	
	@Override
	public void onCreate() { 
		super.onCreate();
		s_instance = this;	
		VLog.initialize(s_instance);
		bindService(new Intent(this, CheckingService.class), this, Context.BIND_AUTO_CREATE);
	}

	@Override
    public void onServiceConnected(ComponentName componentName, IBinder service) {
        ServiceHelper.connect(service);
        VApplication.s_bindingSem.release();
    }  

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        ServiceHelper.disconnect();
    }
    
    public static VApplication getInstance() {
    	return s_instance;
    }
    
    public static void logout(final Activity activity) {
    	new AsyncTask<Void, Void, RemoteException>() {

			@Override
			protected void onPostExecute(RemoteException result) {
				activity.finish();
				PreferenceHelper.setLogged(VApplication.getInstance(), false);
				Intent i = new Intent(VApplication.getInstance(), LoginActivity.class);
				i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				VApplication.getInstance().startActivity(i);
			}

			@Override
			protected RemoteException doInBackground(Void... params) {
				try {
					ServiceHelper.getService().logout();
				} catch (RemoteException e) {
					e.printStackTrace();
					return e;
				}
				return null;
			}
    		
    	}.execute();
    }
    
    public static void stopService(final Activity activity) {
    	//we have to unbind service before stopping it. 
    	VApplication.getInstance().unbindService(VApplication.getInstance());
    	VApplication.getInstance().stopService(new Intent(VApplication.getInstance(), CheckingService.class));
        activity.finish();
   } 
    
    // brutal way to finish both application and service
    public static void exit() {
    	
    	//TODO make all cleanups
    	
    	ActivityManager am = (ActivityManager) getInstance().getSystemService(ACTIVITY_SERVICE);
    	am.restartPackage(getInstance().getPackageName());
    }

}
