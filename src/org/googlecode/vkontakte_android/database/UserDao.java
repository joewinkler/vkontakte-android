package org.googlecode.vkontakte_android.database;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import static org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper.*;
import static org.googlecode.vkontakte_android.provider.UserapiProvider.USERS_URI;
import org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper;

import java.util.List;

public class UserDao extends org.googlecode.userapi.User {
    private static final String TAG = "org.googlecode.vkontakte_android.database.UserDao";


    private long rowId;
    //    private String userPhotoUrl;
    //    private String userPhotoUrlSmall;
    private long photo;
    private long photoSmall;
    private boolean newFriend;


    public UserDao(Cursor cursor) {
        rowId = cursor.getLong(0);
        userId = cursor.getLong(1);
        userName = cursor.getString(2);
        male = cursor.getInt(3) == 1;
        online = cursor.getInt(4) == 1;
        newFriend = cursor.getInt(5) == 1;
    }

    public UserDao(long userId, String userName, boolean male, boolean online, boolean newFriend) {
        this.userId = userId;
        this.userName = userName;
        this.male = male;
        this.online = online;
        this.newFriend = newFriend;
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

    public static UserDao findByUserId(Context context, long id) {
        if (id == -1) return null;
        Cursor cursor = context.getContentResolver().query(USERS_URI, null, KEY_USER_USERID + "=?", new String[]{String.valueOf(id)}, null);
        UserDao userDao = null;
        if (cursor != null && cursor.moveToNext()) {
            userDao = new UserDao(cursor);
            cursor.close();
        } else if (cursor != null) cursor.close();
        return userDao;
    }

    public void saveOrUpdate(Context context) {
        UserDao channel = UserDao.findByUserId(context, userId);
        ContentValues insertValues = new ContentValues();
        insertValues.put(KEY_USER_USERID, getUserId());
        insertValues.put(KEY_USER_NAME, getUserName());
        insertValues.put(KEY_USER_MALE, isMale() ? 1 : 0);
        insertValues.put(KEY_USER_ONLINE, isOnline() ? 1 : 0);
        insertValues.put(KEY_USER_NEW, isNewFriend() ? 1 : 0);
        if (channel == null) {
            context.getContentResolver().insert(USERS_URI, insertValues);
        } else {
            context.getContentResolver().update(ContentUris.withAppendedId(USERS_URI, channel.rowId), insertValues, null, null);
        }
    }
//
//    public static void bulkUpdate(Context context, List<Channel> channels) {
//        List<Long> oldChannels = new ArrayList<Long>();
//        Cursor oldChannelsCursor = context.getContentResolver().query(USERS_URI, new String[]{KEY_USER_ID}, null, null, null);
//        if (oldChannelsCursor != null) {
//            while (oldChannelsCursor.moveToNext()) {
//                oldChannels.add(oldChannelsCursor.getLong(0));
//            }
//            oldChannelsCursor.close();
//        }
//        List<Long> newChannels = new ArrayList<Long>();
//        SQLiteDatabase tvDatabase = new TvDatabaseHelper(context).getWritableDatabase();
//        for (Channel channel : channels) {
//            Channel old = Channel.findById(context, channel.getId());
//            ContentValues insertValues = new ContentValues();
//            newChannels.add(channel.getId());
//            insertValues.put(KEY_USER_ID, channel.getId());
//            insertValues.put(KEY_USER_DESC, channel.getDesc());
//            insertValues.put(KEY_USER_NAME, channel.getName());
//            insertValues.put(KEY_USER_LANG, channel.getLang());
//            insertValues.put(KEY_USER_FREE, channel.isFree());
////            insertValues.put(KEY_USER_ENABLED, channel.isEnabled()); //todo: don't change enabled state on update?
//            insertValues.put(KEY_USER_LOGO1, channel.getLogo74());
//            insertValues.put(KEY_USER_PREVIEW1, channel.getPreview160());
//            insertValues.put(KEY_USER_URL1, channel.getUrl());
//            insertValues.put(KEY_USER_URL2, channel.getUrlLo());
//            insertValues.put(KEY_USER_PACKAGE_NAME, channel.getPackageName());
//            if (old != null) {
//                channel.updateWithoutNotify(insertValues, old.getRowId(), tvDatabase);
//            } else {
//                Log.w("new channel:", channel.getName());
//                insertValues.put(KEY_USER_POSITION, getMaxPosition(context) + 1);
//                insertValues.put(KEY_USER_ENABLED, true);
//                channel.insertWithoutNotify(insertValues, tvDatabase);
//            }
//        }
//        oldChannels.removeAll(newChannels);
//        Log.w("channels removed from catalog:", Arrays.toString(oldChannels.toArray()));
//        for (Long id : oldChannels) {
//            tvDatabase.delete(DATABASE_USERS_TABLE, KEY_USER_ID + "=?", new String[]{String.valueOf(id)});
//        }
//        tvDatabase.close();
//        context.getContentResolver().notifyChange(UserapiProvider.USERS_URI, null);
//    }
//
//    public void updateWithoutNotify(ContentValues contentValues, long rowId, SQLiteDatabase tvDatabase) {
//        tvDatabase.update(DATABASE_USERS_TABLE, contentValues, KEY_USER_ROWID + "=?", new String[]{String.valueOf(rowId)});
//    }
//
//    public void insertWithoutNotify(ContentValues contentValues, SQLiteDatabase tvDatabase) {
//        tvDatabase.insert(DATABASE_USERS_TABLE, KEY_USER_ROWID, contentValues);
//    }
//


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
}