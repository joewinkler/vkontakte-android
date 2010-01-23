package org.googlecode.vkontakte_android.database;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import org.googlecode.userapi.Message;
import org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper;
import org.googlecode.vkontakte_android.provider.UserapiProvider;
import org.googlecode.vkontakte_android.utils.PreferenceHelper;

import java.util.Date;

import static org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper.*;
import static org.googlecode.vkontakte_android.provider.UserapiProvider.MESSAGES_URI;

public class MessageDao extends Message {
    private static final String TAG = "VK:MessageDao";

    public long rowId;
    public long id;
    public long date;
    public String text;
    public long senderId;
    public long receiverId;
    public boolean read;

    private UserDao sender = null;
    private UserDao receiver = null;

    public MessageDao(Cursor cursor) {
        this.rowId = cursor.getLong(0);
        this.id = cursor.getLong(1);
        this.date = cursor.getLong(2);
        this.text = cursor.getString(3);
        this.senderId = cursor.getLong(4);
        this.receiverId = cursor.getLong(5);
        this.read = cursor.getInt(6) == 1;
    }

    /*
     * Use this only when you don't need to save it to DB
     */
    public MessageDao(long id, Date date, String text, long senderId, long receiverId, boolean read) {
        this.id = id;
        this.date = date.getTime();
        this.text = text;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.read = read;
    }

    public MessageDao(Message mess) {
        this.id = mess.getId();
        this.date = mess.getDate().getTime();
        this.text = mess.getText();
        this.senderId = mess.getSender().getUserId();
        this.receiverId = mess.getReceiver().getUserId();
        this.read = mess.isRead();
        sender = new UserDao(mess.getSender(), false, false);
        receiver = new UserDao(mess.getReceiver(), false, false);
    }

//    //TODO add sender/receiver
//    public static int bulkSave(Context context, List<MessageDao> messages) {
//        ContentValues[] values = new ContentValues[messages.size()];
//        int i = 0;
//        for (MessageDao messageDao : messages) {
//            ContentValues insertValues = new ContentValues();
//            insertValues.put(KEY_MESSAGE_MESSAGEID, messageDao.getId());
//            insertValues.put(KEY_MESSAGE_DATE, messageDao.getDate().getTime());
//            insertValues.put(KEY_MESSAGE_TEXT, messageDao.getText());
//            insertValues.put(KEY_MESSAGE_SENDERID, messageDao.getSenderId());
//            insertValues.put(KEY_MESSAGE_RECEIVERID, messageDao.getReceiverId());//todo: save if not exist?
//            insertValues.put(KEY_MESSAGE_READ, messageDao.isRead() ? 0 : 1);
//            values[i] = insertValues;
//            i++;
//        }
//        return context.getContentResolver().bulkInsert(MESSAGES_URI, values);
//    }

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

    public static MessageDao findByMessageId(Context context, long id) {
        if (id == -1) return null;
        Cursor cursor = context.getContentResolver().query(MESSAGES_URI, null, UserapiDatabaseHelper.KEY_MESSAGE_MESSAGEID + "=?", new String[]{String.valueOf(id)}, null);
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

    public int saveOrUpdate(Context context) {
        MessageDao message = MessageDao.findByMessageId(context, id);
        ContentValues insertValues = new ContentValues();
        insertValues.put(KEY_MESSAGE_MESSAGEID, this.id);
        insertValues.put(KEY_MESSAGE_DATE, this.date);
        insertValues.put(KEY_MESSAGE_TEXT, this.text);
        insertValues.put(KEY_MESSAGE_SENDERID, this.senderId);
        insertValues.put(KEY_MESSAGE_RECEIVERID, this.receiverId);
        insertValues.put(KEY_MESSAGE_READ, this.isRead() ? 0 : 1);

        Log.d(TAG, "saving "+sender.userId+"("+sender.userName+") and "+ receiver.userId);
        saveUserIfNeed(context, sender);
        saveUserIfNeed(context, receiver);

        if (message == null) {
            context.getContentResolver().insert(UserapiProvider.MESSAGES_URI, insertValues);
            return 1;
        } else {
            context.getContentResolver().update(ContentUris.withAppendedId(UserapiProvider.MESSAGES_URI, message.rowId), insertValues, null, null);
            return 0;
        }
    }

    
    public long getId(){
    	return id;
    }
    
    
    public long getSenderId() {
        return senderId;
    }

    public long getReceiverId() {
        return receiverId;
    }

    public UserDao getSender(Context ctx) {
        return UserDao.findByUserId(ctx, senderId);
    }

    public UserDao getReceiver(Context ctx) {
        return UserDao.findByUserId(ctx, receiverId);
    }

    public boolean saveUserIfNeed(Context ctx, UserDao user) {
        if (user == null) {
            return false;
        }
        if (user.getUserId() != PreferenceHelper.getMyId(ctx)) {
            user.saveOrUpdate(ctx);
        }
        return true;
    }

}