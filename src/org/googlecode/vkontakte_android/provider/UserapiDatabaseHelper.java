package org.googlecode.vkontakte_android.provider;

import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.content.Context;
import android.util.Log;

public class UserapiDatabaseHelper extends SQLiteOpenHelper {
    public static final String KEY_USER_ROWID = "_id";
    public static final String KEY_USER_USERID = "userid";
    public static final String KEY_USER_NAME = "name";
    public static final String KEY_USER_MALE = "male";
    public static final String KEY_USER_ONLINE = "online";
    public static final String KEY_USER_NEW = "newfriend";

    public static final String KEY_FILE_ROWID = "_id";
    public static final String KEY_FILE_URL = "url";
    public static final String KEY_FILE_DATA = "data";

    public static final String DATABASE_NAME = "userapi";
    private static final int DATABASE_VERSION = 1;

    public static final String DATABASE_USERS_TABLE = "users";
    private static final String DATABASE_USERS_CREATE = "create table " + DATABASE_USERS_TABLE + " (" +
            KEY_USER_ROWID + " integer primary key autoincrement, " +
            KEY_USER_USERID + " long, " +
            KEY_USER_NAME + " text , " +
            KEY_USER_MALE + " int, " +
            KEY_USER_ONLINE + " int, " +
            KEY_USER_NEW + " int " +
            ");";

    public static final String DATABASE_FILES_TABLE = "files";
    private static final String DATABASE_FILES_CREATE = "create table " + DATABASE_FILES_TABLE + " ("
            + KEY_FILE_ROWID + " integer primary key autoincrement, "
            + KEY_FILE_URL + " text, "
            + KEY_FILE_DATA + " blob "
            + ");";

    private static final String TAG = "UserapiDatabaseHelper";

    public UserapiDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_USERS_CREATE);
        db.execSQL(DATABASE_FILES_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_USERS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_FILES_TABLE);
        onCreate(db);
    }
}
