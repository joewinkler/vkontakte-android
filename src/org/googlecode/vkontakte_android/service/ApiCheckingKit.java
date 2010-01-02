package org.googlecode.vkontakte_android.service;

import org.googlecode.userapi.VkontakteAPI;

import java.io.IOException;

/**
 * Some helping functions and variables
 *
 * @author bea
 */
public class ApiCheckingKit {
    private ApiCheckingKit() throws IOException {
    }

    private static final int SITE_ID = 2;
    private static final VkontakteAPI s_api = new VkontakteAPI(SITE_ID);

    public static VkontakteAPI getApi() {
        return s_api;
    }

    public static final HistoryChanges m_histChanges = new HistoryChanges();

}

class HistoryChanges {
    public long prevUnreadMessNum = 0;
    public long prevFriendshipRequestsNum = 0;
    public long prevNewPhotoTagsNum = 0;
}