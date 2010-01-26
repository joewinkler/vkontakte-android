package org.googlecode.vkontakte_android;

import java.util.concurrent.Semaphore;

import org.googlecode.vkontakte_android.service.CheckingService;
import org.googlecode.vkontakte_android.utils.ServiceHelper;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

public class VApplication extends Application implements ServiceConnection {
	private static final String TAG = "VK:VApplication";
	public static Semaphore s_bindingSem = new Semaphore(0);
	
	@Override
	public void onCreate() { 
		bindService(new Intent(this, CheckingService.class), this, Context.BIND_AUTO_CREATE);
		super.onCreate();
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

}
