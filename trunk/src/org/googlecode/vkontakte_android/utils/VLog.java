package org.googlecode.vkontakte_android.utils;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import android.util.Log;

public class VLog {

	private static Logger logger = Logger.getLogger("org.googlecode.vkontakte_android");
	static {
		FileHandler fh = null;
		try {
			fh = new FileHandler("/sdcard/vk-logging.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}
		fh.setFormatter(new SimpleFormatter());
		logger.addHandler(fh); 
    	logger.setLevel(Level.ALL); 
	}
	
	public static void d(String TAG, String message) {
		Log.d(TAG, message);
		logger.fine(TAG+": "+message);
	}
	
	public static void i(String TAG, String message) {
		Log.i(TAG, message);
		logger.info(TAG+": "+message);
	}
	
	public static void e(String TAG, String message) {
		Log.d(TAG, message);
		logger.severe(TAG+": "+message);
	}
}
