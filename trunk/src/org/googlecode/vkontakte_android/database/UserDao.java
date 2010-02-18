package org.googlecode.vkontakte_android.database;

import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import org.googlecode.userapi.User;
import org.googlecode.userapi.VkontakteAPI;
import org.googlecode.vkontakte_android.provider.UserapiProvider;
import org.googlecode.vkontakte_android.service.ApiCheckingKit;
import org.googlecode.vkontakte_android.utils.AppHelper;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper.*;
import static org.googlecode.vkontakte_android.provider.UserapiProvider.USERS_URI;

/**
 * Class that representing user entity in DB.
 */
public class UserDao extends User {
    private static final String TAG = "VK:UserDao";

    public static final String SELECT_FRIENDS = KEY_USER_IS_FRIEND + "=1";
    public static final String SELECT_ONLINE_FRIENDS = KEY_USER_IS_FRIEND + "=1 AND " + KEY_USER_ONLINE + "=1";
    public static final String SELECT_NEW_FRIENDS = KEY_USER_NEW_FRIEND + "=1";

    public enum UserTypes {FRIENDS, ONLINE_FRIENDS, NEW_FRIENDS}

    private String data;

    private Context context;

    private UserDao() {
    }

    private UserDao(User user) {
        userId = user.getUserId();
        userName = user.getUserName();
        userPhotoUrl = user.getUserPhotoUrl();
        userPhotoUrlSmall = user.getUserPhotoUrlSmall();
        male = user.isMale();
        online = user.isOnline();
        friend = user.isFriend();
        newFriend = user.isNewFriend();
    }

    private UserDao(JSONArray userInfo, VkontakteAPI api) throws JSONException {
        super(userInfo, api);
    }

