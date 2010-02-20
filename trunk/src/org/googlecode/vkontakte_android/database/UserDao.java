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
import java.util.Iterator;
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
    public static void synchronizeFriends(Context context, List<User> users, UserTypes type) {
        //TODO: Optimize online user sync
        Log.v(TAG, "Synchronizing friends: " + type);

        boolean friend = true;
        boolean newFriend = false;
        if (type == UserTypes.NEW_FRIENDS) {
            friend = false;
            newFriend = true;
        }

        ContentResolver resolver = context.getContentResolver();
        String[] projection = new String[]{KEY_USER_ID, KEY_USER_NAME, KEY_USER_AVATAR_URL, KEY_USER_ONLINE};
        Cursor cursor = resolver.query(USERS_URI, projection, null, null, KEY_USER_ID);

        int toDeleteCount = 0;

        StringBuilder deleteList = null;
        ArrayList<ContentProviderOperation> updateList = null;
        ArrayList<ContentValues> addList = null;

        User userInDb = null;
        User userFromResp = null;

        Iterator<User> usersIt = users.iterator();
        // While cursor and iterator has more user objects
        do {
            // Taking next user object from DB if needed
            if (userInDb == null && cursor != null && cursor.moveToNext())
                userInDb = makeLite(cursor);
            else
                userInDb = null;

            // Taking next user object from userapi response if needed
            if (userFromResp == null && usersIt.hasNext())
                userFromResp = usersIt.next();
            else
                userFromResp = null;

            // If userapi response doesn't have user that we have in DB
            if (userInDb != null && (userFromResp == null || userFromResp.getUserId() > userInDb.getUserId())) {
                // Skip that case... 
                if (type == UserTypes.NEW_FRIENDS && !userInDb.isNewFriend())
                    userInDb = null;
                else {
                    // Add that user from DB to list for removing or for update if type != FRIENDS
                    ContentValues values = new ContentValues();
                    switch (type) {
                        case FRIENDS:
                            if (userInDb.isFriend()) {
                                if (deleteList == null)
                                    deleteList = new StringBuilder(String.valueOf(userInDb.getUserId()));
                                else
                                    deleteList.append(",").append(userInDb.getUserId());
                                toDeleteCount++;
                            }
                            break;
                        // In such case we should change data in DB, not delete it
                        case ONLINE_FRIENDS:
                            values.put(KEY_USER_ONLINE, false);
                            break;
                        case NEW_FRIENDS:
                            values.put(KEY_USER_NEW_FRIEND, false);
                            break;
                    }
                    if (values.size() > 0) {
                        if (updateList == null)
                            updateList = new ArrayList<ContentProviderOperation>();
                        // Building update operation
                        Uri userUri = ContentUris.withAppendedId(USERS_URI, userInDb.getUserId());
                        ContentProviderOperation operation = ContentProviderOperation.newUpdate(userUri).
                                withValues(values).build();
                        // Adding that operation to update list for batch
                        updateList.add(operation);
                    }
                    // User is processed
                    userInDb = null;
                }
            }
            // If userapi response have user that we don't have in DB
            else if (userFromResp != null && (userInDb == null || userFromResp.getUserId() < userInDb.getUserId())) {
                // Add that user to DB
                if (addList == null)
                    addList = new ArrayList<ContentValues>();
                // Setting all fields
                userFromResp.setMale(true);
                userFromResp.setFriend(friend);
                userFromResp.setNewFriend(newFriend);
                addList.add(makeContentValues(userFromResp));
                // User is processed
                userFromResp = null;
            }
            // If userapi response have user that we have too and that user differs
            else if (userInDb != null && userFromResp != null && userInDb.getUserId() == userFromResp.getUserId() &&
                (userInDb.isOnline() != userFromResp.isOnline() || !userInDb.getUserPhotoUrl().equals(userFromResp.getUserPhotoUrl()) ||
                 !userInDb.getUserName().equals(userFromResp.getUserName()) || userInDb.isFriend() != userFromResp.isFriend() ||
                    userInDb.isNewFriend() != userFromResp.isNewFriend())) {
                // Add that user to update list
                if (updateList == null)
                    updateList = new ArrayList<ContentProviderOperation>();
                // Building update operation
                Uri userUri = ContentUris.withAppendedId(USERS_URI, userFromResp.getUserId());
                ContentProviderOperation operation = ContentProviderOperation.newUpdate(userUri).
                        withValues(makeContentValuesLite(userFromResp)).build();
                // Adding that operation to update list for batch
                updateList.add(operation);
                // User is processed
                userInDb = null;
                userFromResp = null;
            }

        } while ((cursor != null && !cursor.isAfterLast()) || usersIt.hasNext());

        // Apply changes to DB
        if (deleteList != null) {
            resolver.delete(USERS_URI, KEY_USER_ID + " IN (" + deleteList.toString() + ")", null);
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

    private static ContentValues makeContentValues(User user) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_USER_ID, user.getUserId());
        contentValues.put(KEY_USER_NAME, user.getUserName());
        contentValues.put(KEY_USER_MALE, user.isMale());
        contentValues.put(KEY_USER_ONLINE, user.isOnline());
        contentValues.put(KEY_USER_NEW_FRIEND, user.isNewFriend());
        contentValues.put(KEY_USER_IS_FRIEND, user.isFriend());
        contentValues.put(KEY_USER_AVATAR_URL, user.getUserPhotoUrl());
        contentValues.put(KEY_USER_AVATAR_SMALL, user.getUserPhotoUrlSmall());
        return contentValues;
    }

    private static ContentValues makeContentValuesLite(User user) {
        ContentValues contentValues = new ContentValues();
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