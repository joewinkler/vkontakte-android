package org.googlecode.vkontakte_android.service;

import java.io.IOException;
import java.util.Map;

import org.googlecode.vkontakte_android.service.ApiCheckingKit.UpdateType;
import org.json.JSONException;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

public class CheckingService extends Service {

	CheckingThread m_checkingThread;
	//boolean m_hasConnection = true;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		m_checkingThread = new CheckingThread(getApplicationContext());
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
	Log.d("serv", "service started");
    m_checkingThread.start();
	}

	@Override
	public void onDestroy() {
		Log.d("serv", "service stopped");
		m_checkingThread.interrupt();
		super.onDestroy();
	}

}

class CheckingThread extends Thread {

	private Context m_ctx; 
	CheckingThread(Context ctx)
	{
		m_ctx = ctx;
	}
	
	@Override
	public void run() {
		try { 
			while (!isInterrupted()) {
				try {
					sleep(5000);
					ApiCheckingKit kit = ApiCheckingKit.getInstance();
					Map<UpdateType, Long> res = kit.getUpdates(); //fetch updates from site
					processMessages(kit, res);
					processFriends(kit, res);
					processPhotoTags(kit, res);
				} catch (IOException e) {
					UpdatesNotifier.notify(m_ctx, "Can't connect to the server"); 
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
			}
		} catch (InterruptedException e) {
			Log.d("CheckingThread", "interrupted");
			
			return;
		}

	}

	//==============================================================================
	
	private void processMessages(ApiCheckingKit kit, Map<UpdateType, Long> res) {
		long incomingMess = res.get(UpdateType.MESSAGES)
				- kit.getPreviosUnreadMessNum();

		if (incomingMess == 0) //messages count didn't changed since last checking 
			return;

		if (incomingMess > 0) // new incoming messages
		{
			UpdatesNotifier.notify(m_ctx, "New messages: "+incomingMess);
			kit.setPreviosUnreadMessNum(res.get(UpdateType.MESSAGES));
		} else // some messages were read by another way
		{
			kit.setPreviosUnreadMessNum(res.get(UpdateType.MESSAGES));
		}
	}
	
	private void processFriends(ApiCheckingKit kit, Map<UpdateType, Long> res) {
		long incomingFr = res.get(UpdateType.FRIENDSHIP_REQ)
				- kit.getPreviosFriendshipRequestsNum();

		if (incomingFr == 0)  
			return;

		if (incomingFr > 0) 
		{
			UpdatesNotifier.notify(m_ctx, "New friends: "+incomingFr);
			kit.setPreviosFriendshipRequestsNum(res.get(UpdateType.FRIENDSHIP_REQ));
		} else 
		{
			kit.setPreviosFriendshipRequestsNum(res.get(UpdateType.MESSAGES));
		}
	}

	private void processPhotoTags(ApiCheckingKit kit, Map<UpdateType, Long> res) {
		long incomingTags = res.get(UpdateType.TAGS)
				- kit.getPreviosNewPhotoTagsNum();

		if (incomingTags == 0)  
			return;

		if (incomingTags > 0) 
		{
			UpdatesNotifier.notify(m_ctx, "New photo tags: "+incomingTags);
			kit.setPreviosNewPhotoTagsNum(res.get(UpdateType.TAGS));
		} else 
		{
			kit.setPreviosNewPhotoTagsNum(res.get(UpdateType.TAGS));
		}
	}
  }

