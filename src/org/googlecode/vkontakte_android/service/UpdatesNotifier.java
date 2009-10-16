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
	
	public static void showError(final Context ctx, final int error) {
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
            	String contentTitle = "";
            	String contentText  = "";
            	Intent notificationIntent = null;
            	if (num==1) 
            	{  
            		contentTitle = "New message from "+mess.getSender(ctx).userName;
            		contentText = mess.text;
            		notificationIntent = new Intent(ctx, ComposeMessageActivity.class)
                            .putExtra(UserapiDatabaseHelper.KEY_MESSAGE_SENDERID, mess.senderId);
            	}
            	else 
            	{
                  contentTitle = num+" new messages.";	
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