package org.googlecode.vkontakte_android.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import org.googlecode.userapi.*;
import org.googlecode.vkontakte_android.CSettings;
import org.googlecode.vkontakte_android.database.MessageDao;
import org.googlecode.vkontakte_android.database.StatusDao;
import org.googlecode.vkontakte_android.database.UserDao;
import org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper;
import org.googlecode.vkontakte_android.provider.UserapiProvider;
import org.json.JSONException;

import java.io.IOException;
import java.util.*;

//TODO check for thread-safety!!1 

public class CheckingService extends Service {

    private static final String TAG = "VK-Service";

    public static final int MESSAGE_NUM_LOAD = 10;
    public static final int STATUS_NUM_LOAD = 6;

    private Timer m_timer = new Timer();
    private static SharedPreferences s_prefs;
    private List<Thread> threads = Collections.synchronizedList(new LinkedList<Thread>());


    public enum contentToUpdate {
        FRIENDS, MESSAGES_ALL, MESSAGES_IN, MESSAGES_OUT, WALL, HISTORY, STATUSES, ALL, PROFILE
    }

    @Override
    public void onCreate() {
        super.onCreate();
        m_binder = new VkontakteServiceBinder(this);

        s_prefs = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public void onStart(final Intent intent, int startId) {
        super.onStart(intent, startId);

    }

    /**
     * Check given content type for updates
     *
     * @param toUpdate   - ordinal of contentToUpdate
     * @param syncronous
     */
    void doCheck(final int toUpdate, final Bundle updateParams, boolean syncronous) {
        if (syncronous) {
            updateContent(toUpdate, updateParams);
        } else {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    updateContent(toUpdate, updateParams);
                }
            });
            threads.add(t);
            t.start();
        }
    }

    private void updateContent(final int toUpdate, final Bundle updateParams) {
        contentToUpdate what = contentToUpdate.values()[toUpdate];
        Log.d(TAG, "updating " + what + " is starting...");
        try {
            switch (what) {
                case FRIENDS:
                    updateFriends();
                    break;
                case WALL:
                    updateWall();
                    break;
                case MESSAGES_ALL:
                    updateMessages();
                    break;
                case MESSAGES_IN:
                    updateInMessages(0, MESSAGE_NUM_LOAD);
                    break;
                case MESSAGES_OUT:
                    updateOutMessages(0, MESSAGE_NUM_LOAD); //should be called when user sends messages
                    break;
                case HISTORY:
                    updateHistory();
                    break;
                case STATUSES:
                    updateStatuses(0, STATUS_NUM_LOAD);
                    break;
                case PROFILE:
                    //updateProfile();
                    break;

                default:
                    updateStatuses(0, STATUS_NUM_LOAD);
                    updateMessages();
                    //updateWall();
                    updateFriends();
                    //updateHistory();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts a thread checking api periodically
     */
    private void launchScheduledUpdates() {
    	int period = CSettings.getPeriod(getApplicationContext());
    	if (period==0){ 
    		return;
    	}
    	
        class CheckingTask extends TimerTask {
            @Override
            public void run() {
                Log.d(TAG, "checking by timer");
                try {
                    updateHistory();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
//                catch (OutOfMemoryError error) {
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//                    }
//                    onLowMemory();
//                }
            }
        }
        
        m_timer.scheduleAtFixedRate(new CheckingTask(), 0L, period);
        Log.d(TAG, "Timer with period: " + period);
    }

  public void cancelSheduledUpdates(){
	  m_timer.cancel();
	  m_timer.purge();
  }
  
  public void restartSheduledUpdates(){
	  cancelSheduledUpdates();
	  m_timer= new Timer();
	  launchScheduledUpdates();
  }
  
     
    

    // =============== updating methods

    protected void updateInMessages(long first, long last) throws IOException, JSONException {
        //todo: use history or friends-like update with save
        VkontakteAPI api = ApiCheckingKit.getApi();
        List<Message> messages = null;
        try {
            messages = api.getInbox((int) first, (int) last);
        } catch (UserapiLoginException e) {
            e.printStackTrace();
        }
        MessageDao single = null;
        int countNew = 0;
        if (messages != null) {
            for (Message m : messages) {

                MessageDao md = new MessageDao(m);
                if (single == null) {
                    single = md;
                }
                Log.d(TAG, "saving message");
                countNew += md.saveOrUpdate(this);

            }
        }
        if (countNew > 0)
            UpdatesNotifier.notifyMessages(this, countNew, single);
        getContentResolver().notifyChange(UserapiProvider.MESSAGES_URI, null);
        //TODO get real counter from provider
    }

    protected void updateOutMessages(long first, long last) throws IOException, JSONException {
        //todo: use history or friends-like update with save
        VkontakteAPI api = ApiCheckingKit.getApi();
        List<Message> messages = null;
        try {
            messages = api.getOutbox((int) first, (int) last);
        } catch (UserapiLoginException e) {
            e.printStackTrace();
        }
        if (messages != null) {
            for (Message m : messages) {
                MessageDao md = new MessageDao(m);
                Log.d(TAG, "saving outcoming message");
                md.saveOrUpdate(this);
            }
        }
        getContentResolver().notifyChange(UserapiProvider.MESSAGES_URI, null);
    }

    private void updateFriends() throws IOException, JSONException {
        Log.d(TAG, "updating friends:");
        refreshFriends(ApiCheckingKit.getApi(), getApplicationContext());
        Log.d(TAG, "updating new friends:");
        refreshNewFriends(ApiCheckingKit.getApi(), getApplicationContext());
    }

    private void updateMessages() throws IOException, JSONException {
        updateInMessages(0, MESSAGE_NUM_LOAD);
        updateOutMessages(0, MESSAGE_NUM_LOAD);
    }

    private void updateWall() {
        Log.d(TAG, "updating wall");
        // todo: implement
    }

    private void updateHistory() throws IOException, JSONException {
        Log.d(TAG, "updating history");
        ChangesHistory hist = null;
        try {
            hist = ApiCheckingKit.getApi().getChangesHistory();
        } catch (UserapiLoginException e) {
            e.printStackTrace();
        }
        long new_friends = 0;
        if (hist != null) {
            new_friends = hist.getFriendsCount() - ApiCheckingKit.m_histChanges.prevFriendshipRequestsNum;
        }
        long new_messages = 0;
        if (hist != null) {
            new_messages = hist.getMessagesCount() - ApiCheckingKit.m_histChanges.prevUnreadMessNum;
        }
        long new_tags = 0;
        if (hist != null) {
            new_tags = hist.getPhotosCount() - ApiCheckingKit.m_histChanges.prevNewPhotoTagsNum;
        }

        if (new_friends > 0) {
            assert hist != null;
            ApiCheckingKit.m_histChanges.prevFriendshipRequestsNum = hist.getFriendsCount();
            Log.d(TAG, "Received new friends: " + new_friends);
        }
        if (new_messages > 0) {
            assert hist != null;
            ApiCheckingKit.m_histChanges.prevUnreadMessNum = hist.getMessagesCount();
            Log.d(TAG, "Received new messages: " + new_messages);
        }
        if (new_tags > 0) {
            assert hist != null;
            ApiCheckingKit.m_histChanges.prevNewPhotoTagsNum = hist.getPhotosCount();
            Log.d(TAG, "Received new phototags: " + new_friends);
        }

        if ((new_friends | new_messages | new_tags) != 0) {
            UpdatesNotifier.notifyHistoryMessages(getApplicationContext(), new_friends, new_messages, new_tags);
        }
    }

    protected void updateStatuses(int start, int end) throws IOException, JSONException {
        Log.d(TAG, "updating statuses " + start + " to " + end);
        VkontakteAPI api = ApiCheckingKit.getApi();
        List<Status> statuses = null;
        try {
            statuses = api.getTimeline(start, end);
        } catch (UserapiLoginException e) {
            e.printStackTrace();
        }
        List<StatusDao> statusDaos = new LinkedList<StatusDao>();
        if (statuses != null) {
            for (Status status : statuses) {
                boolean personal = false;
                StatusDao statusDao = new StatusDao(status.getStatusId(), status.getUserId(), status.getUserName(), status.getDate(), status.getText(), personal);
                statusDaos.add(statusDao);
            }
        }
        StatusDao.bulkSaveOrUpdate(getApplicationContext(), statusDaos);
    }

    protected void updateStatuses(int start, int end, long id) throws IOException, JSONException {
        Log.d(TAG, "updating statuses for user:" + id + "/" + start + " to " + end);
        VkontakteAPI api = ApiCheckingKit.getApi();
        List<Status> statuses = null;
        try {
            statuses = api.getStatusHistory(id, start, end, 0);
        } catch (UserapiLoginException e) {
            e.printStackTrace();
        }
        List<StatusDao> statusDaos = new LinkedList<StatusDao>();
        if (statuses != null) {
            for (Status status : statuses) {
                boolean personal = true;
                StatusDao statusDao = new StatusDao(status.getStatusId(), status.getUserId(), status.getUserName(), status.getDate(), status.getText(), personal);
                statusDaos.add(statusDao);
            }
        }
        StatusDao.bulkSaveOrUpdate(getApplicationContext(), statusDaos);
    }

    //todo: use 'partial' lock for instead of synchronized(?)

    private synchronized void refreshFriends(VkontakteAPI api, Context context) throws IOException, JSONException {
        boolean firstUpdate = false;
        Cursor cursor = getContentResolver().query(UserapiProvider.USERS_URI, new String[]{UserapiDatabaseHelper.KEY_USER_ROWID}, null, null, null);
        if (cursor != null && cursor.getCount() == 0) {
            firstUpdate = true;
            cursor.close();
        }
        List<User> friends = null;
        try {
            friends = api.getMyFriends();
        } catch (UserapiLoginException e) {
            e.printStackTrace();
        }
        if (friends != null) {
            Log.d(TAG, "got users: " + friends.size());
        }
        StringBuilder notIn = new StringBuilder(" ");
        int counter = 0;
        boolean isNew = false;
        List<UserDao> users = null;
        if (friends != null) {
            users = new ArrayList<UserDao>(friends.size());
        }
        if (friends != null) {
            for (User user : friends) {
                UserDao userDao = new UserDao(user, isNew, true);
                notIn.append(user.getUserId()).append(",");
                Uri useruri = userDao.saveOrUpdate(this);
                if (!firstUpdate) {  //special hack for photo update - load it when needed
                    //userDao.updatePhoto(this, user, useruri);
                }
                if (counter++ == 10) {
                    getContentResolver().notifyChange(useruri, null);
                    counter = 0;
                }
                users.add(userDao);
            }
        }

        notIn.deleteCharAt(notIn.length() - 1);//remove last ','
        getContentResolver().delete(UserapiProvider.USERS_URI, UserapiDatabaseHelper.KEY_USER_NEW + "=0" + " AND "
                + UserapiDatabaseHelper.KEY_USER_USERID + " NOT IN(" + notIn + ")" + " AND " +
                UserapiDatabaseHelper.KEY_USER_IS_FRIEND + "=1", null);

    }


    //todo: use 'partial' lock for instead of synchronized(?)

    private synchronized void refreshNewFriends(VkontakteAPI api, Context context) throws IOException, JSONException {
        List<User> friends = null;
        try {
            friends = api.getMyNewFriends();
        } catch (UserapiLoginException e) {
            e.printStackTrace();
        }
        if (friends != null) {
            Log.d(TAG, "got new users: " + friends.size());
        }
        StringBuilder notIn = new StringBuilder(" ");
        boolean isNew = true;
        if (friends != null) {
            for (User user : friends) {
                UserDao userDao = new UserDao(user, isNew, false);
                Uri useruri = userDao.saveOrUpdate(context);
                notIn.append(user.getUserId()).append(",");
                userDao.updatePhoto(this, user, useruri);
                getContentResolver().notifyChange(useruri, null);
            }
        }
        notIn.deleteCharAt(notIn.length() - 1);//remove last ','
        getContentResolver().delete(UserapiProvider.USERS_URI, UserapiDatabaseHelper.KEY_USER_NEW + "=1" + " AND " + UserapiDatabaseHelper.KEY_USER_USERID + " NOT IN(" + notIn + ")", null);
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

    
       
      
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "service stopped/destroyed");

        // stop all running threads
        for (Thread t : threads) {
            if (t.isAlive())
                t.interrupt();
        }
        super.onDestroy();
    }

    // ============ RPC stuff ============================ 

    private IVkontakteService.Stub m_binder;

    @Override
    public IBinder onBind(Intent intent) {
        launchScheduledUpdates();
        return m_binder;
    }


}
