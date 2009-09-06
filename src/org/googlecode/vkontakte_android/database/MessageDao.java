package org.googlecode.vkontakte_android.database;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import org.googlecode.userapi.Message;
import static org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper.*;
import static org.googlecode.vkontakte_android.provider.UserapiProvider.MESSAGES_URI;

import java.util.Date;
import java.util.List;

public class MessageDao extends Message {
    private static final String TAG = "org.googlecode.vkontakte_android.database.MessageDao";
    private long rowId;
    private long senderId;
    private long receiverId;

    public MessageDao(Cursor cursor) {
        rowId = cursor.getLong(0);
        id = cursor.getLong(1);
        date = new Date(cursor.getLong(2));
        text = cursor.getString(3);
        senderId = cursor.getLong(4);
        receiverId = cursor.getLong(5);
        read = cursor.getInt(6) == 1;
    }

    public MessageDao(long id, Date date, String text, long senderId, long receiverId, boolean read) {
        this.id = id;
        this.date = date;
        this.text = text;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.read = read;
    }

    public static int bulkSave(Context context, List<MessageDao> messages) {
        ContentValues[] values = new ContentValues[messages.size()];
        int i = 0;
        for (MessageDao messageDao : messages) {
            ContentValues insertValues = new ContentValues();
            insertValues.put(KEY_MESSAGE_MESSAGEID, messageDao.getId());
            insertValues.put(KEY_MESSAGE_DATE, messageDao.getDate().getTime());
            insertValues.put(KEY_MESSAGE_TEXT, messageDao.getText());
            insertValues.put(KEY_MESSAGE_SENDERID, messageDao.getSenderId());
            insertValues.put(KEY_MESSAGE_RECEIVERID, messageDao.getReceiverId());//todo: save if not exist?
            insertValues.put(KEY_MESSAGE_READ, messageDao.isRead() ? 0 : 1);
            values[i] = insertValues;
            i++;
        }
        return context.getContentResolver().bulkInsert(MESSAGES_URI, values);
    }

    public long getSenderId() {
        return senderId;
    }

    public long getReceiverId() {
        return receiverId;
    }

    public static MessageDao get(Context context, long rowId) {
        if (rowId == -1) return null;
        Cursor cursor = context.getContentResolver().query(ContentUris.withAppendedId(MESSAGES_URI, rowId), null, null, null, null);
        MessageDao messageDao = null;
        if (cursor != null && cursor.moveToNext()) {
            messageDao = new MessageDao(cursor);
            cursor.close();
        } else if (cursor != null) cursor.close();
        return messageDao;
    }

    public int delete(Context context) {
        return context.getContentResolver().delete(ContentUris.withAppendedId(MESSAGES_URI, rowId), null, null);
    }

    public static int delete(Context context, long rowId) {
        return context.getContentResolver().delete(ContentUris.withAppendedId(MESSAGES_URI, rowId), null, null);
    }

//    public static MessageDao findAllBySenderOrReceiver(Context context, long userId) {
//        if (userId == -1) return null;
//        Cursor cursor = context.getContentResolver().query(MESSAGES_URI, null, KEY_MESSAGE_SENDERID + "=?" + " OR " + KEY_MESSAGE_RECEIVERID + "=?", new String[]{String.valueOf(userId), String.valueOf(userId)}, null);
//        MessageDao messageDao = null;
//        if (cursor != null && cursor.moveToNext()) {
//            messageDao = new MessageDao(cursor);
//            cursor.close();
//        } else if (cursor != null) cursor.close();
//        return messageDao;
//    }

//    public int saveOrUpdate(Context context) {
//        MessageDao channel = MessageDao.findByUserId(context, userId);
//        ContentValues insertValues = new ContentValues();
//        insertValues.put(KEY_USER_USERID, getUserId());
//        insertValues.put(KEY_USER_NAME, getUserName());
//        insertValues.put(KEY_USER_MALE, isMale() ? 1 : 0);
//        insertValues.put(KEY_USER_ONLINE, isOnline() ? 1 : 0);
//        insertValues.put(KEY_USER_NEW, isNewFriend() ? 1 : 0);
//        if (isNewFriend()) System.out.println("new!");
//        else System.out.println("old :(");
//        if (channel == null) {
//            context.getContentResolver().insert(USERS_URI, insertValues);
//            return 1;
//        } else {
//            context.getContentResolver().update(ContentUris.withAppendedId(USERS_URI, channel.rowId), insertValues, null, null);
//            return 0;
//        }
//    }
}