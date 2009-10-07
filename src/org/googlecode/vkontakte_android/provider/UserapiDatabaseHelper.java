package org.googlecode.vkontakte_android.provider;

import java.util.Date;

import org.googlecode.userapi.Status;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class UserapiDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "UserapiDatabaseHelper";
	
	public static final String KEY_USER_ROWID = BaseColumns._ID;
    public static final String KEY_USER_USERID = "userid";
    public static final String KEY_USER_NAME = "name";
    public static final String KEY_USER_MALE = "male";
    public static final String KEY_USER_ONLINE = "online";
    public static final String KEY_USER_NEW = "newfriend";

    public static final String KEY_MESSAGE_ROWID = BaseColumns._ID;
    public static final String KEY_MESSAGE_MESSAGEID = "messageid";
    public static final String KEY_MESSAGE_DATE = "date";
    public static final String KEY_MESSAGE_TEXT = "text";
    public static final String KEY_MESSAGE_SENDERID = "senderid";
    public static final String KEY_MESSAGE_RECEIVERID = "receiverid";
    public static final String KEY_MESSAGE_READ = "read";

    public static final String KEY_FILE_ROWID = BaseColumns._ID;
    public static final String KEY_FILE_URL = "url";
    public static final String KEY_FILE_DATA = "data";

    public static final String KEY_WALL_ROWID = BaseColumns._ID;
    public static final String KEY_WALL_TEXT = "text";
    public static final String KEY_WALL_SENDERID = "senderid";
    public static final String KEY_WALL_PIC = "pic";
    public static final String KEY_WALL_DATA = "data";
    
    public static final String KEY_PROFILE_ROWID = BaseColumns._ID;
    public static final String KEY_PROFILE_USER = "userid";
    public static final String KEY_PROFILE_FIRSTNAME = "firstname";
    public static final String KEY_PROFILE_SURNAME = "surname";
    public static final String KEY_PROFILE_STATUS = "status";
    public static final String KEY_PROFILE_SEX = "sex";
    public static final String KEY_PROFILE_BIRTHDAY = "birthday";
    public static final String KEY_PROFILE_PHONE = "phone";    
    public static final String KEY_PROFILE_PHOTO = "_data";
    
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

    public static final String DATABASE_MESSAGES_TABLE = "messages";
    private static final String DATABASE_MESSAGES_CREATE = "create table " + DATABASE_MESSAGES_TABLE + " (" +
            KEY_MESSAGE_ROWID + " integer primary key autoincrement, " +
            KEY_MESSAGE_MESSAGEID + " long, " +
            KEY_MESSAGE_DATE + " long, " +
            KEY_MESSAGE_TEXT + " text , " +
            KEY_MESSAGE_SENDERID + " long, " +
            KEY_MESSAGE_RECEIVERID + " long, " +
            KEY_MESSAGE_READ + " int " +
            ");";

    public static final String DATABASE_FILES_TABLE = "files";
    private static final String DATABASE_FILES_CREATE = "create table " + DATABASE_FILES_TABLE + " ("
            + KEY_FILE_ROWID + " integer primary key autoincrement, "
            + KEY_FILE_URL + " text, "
            + KEY_FILE_DATA + " blob "
            + ");";

    public static final String DATABASE_WALL_TABLE = "wall";
    private static final String DATABASE_WALL_CREATE = "create table " + DATABASE_WALL_TABLE + " ("
            + KEY_WALL_ROWID + " integer primary key autoincrement, "
            + KEY_MESSAGE_SENDERID + " long"
            + KEY_WALL_TEXT + " text, "
            + KEY_WALL_PIC + " blob "
            + ");";

    public static final String DATABASE_PROFILE_TABLE = "profiles";
    private static final String DATABASE_PROFILE_CREATE = "create table " + DATABASE_PROFILE_TABLE + " ("
            + KEY_PROFILE_ROWID + " integer primary key autoincrement, "
            + KEY_PROFILE_USER + " long,"
            + KEY_PROFILE_FIRSTNAME + " text, "
            + KEY_PROFILE_SURNAME + " text, "
            + KEY_PROFILE_STATUS + " text, "
            + KEY_PROFILE_SEX + " long,"
            + KEY_PROFILE_BIRTHDAY + " long,"
            + KEY_PROFILE_PHONE + " text,"
            + KEY_PROFILE_PHOTO + " text"
            + ");";
 
    
    public UserapiDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_USERS_CREATE);
        db.execSQL(DATABASE_FILES_CREATE);
        db.execSQL(DATABASE_MESSAGES_CREATE);
        db.execSQL(DATABASE_PROFILE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_USERS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_FILES_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_MESSAGES_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_PROFILE_CREATE);
        onCreate(db);
    }
}
