package org.googlecode.vkontakte_android.service;

import java.io.IOException;
import java.util.Map;
import java.util.List;
import java.util.LinkedList;
import java.util.Collections;

import org.googlecode.vkontakte_android.service.ApiCheckingKit.UpdateType;
import org.googlecode.vkontakte_android.database.UserDao;
import org.googlecode.vkontakte_android.provider.UserapiProvider;
import org.googlecode.userapi.Message;
import org.googlecode.userapi.User;
import org.googlecode.userapi.VkontakteAPI;
import org.googlecode.userapi.VkontakteAPI.privateMessagesTypes;
import org.json.JSONException;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class CheckingService extends Service {

    private static String TAG = "VK-Service";

    public enum contentToUpdate {
        FRIENDS,
        MESSAGES,
        WALL,
        HISTORY,
        ALL
    }

    private List<Thread> threads = Collections.synchronizedList(new LinkedList<Thread>());
    //boolean m_hasConnection = true;

    private static SharedPreferences s_prefs;

    @Override
    public void onCreate() {
        super.onCreate();
        s_prefs = PreferenceManager.getDefaultSharedPreferences(this);
        try {
            ApiCheckingKit.s_ctx = getApplicationContext();
            ApiCheckingKit.login();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStart(final Intent intent, int startId) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

                contentToUpdate what =
                        contentToUpdate.values()[intent.getIntExtra("action", 4)];
                Log.d(TAG, "updating " + what + " is starting...");
                try {
                    switch (what) {
                        case FRIENDS:
                            updateFriends();
                            break;
                        case WALL:
                            updateWall();
                            break;
                        case MESSAGES:
                            updateMessages();
                            break;
                        case HISTORY:
                            updateHistory();
                            break;
                        default:
                            updateMessages();
                            updateWall();
                            updateFriends();
                            updateHistory();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        threads.add(t);
        t.start();
    }

    private void updateFriends() throws IOException, JSONException {
        Log.d(TAG, "updating friends");
        int[] updated = refreshFriends(ApiCheckingKit.getS_api(), getApplicationContext());
        Log.d(TAG, "removed: " + updated[0] + "; added: " + updated[1]);
        Log.d(TAG, "updating new friends");
        int updatedNew = refreshNewFriends(ApiCheckingKit.getS_api(), getApplicationContext());
        Log.d(TAG, "total new:" + updatedNew);
    }

    private void updateMessages() {
        Log.d(TAG, "updating messages");
        //todo: implement
    }

    private void updateWall() {
        Log.d(TAG, "updating wall");
        // todo: implement
    }

    private void updateHistory() throws IOException, JSONException {
        Log.d(TAG, "updating history");
        ApiCheckingKit kit = ApiCheckingKit.getInstance();
        VkontakteAPI api = ApiCheckingKit.getS_api();
        Map<UpdateType, Long> res = kit.getHistoryUpdates(); // fetch updates from the site
        processMessages(kit, res);
        processFriends(kit, res);
        processPhotoTags(kit, res);
    }

    private int[] refreshFriends(VkontakteAPI api, Context context) throws IOException, JSONException {
        List<UserDao> users = new LinkedList<UserDao>();
        List<User> friends = api.getMyFriends();
        Log.d(TAG, "got users: " + friends.size());
        for (User user : friends) {
            UserDao userDao = new UserDao(user.getUserId(), user.getUserName(), user.isMale(), user.isOnline(), false);
            users.add(userDao);
        }
        return UserDao.bulkUpdateOrRemove(context, users);
    }

    private int refreshNewFriends(VkontakteAPI api, Context context) throws IOException, JSONException {
        List<UserDao> users = new LinkedList<UserDao>();
        List<User> friends = api.getMyNewFriends();
        Log.d(TAG, "got new users: " + friends.size());
        for (User user : friends) {
            UserDao userDao = new UserDao(user.getUserId(), user.getUserName(), user.isMale(), user.isOnline(), true);
            userDao.saveOrUpdate(context);
        }
        context.getContentResolver().notifyChange(UserapiProvider.USERS_URI, null);
        return friends.size();
    }


    private void processMessages(ApiCheckingKit kit, Map<UpdateType, Long> res) {
        long incomingMess = res.get(UpdateType.MESSAGES)
                - kit.getPreviosUnreadMessNum();
        Log.d(TAG, "process messages: "+incomingMess);
        if (incomingMess == 0) //messages count didn't changed since last checking
            return;

        if (incomingMess > 0) // new incoming messages
        {
        	//TODO check this
        	VkontakteAPI api = ApiCheckingKit.getS_api();
        	try {
				List<Message> mess = api.getPrivateMessages(api.id, 0, 1024, privateMessagesTypes.inbox);
				Message last = mess.get(mess.size()-1);
				if (useNotifications())
	                UpdatesNotifier.notifyMessages(getApplicationContext(), incomingMess, last.getSender().getUserId());
	            kit.setPreviosUnreadMessNum(res.get(UpdateType.MESSAGES));
        	
        	} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
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
            //if (useNotifications())
                //UpdatesNotifier.notify(getApplicationContext(), "New friends: " + incomingFr, useSound());
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
            //if (useNotifications())
                //UpdatesNotifier.notify(getApplicationContext(), "New photo tags: " + incomingTags, useSound());
            kit.setPreviosNewPhotoTagsNum(res.get(UpdateType.TAGS));
        } else {
            kit.setPreviosNewPhotoTagsNum(res.get(UpdateType.TAGS));
        }
    }

    // ========= preferences
    static boolean useSound() {

        return s_prefs.getBoolean("sound", true);
    }

    static boolean usePics() {

        return s_prefs.getBoolean("pics", true);
    }

    static boolean useNotifications() {

        return s_prefs.getBoolean("notif", true);
    }

    static int getRefreshTime() {

        return s_prefs.getInt("period", 30);
    }

    private void restartAlarm(int period) {
        //TODO implement
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d("serv", "service stopped");
        try {
            ApiCheckingKit.getS_api().logout();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // stop all running threads
        for (Thread t : threads) {
            if (t.isAlive())
                t.interrupt();
        }
        super.onDestroy();
    }


}


