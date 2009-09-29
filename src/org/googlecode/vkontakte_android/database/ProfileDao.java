package org.googlecode.vkontakte_android.database;

import static org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper.*;
import static org.googlecode.vkontakte_android.provider.UserapiProvider.*;


import java.util.Date;
import java.util.List;

import org.googlecode.userapi.Status;
import org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper;
import org.googlecode.vkontakte_android.provider.UserapiProvider;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class ProfileDao {
	private static final String TAG = "ProfileDao";
	
	protected long rowid;
    protected long id;
    protected String firstname;
    protected String surname;
    protected String status;  //TODO make Status
    protected String photo;
    protected int sex;
    protected Long birthday;
    protected String phone;
    
    public ProfileDao(Cursor c) {
    	rowid = c.getLong(0);
    	id = c.getLong(1);
    	firstname = c.getString(2);
    	surname = c.getString(3);
    	status = c.getString(4);
    	photo = c.getString(5);
    	sex = c.getInt(6);
    	birthday = c.getLong(7);
    	phone = c.getString(8);
    }
    
    public ProfileDao(long id, String fn, String sn, String st, 
    		          String photo, int sex, Date bd, String phone) {
    	this.id = id;
    	this.firstname = fn;
    	this.surname = sn;
    	this.status = st;
    	this.photo = photo;
    	this.sex = sex;
    	this.birthday = bd.getTime();
    	this.phone = phone;
    }
    
    public int saveOrUpdate(Context context) {
        ProfileDao profile = ProfileDao.findByUserId(context, id) ;
        ContentValues insertValues = new ContentValues();
        insertValues.put(KEY_PROFILE_USER, this.id);
        insertValues.put(KEY_PROFILE_FIRSTNAME, this.birthday);
        insertValues.put(KEY_PROFILE_SURNAME, this.surname);
        insertValues.put(KEY_PROFILE_STATUS, this.status);
        insertValues.put(KEY_PROFILE_PHOTO, this.photo);
        insertValues.put(KEY_PROFILE_SEX, this.sex);
        insertValues.put(KEY_PROFILE_BIRTHDAY, this.birthday);
        insertValues.put(KEY_PROFILE_PHONE, this.phone);
        
        if (profile == null) {
            context.getContentResolver().insert(UserapiProvider.PROFILES_URI, insertValues);
            return 1;
        } else {
            context.getContentResolver().update(ContentUris.withAppendedId(UserapiProvider.PROFILES_URI, profile.rowid), insertValues, null, null);
            return 0;
        }
    }

	private static ProfileDao findByUserId(Context context, long id) {
		if (id == -1) return null;
		Cursor c = context.getContentResolver().query(PROFILES_URI, null, UserapiDatabaseHelper.KEY_PROFILE_USER+"=?", new String[]{String.valueOf(id)}, null);
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
