package org.googlecode.vkontakte_android;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import org.googlecode.vkontakte_android.database.UserDao;
import org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper;

/**
 * Created by Ildar Karimov
 * Date: Oct 10, 2009
 */
public class UserHelper {
    public static void viewProfile(Context context, long rowId) {
        //attention, rowId, not userId
        //todo: implement
        Toast.makeText(context, "view profile not yet implemented", Toast.LENGTH_SHORT).show();
    }

    static void sendMessage(Context context, long rowId) {
        UserDao user = UserDao.get(context, rowId);
        Intent intent = new Intent(context, ComposeMessageActivity.class);
        intent.putExtra(UserapiDatabaseHelper.KEY_MESSAGE_SENDERID, user.getUserId());
        context.startActivity(intent);
    }
}
