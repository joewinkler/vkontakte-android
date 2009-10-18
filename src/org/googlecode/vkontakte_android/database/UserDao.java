package org.googlecode.vkontakte_android.database;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import org.googlecode.userapi.User;
import static org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper.*;
import org.googlecode.vkontakte_android.provider.UserapiProvider;
import static org.googlecode.vkontakte_android.provider.UserapiProvider.USERS_URI;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class UserDao {
    private static final String TAG = "org.googlecode.vkontakte_android.database.UserDao";


    public long rowId;
    public long userId;
    public String userName;
    private String userPhotoUrl;
    public boolean male;
    public boolean online;
    public boolean newFriend;
    public boolean isFriend;
    public String _data;

    public UserDao(Cursor cursor) {
        rowId = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_USER_ROWID));
        userId = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_USER_USERID));
        userName = cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_NAME));
        male = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_USER_MALE)) == 1;
        online = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_USER_ONLINE)) == 1;
        newFriend = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_USER_NEW)) == 1;
        isFriend = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_USER_IS_FRIEND)) == 1;
        userPhotoUrl = cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_AVATAR_URL));
    }

//    public UserDao(long userId, String userName, boolean male, boolean online,
//    		       boolean newFriend, boolean isFriend) {
//        this.userId = userId;
//        Log.d(TAG, "this.userId"+this.userId);
//        this.userName = userName;
//        this.male = male;
//        this.online = online;
//        this.newFriend = newFriend;
//        this.isFriend = isFriend;
//    }

    public UserDao(User user, boolean isNewFriend, boolean isFriend) {
        this.userId = user.getUserId();
        this.userName = user.getUserName();
        this.male = user.isMale();
        this.online = user.isOnline();
        this.newFriend = isNewFriend;
        this.isFriend = isFriend;
        this.userPhotoUrl = user.getUserPhotoUrl();
    }

    public static int bulkSave(Context context, List<UserDao> userListDao) {
        ContentValues[] values = new ContentValues[userListDao.size()];
        int i = 0;
        for (UserDao userDao : userListDao) {
            ContentValues insertValues = new ContentValues();
            insertValues.put(KEY_USER_USERID, userDao.getUserId());
            insertValues.put(KEY_USER_NAME, userDao.getUserName());
            insertValues.put(KEY_USER_MALE, userDao.isMale() ? 1 : 0);
            insertValues.put(KEY_USER_ONLINE, userDao.isOnline() ? 1 : 0);
            insertValues.put(KEY_USER_NEW, userDao.isNewFriend() ? 1 : 0);
            insertValues.put(KEY_USER_IS_FRIEND, userDao.isFriend() ? 1 : 0);
            insertValues.put(KEY_USER_AVATAR_URL, userDao.getUserPhotoUrl());
            userDao._data = userDao.getPath();
            insertValues.put(KEY_USER_AVATAR_SMALL, userDao._data);
            values[i] = insertValues;
            i++;
        }
        return context.getContentResolver().bulkInsert(USERS_URI, values);
    }

    public static UserDao get(Context context, long rowId) {
        if (rowId == -1) return null;
        Cursor cursor = context.getContentResolver().query(ContentUris.withAppendedId(USERS_URI, rowId), null, null, null, null);
        UserDao userDao = null;
        if (cursor != null && cursor.moveToNext()) {
            userDao = new UserDao(cursor);
            cursor.close();
        } else if (cursor != null) cursor.close();
        return userDao;
    }

    public static UserDao findByUserId(Context context, long userId) {
        if (userId == -1) return null;
        Cursor cursor = context.getContentResolver().query(USERS_URI, null, KEY_USER_USERID + "=?", new String[]{String.valueOf(userId)}, null);
        UserDao userDao = null;
        if (cursor != null && cursor.moveToNext()) {
            userDao = new UserDao(cursor);
            cursor.close();
        } else if (cursor != null) cursor.close();
        return userDao;
    }

    public static boolean isMyFriend(Context ctx, Long userid) {
        return ctx.getContentResolver().query(USERS_URI, null, KEY_USER_USERID + "=? AND " + KEY_USER_IS_FRIEND + "=?",
                new String[]{userid.toString(), "1"}, null).moveToNext();
    }

    public Uri saveOrUpdate(Context context) {
        UserDao userDao = UserDao.findByUserId(context, userId);
        ContentValues insertValues = new ContentValues();
        insertValues.put(KEY_USER_USERID, userId);
        insertValues.put(KEY_USER_NAME, userName);
        insertValues.put(KEY_USER_MALE, male ? 1 : 0);
        insertValues.put(KEY_USER_ONLINE, online ? 1 : 0);
        insertValues.put(KEY_USER_NEW, newFriend ? 1 : 0);
        insertValues.put(KEY_USER_IS_FRIEND, isFriend ? 1 : 0);
        insertValues.put(KEY_USER_AVATAR_URL, userPhotoUrl);
        _data = getPath();
        insertValues.put(KEY_USER_AVATAR_SMALL, _data);

        if (userDao == null) {
            return context.getContentResolver().insert(USERS_URI, insertValues);
        } else {
            Uri useruri = ContentUris.withAppendedId(USERS_URI, userDao.rowId);
            context.getContentResolver().update(useruri, insertValues, null, null);
            return useruri;
        }

    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setMale(boolean male) {
        this.male = male;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public boolean isNewFriend() {
        return newFriend;
    }

    public long getRowId() {
        return rowId;
    }

    public boolean isFriend() {
        return isFriend;
    }

    public void setIsFriend(boolean fr) {
        isFriend = fr;
    }

    public String getUserPhotoUrl() {
        return userPhotoUrl;
    }

    public String getUserName() {
        return userName;
    }

    public long getUserId() {
        return userId;
    }

    public boolean isMale() {
        return male;
    }

    public boolean isOnline() {
        return online;
    }

    public void setUserPhotoUrl(String  userPhotoUrl) {
        this.userPhotoUrl = userPhotoUrl;
    }
    
    public void updatePhoto(Context ctx, User proto, Uri uri) throws IOException {
        String oldPhotoUrl = userPhotoUrl;
        String newPhotoUrl = proto.getUserPhotoUrl();
        //photo exists and (updated or file was not downloaded)
        if ((newPhotoUrl != null && !newPhotoUrl.equalsIgnoreCase(oldPhotoUrl) || !UserapiProvider.isExists(getPath()))) {
            Log.d(TAG, "saving photo: " + newPhotoUrl);
            byte[] photo = proto.getUserPhoto();
            OutputStream os = ctx.getContentResolver().openOutputStream(uri);
            os.write(photo);
            os.close();
        }
    }
    
    private String getPath() {
    	return UserapiProvider.APP_DIR + "profiles/id" + userId + ".smallava";
    }
}