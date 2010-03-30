package org.googlecode.vkontakte_android.utils;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

import android.os.Environment;
import android.util.Log;
import org.googlecode.vkontakte_android.R;

import com.nullwire.trace.ExceptionHandler;

public class AppHelper {

    private static final String TAG = "VK:AppHelper";

    public static final String NAME = "vkontakte";
    public static final String AUTHORITY = "org.googlecode.vkontakte_android";

    private static String appDir;
    private static String avatarsDir;

    public static final String ACTION_NOTIFICATION_CLEARED = "org.googlecode.vkontakte_android.action.NOTIFICATION_CLEARED";
    public static final String ACTION_SET_AUTOUPDATE = "org.googlecode.vkontakte_android.action.SET_AUTOUPDATE";
    public static final String ACTION_CHECK_UPDATES = "org.googlecode.vkontakte_android.action.CHECK_UPDATES";

    public static final String EXTRA_AUTOUPDATE_PERIOD = "autoupdate_period";

    public static String getAppDir(Context context) {
        if (appDir == null) {    
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                Log.i(TAG, "SDCard is found, using it");
                appDir = Environment.getExternalStorageDirectory().getPath() + "/" + NAME + "/";
            }
            else {
                Log.i(TAG, "SDCard is not found, using cache folder");
                appDir = context.getCacheDir().getPath() + "/" + NAME + "/";
            }
        }
        return appDir;
    }

    public static String getAvatarsDir(Context context) {
        if (avatarsDir == null)
            avatarsDir = getAppDir(context) + "avatars/";

        return avatarsDir;
    }

    public static void showFatalError(final Activity act, String text) {
        AlertDialog.Builder builder = new AlertDialog.Builder(act);
        AlertDialog dialog = builder.setPositiveButton(R.string.exit,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        act.finish();

                    }
                })
                .setCancelable(false)
                .setTitle(R.string.err_msg_fatal_error)
                .setMessage(text).create();
        dialog.show();
    }
    
    /**
	 * Show dialog offering user to send a report. Used by remote-stacktrace
	 */
	public static void showExceptionDialog(Context ctx) {
		AlertDialog.Builder b = new AlertDialog.Builder(ctx);
		b.setMessage(ctx.getString(R.string.err_msg_offer_report))
		    .setCancelable(false)
		    .setTitle(ctx.getString(R.string.app_name))
		   	.setPositiveButton(ctx.getString(R.string.yes), new OnClickListener() {
			
		   		@Override
		   		public void onClick(DialogInterface dialog, int which) {
		   			ExceptionHandler.submitStackTraces();
		   		}
		   	})
		   	.setNegativeButton(ctx.getString(R.string.no), new OnClickListener() {
			
		   		@Override
		   		public void onClick(DialogInterface dialog, int which) {
		   			ExceptionHandler.deleteStackTrace();
		   		} 
		   		
		   	})
		   	.create()
		   	.show();
	}

}