    private UserDao(Context context, Cursor cursor) {
        this.context = context;
        userId = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_USER_ID));
        userName = cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_NAME));
        male = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_USER_MALE)) == 1;
        online = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_USER_ONLINE)) == 1;
        newFriend = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_USER_NEW_FRIEND)) == 1;
        friend = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_USER_IS_FRIEND)) == 1;
        userPhotoUrl = cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_AVATAR_URL));
        data = cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_AVATAR_SMALL));
    }

    /**
     * Makes user DB entity from cursor.
     *
     * @param context application context
     * @param cursor cursor from users table
     * @return user DB entity
     */
    public static UserDao make(Context context, Cursor cursor) {
        return new UserDao(context, cursor);
    }

    /**
     * Makes User object from cursor with only such data: KEY_USER_ID, KEY_USER_NAME, KEY_USER_ONLINE,
     * KEY_USER_AVATAR_URL. It returns User object because this data doesn't fully correspond to object in DB.
     *
     * @param cursor cursor from users table
     * @return User object
     */
    public static User makeLite(Cursor cursor) {
        UserDao user = new UserDao();
        user.userId = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_USER_ID));
        user.userName = cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_NAME));
        user.online = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_USER_ONLINE)) == 1;
        user.userPhotoUrl = cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_AVATAR_URL));

        return user;
    }

    /**
     * Returns user entity by it's ID.
     *
     * @param context application context
     * @param id user's ID
     * @return user DB entity
     */
    public static UserDao get(Context context, long id) {
        if (id == -1)
            return null;

        Cursor cursor = context.getContentResolver().query(ContentUris.withAppendedId(USERS_URI, id), null, null, null, null);
        if (cursor == null)
            return null;

        try {
            if (cursor.moveToNext())
                return new UserDao(context, cursor);
            else
                return null;
        }
        finally {
            cursor.close();
        }
    }

    /**
     * Adds user to DB.
     *
     * @param context application context
     * @param user user to add
     * @return DB entity of just added user
     */
    public static UserDao insert(Context context, User user) {
        ContentValues insertValues = new ContentValues();
        insertValues.put(KEY_USER_ID, user.getUserId());
        insertValues.put(KEY_USER_NAME, user.getUserName());
        insertValues.put(KEY_USER_MALE, user.isMale());
        insertValues.put(KEY_USER_ONLINE, user.isOnline());
        insertValues.put(KEY_USER_NEW_FRIEND, user.isNewFriend());
        insertValues.put(KEY_USER_IS_FRIEND, user.isFriend());
        insertValues.put(KEY_USER_AVATAR_URL, user.getUserPhotoUrl());

        context.getContentResolver().insert(USERS_URI, insertValues);

        UserDao userDao = new UserDao(user);
        userDao.context = context;
        return userDao;
    }

    /**
     * Synchronizes users from Userapi with DB.
     *
     * @param context application context
     * @param users list of users
     * @param type type of users that should be synchronized
     */
    public static void synchronizeAllFriends(Context context, List<User> users, UserTypes type) {
        Log.v(TAG, "Synchronizing friends: " + type);

        String selection = null;
        switch (type) {
            case FRIENDS:
                selection = SELECT_FRIENDS;
                break;
            case ONLINE_FRIENDS:
                selection = SELECT_ONLINE_FRIENDS;
                break;
            case NEW_FRIENDS:
                selection = SELECT_NEW_FRIENDS;
                break;
        }

        ContentResolver resolver = context.getContentResolver();
        String[] projection = new String[]{KEY_USER_ID, KEY_USER_NAME, KEY_USER_AVATAR_URL, KEY_USER_ONLINE};
        Cursor cursor = resolver.query(USERS_URI, projection, selection, null, KEY_USER_ID);

        int toDeleteCount = 0;

        StringBuilder deleteList = null;
        ArrayList<ContentProviderOperation> updateList = null;
        ArrayList<ContentValues> addList = null;

        for (User user : users) {
            User userInDb = null;
            // Trying to find such user in DB
            while (cursor != null && cursor.moveToNext()) {
                userInDb = makeLite(cursor);
                if (userInDb.getUserId() >= user.getUserId())
                    break; // We found corresponding user in DB or user is not in DB yet
                else {
                    // Add that user from DB to list for removing or for update if type != FRIENDS
                    switch (type) {
                        case FRIENDS:
                            if (deleteList == null)
                                deleteList = new StringBuilder(String.valueOf(userInDb.getUserId()));
                            else
                                deleteList.append(",").append(userInDb.getUserId());
                            toDeleteCount++;
                            break;
                        // In such case we should change data in DB, not delete it
                        case ONLINE_FRIENDS:
                            userInDb.setOnline(false);
                        case NEW_FRIENDS:
                            userInDb.setNewFriend(false);
                            if (updateList == null)
                                updateList = new ArrayList<ContentProviderOperation>();
                            // Building update operation
                            Uri userUri = ContentUris.withAppendedId(USERS_URI, user.getUserId());
                            ContentProviderOperation operation = ContentProviderOperation.newUpdate(userUri).
                                    withValues(makeContentValuesLite(userInDb)).build();
                            // Adding that operation to update list for batch
                            updateList.add(operation);
                            break;
                    }
                }
            }

            // If there is no such user in DB
            if (userInDb == null) {
                // Add user
                if (addList == null)
                    addList = new ArrayList<ContentValues>();
                addList.add(makeContentValuesLite(user));
            }
            // Such user already in DB; if it differs from new one
            else if (userInDb.getUserId() == user.getUserId() &&
                (userInDb.isOnline() != user.isOnline() || !userInDb.getUserPhotoUrl().equals(user.getUserPhotoUrl()) ||
                 !userInDb.getUserName().equals(user.getUserName()))) {
                // Add such user to update list
                if (updateList == null)
                    updateList = new ArrayList<ContentProviderOperation>();
                // Building update operation
                Uri userUri = ContentUris.withAppendedId(USERS_URI, user.getUserId());
                ContentProviderOperation operation = ContentProviderOperation.newUpdate(userUri).
                        withValues(makeContentValuesLite(user)).build();
                // Adding that operation to update list for batch
                updateList.add(operation);
            }
        }

        // Apply changes to DB
        if (deleteList != null) {
            resolver.delete(USERS_URI, KEY_USER_ID + " IN (" + deleteList.toString() + ") AND " + selection, null);
            Log.d(TAG, "Deleted users: " + toDeleteCount);
        }
        if (updateList != null) {
            try {
                resolver.applyBatch(AppHelper.AUTHORITY, updateList);
                Log.d(TAG, "Updated users: " + updateList.size());
            }
            catch (Exception e) {
                Log.e(TAG, "Something wrong with batch for updating users", e);
            }
        }
        if (addList != null) {
            ContentValues[] valuesArr = new ContentValues[addList.size()];
            addList.toArray(valuesArr);
            resolver.bulkInsert(USERS_URI, valuesArr);
            Log.d(TAG, "Added users: " + valuesArr.length);
        }

        if (cursor != null)
            cursor.close();

        if (deleteList != null || updateList != null || addList != null)
            resolver.notifyChange(USERS_URI, null);
    }

    private static ContentValues makeContentValuesLite(User user) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_USER_ID, user.getUserId());
        contentValues.put(KEY_USER_NAME, user.getUserName());
        contentValues.put(KEY_USER_AVATAR_URL, user.getUserPhotoUrl());
        contentValues.put(KEY_USER_ONLINE, user.isOnline());
        return contentValues;
    }

    /**
     * Updates any changes of this entity to DB.
     */
    public void update() {
        ContentValues insertValues = new ContentValues();
        insertValues.put(KEY_USER_NAME, userName);
        insertValues.put(KEY_USER_MALE, male);
        insertValues.put(KEY_USER_ONLINE, online);
        insertValues.put(KEY_USER_NEW_FRIEND, newFriend);
        insertValues.put(KEY_USER_IS_FRIEND, friend);
        insertValues.put(KEY_USER_AVATAR_URL, userPhotoUrl);

        context.getContentResolver().update(ContentUris.withAppendedId(USERS_URI, userId), insertValues, null, null);
    }

    /**
     * Deletes this entity from DB.
     */
    public void delete() {
        context.getContentResolver().delete(ContentUris.withAppendedId(USERS_URI, userId), null, null);
    }

    public static Uri saveOrUpdate(Context context, User user) {
        return new UserDao(user).saveOrUpdate(context);
    }

    //todo: remove this method
    private Uri saveOrUpdate(Context context) {
        UserDao userDao = UserDao.get(context, userId);
        ContentValues insertValues = new ContentValues();
        insertValues.put(KEY_USER_ID, userId);
        insertValues.put(KEY_USER_NAME, userName);
        insertValues.put(KEY_USER_MALE, male ? 1 : 0);
        insertValues.put(KEY_USER_ONLINE, online ? 1 : 0);
        insertValues.put(KEY_USER_NEW_FRIEND, newFriend ? 1 : 0);
        insertValues.put(KEY_USER_IS_FRIEND, friend ? 1 : 0);
        insertValues.put(KEY_USER_AVATAR_URL, userPhotoUrl);

        if (userDao == null) {
            return context.getContentResolver().insert(USERS_URI, insertValues);
        } else {
            Uri useruri = ContentUris.withAppendedId(USERS_URI, userDao.userId);
            context.getContentResolver().update(useruri, insertValues, null, null);
            return useruri;
        }

    }

    public synchronized void updatePhoto(Context ctx) throws IOException {
        if (this.userPhotoUrl == null) {
            this.userPhotoUrl = User.STUB_URL;
        }

        ContentValues insertValues = new ContentValues();
        this.data = getPath();
        insertValues.put(KEY_USER_AVATAR_SMALL, data);
        Uri uri = Uri.withAppendedPath(UserapiProvider.USERS_URI, String.valueOf(userId));
		if (1 == ctx.getContentResolver().update(uri, insertValues, null, null)) {
			Log.d(TAG, "Updating photo of " + this.userName + " "+ this.userPhotoUrl);
			byte[] photo = null;

			try {
				photo = ApiCheckingKit.getApi().getFileFromUrl(userPhotoUrl);
				OutputStream os = null;
				os = ctx.getContentResolver().openOutputStream(uri);
				if (photo != null) {
					os.write(photo);
					os.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			ctx.getContentResolver().notifyChange(uri, null);
		}
        
    }

    public synchronized void updatePhoto(Context ctx, User proto, Uri uri) throws IOException {
        String oldPhotoUrl = userPhotoUrl;
        String newPhotoUrl = proto.getUserPhotoUrl();

        //photo was updated or file was not downloaded
        if ((newPhotoUrl != null && !newPhotoUrl.equalsIgnoreCase(oldPhotoUrl)) || !UserapiProvider.isExists(getPath())) {

            //initialize _data field
            ContentValues insertValues = new ContentValues();
            data = getPath();
            insertValues.put(KEY_USER_AVATAR_SMALL, data);
            if (1 == ctx.getContentResolver().update(uri, insertValues, null, null)) {
                Log.d(TAG, "Saving photo of " + proto.getUserName() + " " + newPhotoUrl);
                byte[] photo = proto.getUserPhoto();
                OutputStream os = ctx.getContentResolver().openOutputStream(uri);
                os.write(photo);
                os.close();
                ctx.getContentResolver().notifyChange(uri, null);
            }
        }
    }

    private String getPath() {
        return UserapiProvider.APP_DIR + "profiles/id" + userId + ".smallava";
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
    
}