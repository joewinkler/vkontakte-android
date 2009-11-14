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

/**
 * Some helping functions and variables
 * @author bea
 *
 */
public class ApiCheckingKit {
    private ApiCheckingKit() throws IOException {}
    
    private static final int SITE_ID = 2;
    private static VkontakteAPI s_api = new VkontakteAPI(SITE_ID);
    public static VkontakteAPI getApi() {
        return s_api;
    }
    
    public static HistoryChanges m_histChanges = new HistoryChanges();
    
}

class HistoryChanges {
	    public long prevUnreadMessNum = 0;
	    public long prevFriendshipRequestsNum = 0;
	    public long prevNewPhotoTagsNum = 0;
    }