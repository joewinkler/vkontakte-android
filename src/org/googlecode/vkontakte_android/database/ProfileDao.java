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
import android.net.Uri;

public class ProfileDao {
	private static final String TAG = "ProfileDao";
	
	public long rowid;
    public long id;
    public String firstname;
    public String surname;
    public String status;  //TODO make Status
    public int sex;
    public Long birthday;
    public String phone;
    
    public ProfileDao(Cursor c) {
    	rowid = c.getLong(0);
    	id = c.getLong(1);
    	firstname = c.getString(2);
    	surname = c.getString(3);
    	status = c.getString(4);
    	sex = c.getInt(6);
    	birthday = c.getLong(7);
    	phone = c.getString(8);
    }
    
    public ProfileDao(long id, String fn, String sn, String st, 
    		          int sex, Date bd, String phone) {
    	this.id = id;
    	this.firstname = fn;
    	this.surname = sn;
    	this.status = st;
    	this.sex = sex;
    	this.birthday = (bd==null)? 0 : bd.getTime();
    	this.phone = phone;
    }
    
    
    public Uri saveOrUpdate(Context context) {
        ProfileDao profile = ProfileDao.findByUserId(context, id) ;
        ContentValues insertValues = new ContentValues();
        insertValues.put(KEY_PROFILE_USER, this.id);
        insertValues.put(KEY_PROFILE_FIRSTNAME, this.firstname);
        insertValues.put(KEY_PROFILE_SURNAME, this.surname);
        insertValues.put(KEY_PROFILE_STATUS, this.status);
        insertValues.put(KEY_PROFILE_SEX, this.sex);
        insertValues.put(KEY_PROFILE_BIRTHDAY, this.birthday);
        insertValues.put(KEY_PROFILE_PHONE, this.phone);
        
        if (profile == null) {
        	//TODO updating "_data"
        	insertValues.put("_data", UserapiProvider.APP_DIR+"photos/id" + this.id + ".ava");
        	return context.getContentResolver().insert(UserapiProvider.PROFILES_URI, insertValues);
        } else {
        	Uri uri = ContentUris.withAppendedId(UserapiProvider.PROFILES_URI, profile.rowid);
            context.getContentResolver().update(uri, insertValues, null, null);
            return uri;
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
