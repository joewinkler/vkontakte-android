package org.googlecode.vkontakte_android.provider;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import static org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper.*;

public class UserapiProvider extends ContentProvider {
    public static final Uri USERS_URI = Uri.parse("content://org.googlecode.vkontakte_android/users");
    public static final Uri FILES_URI = Uri.parse("content://org.googlecode.vkontakte_android/files");

    private static final int ALL_USERS = 1;
    private static final int SINGLE_USER = 2;
    private static final int ALL_FILES = 3;
    private static final int SINGLE_FILE = 4;

    private static UriMatcher uriMatcher;
    private UserapiDatabaseHelper databaseHelper;
    private SQLiteDatabase tvDatabase;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI("org.googlecode.vkontakte_android", "users", ALL_USERS);
        uriMatcher.addURI("org.googlecode.vkontakte_android", "users/#", SINGLE_USER);
        uriMatcher.addURI("org.googlecode.vkontakte_android", "files", ALL_FILES);
        uriMatcher.addURI("org.googlecode.vkontakte_android", "files/#", SINGLE_FILE);
    }

    public boolean onCreate() {
        Context context = getContext();
        databaseHelper = new UserapiDatabaseHelper(context);
        tvDatabase = databaseHelper.getWritableDatabase();
        return (tvDatabase != null);
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sort) {
        String table;
        String column = null;
        String mySort;
        switch (uriMatcher.match(uri)) {
            case ALL_USERS:
                table = DATABASE_USERS_TABLE;
                mySort = KEY_USER_ROWID;
                break;
            case SINGLE_USER:
                table = DATABASE_USERS_TABLE;
                mySort = KEY_USER_NAME;
                column = KEY_USER_ROWID;
                break;
            case ALL_FILES:
                table = DATABASE_FILES_TABLE;
                mySort = KEY_FILE_ROWID;
                break;
            case SINGLE_FILE:
                table = DATABASE_FILES_TABLE;
                mySort = KEY_FILE_ROWID;
                column = KEY_FILE_ROWID;
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI:" + uri);
        }
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(table);
        String orderBy;
        if (TextUtils.isEmpty(sort)) {
            orderBy = mySort;
        } else {
            orderBy = sort;
        }
        if (column != null) selection = column + "="
                + uri.getPathSegments().get(1)
                + (!TextUtils.isEmpty(selection) ? " AND ("
                + selection + ')' : "");
        Cursor cursor = builder.query(tvDatabase, projection, selection, selectionArgs, null, null, orderBy);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case ALL_USERS:
                return "vnd.android.cursor.dir/vnd.softspb.user";
            case SINGLE_USER:
                return "vnd.android.cursor.item/vnd.softspb.user";
//            case ALL_FILES:
//                return "vnd.android.cursor.dir/vnd.softspb.file";
//            case SINGLE_FILE:
//                return "vnd.android.cursor.item/vnd.softspb.file";
//            case ALL_CASTS:
//                return "vnd.android.cursor.dir/vnd.softspb.casts";
//            case SINGLE_CAST:
//                return "vnd.android.cursor.item/vnd.softspb.casts";
            default:
                throw new IllegalArgumentException("Unsupported URI:" + uri);
        }
    }

    public Uri insert(Uri uri, ContentValues contentValues) {
        String table;
        String column;
        switch (uriMatcher.match(uri)) {
            case ALL_USERS:
                table = DATABASE_USERS_TABLE;
                column = KEY_USER_ROWID;
                break;
//            case ALL_FILES:
//                table = DATABASE_FILES_TABLE;
//                column = KEY_FILE_ROWID;
//                break;
//            case ALL_CASTS:
//                table = DATABASE_CASTS_TABLE;
//                column = KEY_CAST_ROWID;
//                break;
            default:
                throw new IllegalArgumentException("Unsupported URI:" + uri);
        }
        long rowId = tvDatabase.insert(table, column, contentValues);
        if (rowId > 0) {
            Uri result = ContentUris.withAppendedId(uri, rowId);
            getContext().getContentResolver().notifyChange(result, null);
            return result;
        } else return null;
    }

    public int delete(Uri uri, String where, String[] whereArgs) {
        String table;
        String column = null;
        switch (uriMatcher.match(uri)) {
            case ALL_USERS:
                table = DATABASE_USERS_TABLE;
                break;
            case SINGLE_USER:
                table = DATABASE_USERS_TABLE;
                column = KEY_USER_ROWID;
                break;
//            case ALL_FILES:
//                table = DATABASE_FILES_TABLE;
//                break;
//            case SINGLE_FILE:
//                table = DATABASE_FILES_TABLE;
//                column = KEY_FILE_ROWID;
//                break;
//            case ALL_CASTS:
//                table = DATABASE_CASTS_TABLE;
//                break;
//            case SINGLE_CAST:
//                table = DATABASE_CASTS_TABLE;
//                column = KEY_CAST_BEGIN;
//                break;
            default:
                throw new IllegalArgumentException("Unsupported URI:" + uri);
        }
        if (column != null) where = column + "="
                + uri.getPathSegments().get(1)
                + (!TextUtils.isEmpty(where) ? " AND ("
                + where + ')' : "");
        int count = tvDatabase.delete(table, where, whereArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    public int update(Uri uri, ContentValues contentValues, String where, String[] whereArgs) {
        String table;
        String column = null;
        switch (uriMatcher.match(uri)) {
            case ALL_USERS:
                table = DATABASE_USERS_TABLE;
                break;
            case SINGLE_USER:
                table = DATABASE_USERS_TABLE;
                column = KEY_USER_ROWID;
                break;
//            case ALL_FILES:
//                table = DATABASE_FILES_TABLE;
//                break;
//            case SINGLE_FILE:
//                table = DATABASE_FILES_TABLE;
//                column = KEY_FILE_ROWID;
//                break;
//            case ALL_CASTS:
//                table = DATABASE_CASTS_TABLE;
//                break;
//            case SINGLE_CAST:
//                table = DATABASE_CASTS_TABLE;
//                column = KEY_CAST_BEGIN;
//                break;
            default:
                throw new IllegalArgumentException("Unsupported URI:" + uri);
        }
        if (column != null) where = column + "="
                + uri.getPathSegments().get(1)
                + (!TextUtils.isEmpty(where) ? " AND ("
                + where + ')' : "");
        int count = tvDatabase.update(table, contentValues, where, whereArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] contentValueses) {
        tvDatabase.beginTransaction();
        String table;
        String column;
        switch (uriMatcher.match(uri)) {
            case ALL_USERS:
                table = DATABASE_USERS_TABLE;
                column = KEY_USER_ROWID;
                break;
//            case ALL_FILES:
//                table = DATABASE_FILES_TABLE;
//                column = KEY_FILE_ROWID;
//                break;
//            case ALL_CASTS:
//                table = DATABASE_CASTS_TABLE;
//                column = KEY_CAST_ROWID;
//                break;
            default:
                throw new IllegalArgumentException("Unsupported URI:" + uri);
        }
        int count = 0;
        for (ContentValues values : contentValueses) {
            if (values == null) continue;
            long result = tvDatabase.insert(table, column, values);
            if (result != -1) count++;
        }
        tvDatabase.setTransactionSuccessful();
        tvDatabase.endTransaction();
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}