package org.googlecode.vkontakte_android.service;

import org.googlecode.vkontakte_android.*;
import org.googlecode.vkontakte_android.database.MessageDao;
import org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.widget.Toast;

//TODO toast => notifier
class UpdatesNotifier {
	
	public static void showError(final Context ctx, final String error) {
		new Thread() {
            @Override
            public void run() { 
                Looper.prepare();
                Toast.makeText(ctx, error, Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
		}.start();
	}
	
    public static void notifyMessages(final Context ctx, final long num, final MessageDao mess) {
        if (num<1) return;
        
    	new Thread() {
            @Override
            public void run() { 
                Looper.prepare();
            	NotificationManager mNotificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            	String tickerText = "VKontakte updates";
            	Notification notification = new Notification(R.drawable.icon, tickerText, System.currentTimeMillis());
            	String contentTitle = "Updates";
            	String contentText  = "New messages: "+num;
            	Intent notificationIntent = null;
            	if (num==1) 
            	{  
            		contentText = mess.getText();
            		//String sender = mess.getSender().getUserName();
            		notificationIntent = new Intent(ctx, ComposeMessageActivity.class)
                            .putExtra(UserapiDatabaseHelper.KEY_MESSAGE_SENDERID, mess.getSenderId());
            	}
            	else 
            	{
            	  notificationIntent = new Intent(ctx, CGuiTest.class).putExtra("tabToShow", "Messages").setFlags(
                          Intent.FLAG_ACTIVITY_SINGLE_TOP);
               	}
            	PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0, notificationIntent, 0);
             	notification.setLatestEventInfo(ctx, contentTitle, contentText, contentIntent);
                notification.flags |= Notification.FLAG_AUTO_CANCEL;
            	final int HELLO_ID = 2;
            	mNotificationManager.notify(HELLO_ID, notification);
                Looper.loop();
            }
        }.start();
    }

}