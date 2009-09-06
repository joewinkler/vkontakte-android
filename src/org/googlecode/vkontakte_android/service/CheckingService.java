package org.googlecode.vkontakte_android.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import org.googlecode.userapi.Message;
import org.googlecode.userapi.User;
import org.googlecode.userapi.VkontakteAPI;
import org.googlecode.vkontakte_android.CSettings;
import org.googlecode.vkontakte_android.database.MessageDao;
import org.googlecode.vkontakte_android.database.UserDao;
import org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper;
import org.googlecode.vkontakte_android.provider.UserapiProvider;
import org.googlecode.vkontakte_android.service.ApiCheckingKit.UpdateType;
import org.json.JSONException;

import java.io.IOException;
import java.util.*;

public class CheckingService extends Service {

    private static String TAG = "VK-Service";

    public enum contentToUpdate {
        FRIENDS, MESSAGES_ALL, MESSAGES_IN, MESSAGES_OUT, WALL, HISTORY, ALL
    }

    private ThreadGroup threads = new ThreadGroup("downloads");
    // boolean m_hasConnection = true;

    private static SharedPreferences s_prefs;

    @Override
    public void onCreate() {
        super.onCreate();
        s_prefs = PreferenceManager.getDefaultSharedPreferences(this);
        try {
            ApiCheckingKit.s_ctx = getApplicationContext();
            ApiCheckingKit.login();
            restartScheduledUpdates();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
      * starts a thread checking api periodically
      */
    Thread m_periodCheckingThread;
    Timer m_timer = new Timer();

    private void restartScheduledUpdates() {
        try {
            //m_timer.cancel();
        } catch (IllegalStateException ex) {
            Log.d(TAG, "Timer has been tried to cancel");
        }
        final Intent in = new Intent().putExtra("action",
                contentToUpdate.MESSAGES_ALL.ordinal());
        class CheckingTask extends TimerTask {
            @Override
            public void run() {
                Log.d(TAG, "checking by timer");
                doCheck(in);
            }
        }
        int period = CSettings.getPeriod(getApplicationContext());
        m_timer.scheduleAtFixedRate(new CheckingTask(), 0L, 1000 * 5);
        Log.d(TAG, "Timer with period: " + period);
    }

    public void doCheck(Intent in) {

        contentToUpdate what = contentToUpdate.values()[in.getIntExtra("action", 1)];
        Log.d(TAG, "updating " + what + " is starting...");
        try {
            switch (what) {
                case FRIENDS:
                    updateFriends();
                    break;
                case WALL:
                    updateWall();
                    break;
                case MESSAGES_ALL:  //TODO count from Intent
                    updateInMessages(100);
                    updateOutMessages(100);

                    break;
                case MESSAGES_IN:
                    updateInMessages(100);
                    break;
                case MESSAGES_OUT:
                    updateOutMessages(1); //should be called when user sends messages
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

    // =============== updating methods

    private void updateInMessages(int count) throws IOException, JSONException {
        VkontakteAPI api = ApiCheckingKit.getS_api();
        List<Message> messages = api.getInbox(0, count);
        for (Message m : messages) {
            MessageDao md = new MessageDao(m.getId(), m.getDate(), m.getText(), m.getSender().getUserId(), m.getReceiver().getUserId(), m.isRead());
            md.saveOrUpdate(this);
        }
        Cursor cursor = this.getContentResolver().query(UserapiProvider.MESSAGES_URI, null,
                UserapiDatabaseHelper.KEY_MESSAGE_READ + "=?",
                new String[]{"0"},
                null);
        int countNew = 0;
        MessageDao single = null;
        if (cursor != null) {

            countNew = cursor.getCount();
            if (countNew == 1) {
                cursor.moveToNext();
                single = new MessageDao(cursor);
            }
            cursor.close();
        }

        UpdatesNotifier.notifyMessages(this, countNew, single);
        getContentResolver().notifyChange(UserapiProvider.MESSAGES_URI, null);
        //TODO get real counter from provider
    }

    private void updateOutMessages(int count) throws IOException, JSONException {
        VkontakteAPI api = ApiCheckingKit.getS_api();
        api.getOutbox(0, count);
        getContentResolver().notifyChange(UserapiProvider.MESSAGES_URI, null);
    }


    private void updateFriends() throws IOException, JSONException {
        Log.d(TAG, "updating friends");
        int[] updated = refreshFriends(ApiCheckingKit.getS_api(),
                getApplicationContext());
        Log.d(TAG, "removed: " + updated[0] + "; added: " + updated[1]);
        Log.d(TAG, "updating new friends");
        int updatedNew = refreshNewFriends(ApiCheckingKit.getS_api(),
                getApplicationContext());
        Log.d(TAG, "total new:" + updatedNew);
    }

    private void updateMessages() {
        Log.d(TAG, "updating messages");
        // todo: implement
    }

    private void updateWall() {
        Log.d(TAG, "updating wall");
        // todo: implement
    }

    private void updateHistory() throws IOException, JSONException {
        Log.d(TAG, "updating history");
        ApiCheckingKit kit = ApiCheckingKit.getInstance();
        VkontakteAPI api = ApiCheckingKit.getS_api();
        Map<UpdateType, Long> res = kit.getHistoryUpdates(); // fetch updates
        // from the site
//		processMessages(kit, res);
//		processFriends(kit, res);
//		processPhotoTags(kit, res);
    }

    private int[] refreshFriends(VkontakteAPI api, Context context)
            throws IOException, JSONException {

        // toDo replace

        List<UserDao> users = new LinkedList<UserDao>();
        List<User> friends = api.getMyFriends();
        Log.d(TAG, "got users: " + friends.size());
        for (User user : friends) {
            UserDao userDao = new UserDao(user.getUserId(), user.getUserName(),
                    user.isMale(), user.isOnline(), false);
            users.add(userDao);
        }
        return UserDao.bulkUpdateOrRemove(context, users);
    }

    private int refreshNewFriends(VkontakteAPI api, Context context)
            throws IOException, JSONException {
        List<UserDao> users = new LinkedList<UserDao>();
        List<User> friends = api.getMyNewFriends();
        Log.d(TAG, "got new users: " + friends.size());
        for (User user : friends) {
            UserDao userDao = new UserDao(user.getUserId(), user.getUserName(),
                    user.isMale(), user.isOnline(), true);
            userDao.saveOrUpdate(context);
        }
        context.getContentResolver().notifyChange(UserapiProvider.USERS_URI,
                null);
        return friends.size();
    }

//	private void processMessages(ApiCheckingKit kit, Map<UpdateType, Long> res) {
//		long incomingMess = res.get(UpdateType.MESSAGES)
//				- kit.getPreviosUnreadMessNum();
//		Log.d(TAG, "process messages: " + incomingMess);
//		if (incomingMess == 0) // messages count didn't changed since last
//								// checking
//			return;
//
//		if (incomingMess > 0) // new incoming messages
//		{
//			// TODO check this
//			VkontakteAPI api = ApiCheckingKit.getS_api();
//			try {
//				List<Message> mess = api.getPrivateMessages(api.id, 0, 1024,
//						privateMessagesTypes.inbox);
//				Message last = mess.get(mess.size() - 1);
//				if (useNotifications())
//					UpdatesNotifier.notifyMessages(getApplicationContext(),
//							incomingMess, last.getSender().getUserId());
//				kit.setPreviosUnreadMessNum(res.get(UpdateType.MESSAGES));
//
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (JSONException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//
//		} else // some messages were read by another way
//		{
//			kit.setPreviosUnreadMessNum(res.get(UpdateType.MESSAGES));
//		}
//	}
//
//	private void processFriends(ApiCheckingKit kit, Map<UpdateType, Long> res) {
//		long incomingFr = res.get(UpdateType.FRIENDSHIP_REQ)
//				- kit.getPreviosFriendshipRequestsNum();
//
//		if (incomingFr == 0)
//			return;
//
//		if (incomingFr > 0) {
//			// if (useNotifications())
//			// UpdatesNotifier.notify(getApplicationContext(), "New friends: " +
//			// incomingFr, useSound());
//			kit.setPreviosFriendshipRequestsNum(res
//					.get(UpdateType.FRIENDSHIP_REQ));
//		} else {
//			kit.setPreviosFriendshipRequestsNum(res.get(UpdateType.MESSAGES));
//		}
//	}
//
//	private void processPhotoTags(ApiCheckingKit kit, Map<UpdateType, Long> res) {
//		long incomingTags = res.get(UpdateType.TAGS)
//				- kit.getPreviosNewPhotoTagsNum();
//
//		if (incomingTags == 0)
//			return;
//
//		if (incomingTags > 0) {
//			// if (useNotifications())
//			// UpdatesNotifier.notify(getApplicationContext(),
//			// "New photo tags: " + incomingTags, useSound());
//			kit.setPreviosNewPhotoTagsNum(res.get(UpdateType.TAGS));
//		} else {
//			kit.setPreviosNewPhotoTagsNum(res.get(UpdateType.TAGS));
//		}
//	}

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
        // TODO implement
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
        threads.interrupt();
        super.onDestroy();
    }

}
