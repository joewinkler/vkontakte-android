package org.googlecode.vkontakte_android.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import org.googlecode.vkontakte_android.R;

public class AppHelper {

    public static final String AUTHORITY = "org.googlecode.vkontakte_android";

    public static final String ACTION_NOTIFICATION_CLEARED = "org.googlecode.vkontakte_android.action.NOTIFICATION_CLEARED";

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

}
