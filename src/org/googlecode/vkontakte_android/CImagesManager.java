package org.googlecode.vkontakte_android;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class CImagesManager {

	
	//icons collection
	private static Map<String, Bitmap> s_bitmaps = new HashMap<String, Bitmap>();
	
	//have to be called once
	public static void loadImages(Context ctx)
	{
		InputStream is = ctx.getResources().openRawResource(R.drawable.stub);
		s_bitmaps.put("stub", Bitmap.createScaledBitmap(BitmapFactory
				.decodeStream(is), 60, 60, false));
		
		is = ctx.getResources().openRawResource(R.drawable.cancel);
		s_bitmaps.put("cancel", Bitmap.createScaledBitmap(BitmapFactory
				.decodeStream(is), 30, 30, false));
		
		is = ctx.getResources().openRawResource(R.drawable.delete);
		s_bitmaps.put("delete", Bitmap.createScaledBitmap(BitmapFactory
				.decodeStream(is), 20, 20, false));
		
		is = ctx.getResources().openRawResource(R.drawable.send);
		s_bitmaps.put("send", Bitmap.createScaledBitmap(BitmapFactory
				.decodeStream(is), 30, 20, false));
		
		is = ctx.getResources().openRawResource(R.drawable.reply);
		s_bitmaps.put("reply", Bitmap.createScaledBitmap(BitmapFactory
				.decodeStream(is), 40, 30, false));

		is = ctx.getResources().openRawResource(R.drawable.ok);
		s_bitmaps.put("ok", Bitmap.createScaledBitmap(BitmapFactory
				.decodeStream(is), 30, 30, false));

		is = ctx.getResources().openRawResource(R.drawable.right);
		s_bitmaps.put("right", Bitmap.createScaledBitmap(BitmapFactory
				.decodeStream(is), 30, 30, false));

		is = ctx.getResources().openRawResource(R.drawable.left);
		s_bitmaps.put("left", Bitmap.createScaledBitmap(BitmapFactory
				.decodeStream(is), 30, 30, false));

	}

	/*
	 *  name - name of icon, like "ok", "left" and such
	 */
	public static Bitmap getBitmap(String name)
	{
		return s_bitmaps.containsKey(name) ? 
               s_bitmaps.get(name):
		       s_bitmaps.get("stub");
	}
	
}
