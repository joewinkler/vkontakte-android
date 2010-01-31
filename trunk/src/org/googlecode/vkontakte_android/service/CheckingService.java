package org.googlecode.vkontakte_android.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import org.googlecode.userapi.*;
import org.googlecode.vkontakte_android.database.MessageDao;
import org.googlecode.vkontakte_android.database.StatusDao;
import org.googlecode.vkontakte_android.database.UserDao;
import org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper;
import org.googlecode.vkontakte_android.provider.UserapiProvider;
import org.googlecode.vkontakte_android.utils.AppHelper;
import org.googlecode.vkontakte_android.utils.PreferenceHelper;
import org.json.JSONException;

import java.io.IOException;
import java.util.*;

//TODO check for thread-safety!!1 

public class CheckingService extends Service {

	private static final String TAG = "VK:CheckingService";

    public static final int MESSAGE_NUM_LOAD = 10;
    public static final int STATUS_NUM_LOAD = 6;

    private List<Thread> threads = Collections.synchronizedList(new LinkedList<Thread>());

    private ChangesHistory prevChangesHistory = new ChangesHistory();

    public enum ContentToUpdate {
        FRIENDS, MESSAGES_ALL, MESSAGES_IN, MESSAGES_OUT, WALL, HISTORY, STATUSES, ALL, PROFILE
    }

    @Override
    public void onCreate() {
        Log.v(TAG, "Service onCreate");
        super.onCreate();
        m_binder = new VkontakteServiceBinder(this);
    }

    @Override
    public void onStart(final Intent intent, int startId) {
        super.onStart(intent, startId);

        Log.v(TAG, "Started command: " + intent);
        try {
            String action = intent.getAction();
            if (AppHelper.ACTION_CHECK_UPDATES.equals(action))
                checkUpdates();
        } catch (Exception e) {
            Log.e(TAG, "Exception while checking updates", e);
            //TODO: Need to save that to show for user later...
        }
    	
        
    }

    /*
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "Started command: " + intent);

        try {
            String action = intent.getAction();
            if (AppHelper.ACTION_CHECK_UPDATES.equals(action))
                checkUpdates();
        } catch (Exception e) {
            Log.e(TAG, "Exception while checking updates", e);
            //TODO: Need to save that to show for user later...
        }

        return START_NOT_STICKY;
    }
*/
    /**
     * Check given content type for updates
     *
     * @param toUpdate   - ordinal of ContentToUpdate
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
        ContentToUpdate what = ContentToUpdate.values()[toUpdate];
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
                case MESSAGES_IN:
                case MESSAGES_OUT:
                    updateMessages();
                    break;
                case HISTORY:
                    checkUpdates();
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
                    //checkUpdates();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UserapiLoginException e) {
            e.printStackTrace();
        }
    }

    // =============== updating methods

    private long loadInboxMessages(int first, int last) throws IOException, JSONException, UserapiLoginException {
        VkontakteAPI api = ApiCheckingKit.getApi();
        MessagesStruct messagesStruct = api.getInboxMessagesStruct(first, last);
        List<Message> messages = messagesStruct.getMessages();
        if (messages != null) {
            for (Message m : messages) {
                MessageDao md = new MessageDao(m);
                md.add(this);
            }
        }
        getContentResolver().notifyChange(UserapiProvider.MESSAGES_URI, null);
        return messagesStruct.getTimestamp();
    }

    private long loadOutboxMessages(int first, int last) throws IOException, JSONException, UserapiLoginException {
        VkontakteAPI api = ApiCheckingKit.getApi();
        MessagesStruct messagesStruct = api.getOutboxMessagesStruct(first, last);
        List<Message> messages = messagesStruct.getMessages();
        if (messages != null) {
            for (Message m : messages) {
                MessageDao md = new MessageDao(m);
                md.add(this);
            }
        }
        getContentResolver().notifyChange(UserapiProvider.MESSAGES_URI, null);
        return messagesStruct.getTimestamp();
    }

    protected void loadMoreMessages(ContentToUpdate type) throws IOException, JSONException, UserapiLoginException {
        int count;
        switch (type) {
            case MESSAGES_IN:
                count = MessageDao.getInboxMessagesCount(this);
                loadInboxMessages(count, count + MESSAGE_NUM_LOAD - 1);
                break;
            case MESSAGES_OUT:
                count = MessageDao.getOutboxMessagesCount(this);
                loadOutboxMessages(count, count + MESSAGE_NUM_LOAD - 1);
                break;
            case MESSAGES_ALL:
                break;
        }
    }

    private void updateMessages() throws IOException, JSONException, UserapiLoginException {
        long timestamp = PreferenceHelper.getMessagesTimestamp(this);
        // If we haven't got messages yet...
        if (timestamp == -1) {
            long inTs, outTs;
            // This loop is needed to be sure that no new messages user will receive between loadInboxMessages and
            // loadOutboxMessages calls
            do {
                inTs = loadInboxMessages(0, MESSAGE_NUM_LOAD - 1);
                outTs = loadOutboxMessages(0, MESSAGE_NUM_LOAD - 1);
            } while (inTs != outTs);
            PreferenceHelper.setMessagesTimestamp(this, inTs);
        }
        else {
            // Getting messages changes from server and applying them to DB
            VkontakteAPI api = ApiCheckingKit.getApi();
            List<MessageHistory> history = api.getPrivateMessagesHistory(timestamp);
            MessageDao.applyMessagesHistory(this, history);
        }
    }

    private void updateFriends() throws IOException, JSONException {
        Log.d(TAG, "updating friends:");
        refreshFriends(ApiCheckingKit.getApi(), getApplicationContext());
        Log.d(TAG, "updating new friends:");
        refreshNewFriends(ApiCheckingKit.getApi(), getApplicationContext());
    }

    private void updateWall() {
        Log.d(TAG, "updating wall");
        // todo: implement
    }

    private void checkUpdates() throws IOException, JSONException, UserapiLoginException {
        Log.v(TAG, "Checking updates");

        Timestamps timestamps = new Timestamps();
        timestamps.setMessagesTs(PreferenceHelper.getMessagesTimestamp(this));

        ChangesHistory changesHistory = ApiCheckingKit.getApi().getChangesHistory(timestamps);

        // Applying history changes if exist
        List<MessageHistory> messagesHistory = changesHistory.getMessagesHistory();
        if (messagesHistory != null)
            MessageDao.applyMessagesHistory(this, messagesHistory);

        int changes = prevChangesHistory.compareTo(changesHistory);

        // if there were changes and notifications are enabled in settings
        if (changes != 0 && PreferenceHelper.getNotifications(getApplicationContext())) {
            prevChangesHistory = changesHistory;

            boolean newEvents = changes == -1;
            UpdatesNotifier.notifyChangesHistory(getApplicationContext(), changesHistory, newEvents);
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

    protected void updateStatusesForUser(int start, int end, long id) throws IOException, JSONException {
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
            boolean isNew = true;
            //todo: delete only partial; use date/timestamp
            getContentResolver().delete(UserapiProvider.USERS_URI, UserapiDatabaseHelper.KEY_USER_NEW + "=1", null);
            for (User user : friends) {
                UserDao userDao = new UserDao(user, isNew, false);
                Uri useruri = userDao.saveOrUpdate(context);
                userDao.updatePhoto(this, user, useruri);
            }
            getContentResolver().notifyChange(UserapiProvider.USERS_URI, null);
        }
    }


    @Override
    public void onDestroy() {
        Log.v(TAG, "Service onDestroy");

        // TODO: stop all running threads
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
        Log.d(TAG, "Service onBind");

        return m_binder;
    }


}
