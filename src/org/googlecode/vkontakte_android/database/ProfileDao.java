package org.googlecode.vkontakte_android.database;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper;
import org.googlecode.vkontakte_android.provider.UserapiProvider;
import org.googlecode.vkontakte_android.utils.AppHelper;

import java.util.Date;

import static org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper.*;
import static org.googlecode.vkontakte_android.provider.UserapiProvider.PROFILES_URI;

public class ProfileDao {
    private static final String TAG = "VK:ProfileDao";

    public long rowid;
    public long id;
    public String firstname;
    public String surname;
    public String status;  //TODO make Status
    public int sex;
    public Long birthday;
    public String phone;
    public int politicalViews;
    public int familyStatus;
    public String currentCity;
    public int allPhotosCount;
    public int taggedPhotosCount;

    public ProfileDao(Cursor cursor) {
        rowid = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_PROFILE_ROWID));
        id = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_PROFILE_USERID));
        firstname = cursor.getString(cursor.getColumnIndexOrThrow(KEY_PROFILE_FIRSTNAME));
        surname = cursor.getString(cursor.getColumnIndexOrThrow(KEY_PROFILE_SURNAME));
        status = cursor.getString(cursor.getColumnIndexOrThrow(KEY_PROFILE_STATUS));
        sex = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_PROFILE_SEX));
        birthday = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_PROFILE_BIRTHDAY));
        phone = cursor.getString(cursor.getColumnIndexOrThrow(KEY_PROFILE_PHONE));
        politicalViews = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_PROFILE_PV));
        familyStatus = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_PROFILE_FS));
        currentCity = cursor.getString(cursor.getColumnIndexOrThrow(KEY_PROFILE_CURRENT_CITY));
    }

    public ProfileDao(long id, String fn, String sn, String st,
                      int sex, Date bd, String phone, int pv, int fs, String curCity) {
        this.id = id;
        this.firstname = fn;
        this.surname = sn;
        this.status = st;
        this.sex = sex;
        this.birthday = (bd == null) ? 0 : bd.getTime();
        this.phone = phone;
        this.politicalViews = pv;
        this.familyStatus = fs;
        this.currentCity = curCity;
    }


    public Uri saveOrUpdate(Context context) {
        ProfileDao profile = ProfileDao.findByUserId(context, id);
        ContentValues insertValues = new ContentValues();
        insertValues.put(KEY_PROFILE_USERID, this.id);
        insertValues.put(KEY_PROFILE_FIRSTNAME, this.firstname);
        insertValues.put(KEY_PROFILE_SURNAME, this.surname);
        insertValues.put(KEY_PROFILE_STATUS, this.status);
        insertValues.put(KEY_PROFILE_SEX, this.sex);
        insertValues.put(KEY_PROFILE_BIRTHDAY, this.birthday);
        insertValues.put(KEY_PROFILE_PHONE, this.phone);
        insertValues.put(KEY_PROFILE_FS, this.familyStatus);
        insertValues.put(KEY_PROFILE_PV, this.politicalViews);
        insertValues.put(KEY_PROFILE_CURRENT_CITY, this.currentCity);

        if (profile == null) {
            //TODO updating "_data"
            String filename = AppHelper.APP_DIR + "profiles/id" + this.id + ".ava";
            insertValues.put("_data", filename);
            Log.d(TAG, "Writing " + filename);
            return context.getContentResolver().insert(UserapiProvider.PROFILES_URI, insertValues);
        } else {
            Uri uri = ContentUris.withAppendedId(UserapiProvider.PROFILES_URI, profile.rowid);
            context.getContentResolver().update(uri, insertValues, null, null);
            return uri;
        }
    }

    private static ProfileDao findByUserId(Context context, long id) {
        if (id == -1) return null;
        Cursor c = context.getContentResolver().query(PROFILES_URI, null, UserapiDatabaseHelper.KEY_PROFILE_USERID + "=?", new String[]{String.valueOf(id)}, null);
        ProfileDao profile = null;

        if (c != null) {
            if (c.moveToNext()) {
                profile = new ProfileDao(c);
            }
            c.close();
        }
        return profile;
    }

    public int delete(Context context) {
        return context.getContentResolver().delete(ContentUris.withAppendedId(PROFILES_URI, id), null, null);
    }

    public static int delete(Context context, long rowId) {
        return context.getContentResolver().delete(ContentUris.withAppendedId(PROFILES_URI, rowId), null, null);
    }


}
