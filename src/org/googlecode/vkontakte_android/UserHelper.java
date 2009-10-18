package org.googlecode.vkontakte_android;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import org.googlecode.vkontakte_android.database.UserDao;
import org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper;
import static org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper.KEY_MESSAGE_SENDERID;
import static org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper.KEY_PROFILE_USERID;
import org.googlecode.vkontakte_android.provider.UserapiProvider;
import static org.googlecode.vkontakte_android.provider.UserapiProvider.USERS_URI;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Ildar Karimov
 * Date: Oct 10, 2009
 */
public class UserHelper {
    public static void viewProfile(Context context, long userId) {
        Intent intent = new Intent(context, ProfileViewActivity.class);
        intent.putExtra(KEY_PROFILE_USERID, userId);
        context.startActivity(intent);
    }

    public static void sendMessage(Context context, long userId) {
        Intent intent = new Intent(context, ComposeMessageActivity.class);
        intent.putExtra(KEY_MESSAGE_SENDERID, userId);
        context.startActivity(intent);
    }

    //todo: return null in case of FileNotFoundException
    public static Bitmap getPhoto(Context context, long rowId) {
        Uri uri = ContentUris.withAppendedId(USERS_URI, rowId);
        InputStream is = null;
        Bitmap bitmap = null;
        try {
            is = context.getContentResolver().openInputStream(uri);
            bitmap = BitmapFactory.decodeStream(is);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    //todo: log
                }
            }
        }
        return bitmap;
    }

    public static Bitmap getPhotoByUserId(Context context, long userId) {
        UserDao user = UserDao.findByUserId(context, userId);
        if (user == null || UserapiProvider.isExists(user._data)) return null;//todo: not yet loaded
        else return getPhoto(context, user.getRowId());
    }
}
