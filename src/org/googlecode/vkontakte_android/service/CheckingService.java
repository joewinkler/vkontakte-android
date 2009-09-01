package org.googlecode.vkontakte_android.service;

import java.io.IOException;
import java.util.Map;
import java.util.List;
import java.util.LinkedList;
import java.util.Collections;

import org.googlecode.vkontakte_android.service.ApiCheckingKit.UpdateType;
import org.googlecode.vkontakte_android.database.UserDao;
import org.googlecode.userapi.User;
import org.googlecode.userapi.VkontakteAPI;
import org.json.JSONException;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class CheckingService extends Service {

    private static final int UPDATE_FRIENDS = 1;
    private List<Thread> threads = Collections.synchronizedList(new LinkedList<Thread>());
    //boolean m_hasConnection = true;

    private static SharedPreferences s_prefs;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        s_prefs = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public void onStart(final Intent intent, int startId) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("serv", "service started");
                switch (intent.getIntExtra("action", -1)) {
                    case UPDATE_FRIENDS:
                        updateFriends();
                        break;
                    default:
                        updateHistory();
                }
            }
        });
        threads.add(t);
        t.start();
    }

    private void updateFriends() {
        //todo: implement
    }

    private void updateHistory() {
        //todo: implement
        try {
            ApiCheckingKit kit = ApiCheckingKit.getInstance();

            VkontakteAPI api = ApiCheckingKit.getS_api();
            int[] updated = refreshFriends(api, getApplicationContext());
            System.out.println("removed:" + updated[0] + "; added:" + updated[1]);
            Map<UpdateType, Long> res = kit.getUpdates(); //fetch updates from site
            processMessages(kit, res);
            processFriends(kit, res);
            processPhotoTags(kit, res);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private int[] refreshFriends(VkontakteAPI api, Context context) throws IOException, JSONException {
        List<UserDao> users = new LinkedList<UserDao>();
        List<User> friends = api.getFriends();
        System.out.println("got users:" + friends.size());
        for (User user : friends) {
            UserDao userDao = new UserDao(user.getUserId(), user.getUserName(), user.isMale(), user.isOnline(), false);
            users.add(userDao);
        }
        return UserDao.bulkUpdateOrRemove(context, users);
    }


    private void processMessages(ApiCheckingKit kit, Map<UpdateType, Long> res) {
        long incomingMess = res.get(UpdateType.MESSAGES)
                - kit.getPreviosUnreadMessNum();

        if (incomingMess == 0) //messages count didn't changed since last checking
            return;

        if (incomingMess > 0) // new incoming messages
        {
            if (useNotifications(this))
                UpdatesNotifier.notify(getApplicationContext(), "New messages: " + incomingMess, useSound(this));
            kit.setPreviosUnreadMessNum(res.get(UpdateType.MESSAGES));
        } else // some messages were read by another way
        {
            kit.setPreviosUnreadMessNum(res.get(UpdateType.MESSAGES));
        }
    }

    private void processFriends(ApiCheckingKit kit, Map<UpdateType, Long> res) {
        long incomingFr = res.get(UpdateType.FRIENDSHIP_REQ)
                - kit.getPreviosFriendshipRequestsNum();

        if (incomingFr == 0)
            return;

        if (incomingFr > 0) {
            if (useNotifications(this))
                UpdatesNotifier.notify(getApplicationContext(), "New friends: " + incomingFr, useSound(this));
            kit.setPreviosFriendshipRequestsNum(res.get(UpdateType.FRIENDSHIP_REQ));
        } else {
            kit.setPreviosFriendshipRequestsNum(res.get(UpdateType.MESSAGES));
        }
    }

    private void processPhotoTags(ApiCheckingKit kit, Map<UpdateType, Long> res) {
        long incomingTags = res.get(UpdateType.TAGS)
                - kit.getPreviosNewPhotoTagsNum();

        if (incomingTags == 0)
            return;

        if (incomingTags > 0) {
            if (useNotifications(this))
                UpdatesNotifier.notify(getApplicationContext(), "New photo tags: " + incomingTags, useSound(this));
            kit.setPreviosNewPhotoTagsNum(res.get(UpdateType.TAGS));
        } else {
            kit.setPreviosNewPhotoTagsNum(res.get(UpdateType.TAGS));
        }
    }

    // ========= preferences
    static boolean useSound(Context ctx) {

        return s_prefs.getBoolean("sound", true);
    }

    static boolean usePics(Context ctx) {

        return s_prefs.getBoolean("pics", true);
    }

    static boolean useNotifications(Context ctx) {

        return s_prefs.getBoolean("notif", true);
    }

    static int getRefreshTime(Context ctx) {

        return s_prefs.getInt("period", 30);
    }

    private void restartAlarm(int period) {
        //TODO implement
    }


    @Override
    public void onDestroy() {
        Log.d("serv", "service stopped");
        //todo: stop all running threads
        super.onDestroy();
    }
}


