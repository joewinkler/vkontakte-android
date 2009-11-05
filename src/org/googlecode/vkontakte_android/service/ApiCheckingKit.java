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
    private static String TAG = "ApiCheckingKit";
    private ApiCheckingKit() throws IOException {

    }
    private static VkontakteAPI s_api = new VkontakteAPI();
    private static ApiCheckingKit s_instance;
    public static HistoryChanges m_histChanges = new HistoryChanges();
    
    public static synchronized ApiCheckingKit getInstance() throws IOException {
        if (s_instance == null)
            s_instance = new ApiCheckingKit();
        return s_instance;
    }

    public static VkontakteAPI getApi() {
        return s_api;
    }

    
}

class HistoryChanges {
	    public long prevUnreadMessNum = 0;
	    public long prevFriendshipRequestsNum = 0;
	    public long prevNewPhotoTagsNum = 0;
    }