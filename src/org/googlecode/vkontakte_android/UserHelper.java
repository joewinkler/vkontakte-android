package org.googlecode.vkontakte_android;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.Toast;
import org.googlecode.vkontakte_android.database.UserDao;
import org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper;
import org.googlecode.vkontakte_android.provider.UserapiProvider;

import java.io.FileNotFoundException;
import java.io.InputStream;

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

    static Bitmap getPhoto(Context context, long rowId) throws FileNotFoundException {
        Uri uri = ContentUris.withAppendedId(UserapiProvider.USERS_URI, rowId);
        InputStream is = context.getContentResolver().openInputStream(uri);
        return BitmapFactory.decodeStream(is);
    }

    static Bitmap getPhotoByUserId(Context context, long userId) throws FileNotFoundException {
        return getPhoto(context, UserDao.findByUserId(context, userId).getRowId());
    }
}
