package org.googlecode.vkontakte_android.utils;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;


import org.googlecode.vkontakte_android.CImagesManager;
import org.googlecode.vkontakte_android.ComposeMessageActivity;
import org.googlecode.vkontakte_android.ProfileViewActivity;
import org.googlecode.vkontakte_android.CImagesManager.Icons;
import org.googlecode.vkontakte_android.database.UserDao;
import org.googlecode.vkontakte_android.provider.UserapiProvider;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.HashMap;

import static org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper.KEY_MESSAGE_SENDERID;
import static org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper.KEY_PROFILE_USERID;
import static org.googlecode.vkontakte_android.provider.UserapiProvider.USERS_URI;

/**
 * Created by Ildar Karimov
 * Date: Oct 10, 2009
 */
public class UserHelper {

	@SuppressWarnings("unused")
	private static final String TAG = "VK:UserHelper";
	
    public static HashMap<Long, SoftReference<Bitmap>> bitmapCache = new HashMap<Long, SoftReference<Bitmap>>();
    public static HashMap<Long,Bitmap> bitmapCache2 = new HashMap<Long,Bitmap>();
    private static final int PHOTO_SIZE = 90;
	
    public static void viewProfile(Context context, long userId) {
        Intent intent = new Intent(context, ProfileViewActivity.class);
        intent.putExtra(KEY_PROFILE_USERID, userId);
        context.startActivity(intent);
    }

    public static void sendMessage(Context context, long userId) {
        Intent intent = new Intent(context, ComposeMessageActivity.class);
        intent.putExtra(KEY_MESSAGE_SENDERID, userId);
        context.startActivity(intent);
    }

    //todo: return null in case of FileNotFoundException

    private static Bitmap getPhoto(Context context, long rowId) {
        Uri uri = ContentUris.withAppendedId(USERS_URI, rowId);
        InputStream is = null;
        Bitmap bitmap = null;
        try {
            is = context.getContentResolver().openInputStream(uri);
            bitmap = BitmapFactory.decodeStream(is);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    //todo: log
                }
            }
        }
        return bitmap;
    }

    public static Bitmap getPhotoByUserId(Context context, long userId) {

    	Bitmap bm=null;
    	SoftReference<Bitmap> photoRef = bitmapCache.get(userId);
    	if (photoRef != null) {
    	    bm = photoRef.get();
    	    if(bm==null){
    	    	bitmapCache.remove(userId);
    	    }
    	}
    	if (bm==null){
    		UserDao user = UserDao.get(context, userId);
    		if (user!=null){
    			return getPhotoByUser(context, user);
    		}
    		return CImagesManager.getBitmap(context, Icons.STUB);
    	}else{
        	return bm;    		
    	}
    }

    public static Bitmap getPhotoByUserId2(Context context, long userId) {

    	Bitmap bm=bitmapCache2.get(userId);
    	if (bm==null){
    		UserDao user = UserDao.get(context, userId);
    		if (user!=null){
    			return getPhotoByUser2(context, user);
    		}
    		return CImagesManager.getBitmap(context, Icons.STUB);
    	}else{
        	return bm;    		
    	}
    }
    
    public static Bitmap getPhotoByUser2(Context context, UserDao user) {
        long id = user.getUserId();
        String data = user.getData();
    	Bitmap bm=bitmapCache2.get(id);

    	if (bm==null){
    		if (data != null && UserapiProvider.isExists(data)) {
    			bm=getPhoto(context, id);
    			if (bm!=null){
                    int srcWidth = bm.getWidth();
                    int srcHeight = bm.getHeight();
                    int dstWidth = PHOTO_SIZE;
                    int dstHeight = srcHeight * PHOTO_SIZE / srcWidth;
                    //int dstHeight = srcHeight ;
                    Canvas canvas=new Canvas();

                    Bitmap bitmap = Bitmap.createBitmap( Math.min(PHOTO_SIZE, dstWidth), Math.min(PHOTO_SIZE, dstHeight), Bitmap.Config.RGB_565);

                    canvas.setBitmap(bitmap);

                    canvas.drawBitmap(bm, 0, 0, null);

//                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(bm,dstWidth,dstHeight,true);
                      //bm = Bitmap.createBitmap(bm, 0, 0, Math.min(PHOTO_SIZE, dstWidth), Math.min(PHOTO_SIZE, dstHeight));
    //                croppedBitmap = Bitmap.createBitmap(croppedBitmap, 0, 0, Math.min(PHOTO_SIZE, dstWidth), Math.min(PHOTO_SIZE, dstHeight));

    				bitmapCache2.put(id, bitmap);
    				return bitmapCache2.get(id);
				} else {

					return CImagesManager.getBitmap(context, Icons.STUB);
				}
			} else {
//				try {
//					user.updatePhoto(context);
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}

				return CImagesManager.getBitmap(context, Icons.STUB);
			}
		} else {
    		return bm;
    	}
    }

    
    
    
    
    public static Bitmap getPhotoByUser(Context context, UserDao user) {
        long id = user.getUserId();
        String data = user.getData();
    	Bitmap bm = null;
    	SoftReference<Bitmap> photoRef = bitmapCache.get(id);

    	if (photoRef != null) {
    	    bm = photoRef.get();
    	    if(bm==null){
    	    	bitmapCache.remove(id);
    	    }
    	}
    	if (bm==null){
    		if (data != null && UserapiProvider.isExists(data)) {
    			bm=getPhoto(context, id);
    			if (bm!=null){
    				
                    int srcWidth = bm.getWidth();
                    int srcHeight = bm.getHeight();
                    int dstWidth = PHOTO_SIZE;
                    int dstHeight = srcHeight * PHOTO_SIZE / srcWidth;

                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(bm,dstWidth,dstHeight,true);
                    Bitmap croppedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, Math.min(PHOTO_SIZE, dstWidth), Math.min(PHOTO_SIZE, dstHeight));

    				bitmapCache.put(id, new SoftReference<Bitmap>(croppedBitmap));
    				return bitmapCache.get(id).get();
    			}else{
    				return CImagesManager.getBitmap(context, Icons.STUB);
    			}
    		}else{
    			return CImagesManager.getBitmap(context, Icons.STUB);
    		}
    	}else{
    		return bm;
    	}
    }
}
