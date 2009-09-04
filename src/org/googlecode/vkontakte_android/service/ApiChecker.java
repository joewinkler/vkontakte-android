package org.googlecode.vkontakte_android.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.googlecode.userapi.ChangesHistory;
import org.googlecode.userapi.VkontakteAPI;
import org.googlecode.vkontakte_android.CSettings;
import org.json.JSONException;

import android.content.Context;
import android.util.Log;

/*
 *  A kit of methods that allows to check the account for some updates
 */
class ApiCheckingKit {
 
	private static String TAG = "VK-Service";
	
    //========================================

    private ApiCheckingKit() throws IOException {
    	
         
    }

    private static VkontakteAPI s_api = new VkontakteAPI();
    private static ApiCheckingKit s_instance;

    public static ApiCheckingKit getInstance() throws IOException {
        if (s_instance == null)
            s_instance = new ApiCheckingKit();
        return s_instance;
    }

    //========================================
    public static Context s_ctx; 

    /*
     *  make the instance of service's API to login.
     *  call it before using ApiCheckingKit first time
     */
    public static void login() throws IOException
    {
    	Log.d(TAG, "service is logging...");
        s_api.login(CSettings.getLogin(s_ctx), CSettings.getPass(s_ctx));
    	
    }

    public static VkontakteAPI getS_api() {
        return s_api;
    }

    enum UpdateType {
        MESSAGES, FRIENDSHIP_REQ, TAGS
    }

    static Map<UpdateType, Long> s_updates = new HashMap<UpdateType, Long>();

    public Map<UpdateType, Long> getHistoryUpdates() throws IOException, JSONException {
        s_updates.clear();

        ChangesHistory changes = s_api.getChangesHistory();
        Log.d("apichecker", changes.getMessagesCount() + " " + s_previosUnreadMessNum);
        s_updates.put(UpdateType.MESSAGES, changes.getMessagesCount());
        s_updates.put(UpdateType.FRIENDSHIP_REQ, changes.getFriendsCount());
        s_updates.put(UpdateType.TAGS, changes.getPhotosCount());
        return s_updates;
    }

    public void logout() throws IOException {
        s_api.logout();
    }


    //================================================

    private static long s_previosUnreadMessNum = 0;
    private static long s_previosFriendshipRequestsNum = 0;
    private static long s_previosNewPhotoTagsNum = 0;


    public void setPreviosUnreadMessNum(long n) {
        s_previosUnreadMessNum = n;
    }

    public void setPreviosFriendshipRequestsNum(long n) {
        s_previosFriendshipRequestsNum = n;
    }

    public void setPreviosNewPhotoTagsNum(long n) {
        s_previosNewPhotoTagsNum = n;
    }

    public long getPreviosUnreadMessNum() {
        return s_previosUnreadMessNum;
    }

    public long getPreviosFriendshipRequestsNum() {
        return s_previosFriendshipRequestsNum;
    }

    public long getPreviosNewPhotoTagsNum() {
        return s_previosNewPhotoTagsNum;
    }


}