package org.googlecode.vkontakte_android.service;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.List;
import java.util.LinkedList;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

import org.googlecode.vkontakte_android.CGuiTest;
import org.googlecode.vkontakte_android.CSettings;
import org.googlecode.vkontakte_android.service.ApiCheckingKit.UpdateType;
import org.googlecode.vkontakte_android.database.MessageDao;
import org.googlecode.vkontakte_android.database.UserDao;
import org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper;
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
import android.database.Cursor;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

public class CheckingService extends Service {

    private static String TAG = "VK-Service";
    private Timer m_timer = new Timer();
    private static SharedPreferences s_prefs;
    private List<Thread> threads = Collections.synchronizedList(new LinkedList<Thread>());
   //private boolean m_hasConnection = true;

    
    public enum contentToUpdate {
        FRIENDS, MESSAGES_ALL, MESSAGES_IN, MESSAGES_OUT, WALL, HISTORY, ALL
    }

    @Override
    public void onCreate() {
        super.onCreate();
        s_prefs = PreferenceManager.getDefaultSharedPreferences(this);
        try {
            ApiCheckingKit.s_ctx = getApplicationContext(); 
            //TODO if login fails
            ApiCheckingKit.login();
            restartScheduledUpdates();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStart(final Intent intent, int startId) {
        doCheck(intent.getIntExtra("action", 1));
    }

    /**
     * Check given content type for updates
     * @param toUpdate - ordinal of contentToUpdate
     */
    private void doCheck(final int toUpdate)  {
    	Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

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
        });
        threads.add(t);
        t.start();
     }
     
    /**
     * Starts a thread checking api periodically
     */
     private void restartScheduledUpdates() {
       
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
            }
        }
        int period = CSettings.getPeriod(getApplicationContext());
        m_timer.scheduleAtFixedRate(new CheckingTask(), 0L, 1000 * 30);
        Log.d(TAG, "Timer with period: " + period);
    }

    
    // =============== updating methods

    private void updateInMessages(long count) throws IOException, JSONException {
        VkontakteAPI api = ApiCheckingKit.getApi();
        List<Message> messages = api.getInbox(0, (int) count);
        MessageDao single = null;
        int countNew = 0;
        for (Message m : messages) {
            MessageDao md = new MessageDao(m.getId(), m.getDate(), m.getText(), m.getSender().getUserId(), m.getReceiver().getUserId(), m.isRead());
            countNew += md.saveOrUpdate(this);
            single = md;
        }

//		Cursor cursor = this.getContentResolver().query(UserapiProvider.MESSAGES_URI, null,
//                UserapiDatabaseHelper.KEY_MESSAGE_READ + "=?",
//                new String[]{"0"},
//                null);
//		int countNew = 0;

//		if (cursor!=null){
//
//			countNew=cursor.getCount();
//			if (countNew==1){
//				cursor.moveToNext();
//				single=new MessageDao(cursor);
//			}
//			cursor.close();
//		}

//		UpdatesNotifier.notifyMessages(this, countNew, single);
        if (countNew>0)
        UpdatesNotifier.notifyMessages(this, count, single);
        getContentResolver().notifyChange(UserapiProvider.MESSAGES_URI, null);
        //TODO get real counter from provider
    }

    private void updateOutMessages(int count) throws IOException, JSONException {
        VkontakteAPI api = ApiCheckingKit.getApi();
        api.getOutbox(0, count);
        getContentResolver().notifyChange(UserapiProvider.MESSAGES_URI, null);
    }

    private void updateFriends() throws IOException, JSONException {
        Log.d(TAG, "updating friends");
        int[] updated = refreshFriends(ApiCheckingKit.getApi(),
                getApplicationContext());
        Log.d(TAG, "removed: " + updated[0] + "; added: " + updated[1]);
        Log.d(TAG, "updating new friends");
        int updatedNew = refreshNewFriends(ApiCheckingKit.getApi(),
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
        VkontakteAPI api = ApiCheckingKit.getApi();
        updateInMessages(api.getChangesHistory().getFriendsCount());
    }

    private int[] refreshFriends(VkontakteAPI api, Context context)
            throws IOException, JSONException {
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
        context.getContentResolver().notifyChange(UserapiProvider.USERS_URI, null);
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

    @Override
    public void onDestroy() {
        Log.d(TAG, "service stopped");
        try {
            ApiCheckingKit.getApi().logout();
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

    // ============ RPC stuff ============================ 

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	private final IVkontakteService.Stub binder = new IVkontakteService.Stub() {
		
		@Override
		public void sendMessage(String text, long id) throws RemoteException {
            Message message = new Message();
            message.setDate(new Date());
            message.setReceiverId(id);
            message.setText(text);
			try {
				ApiCheckingKit.getApi().sendMessageToUser(message);
			} catch (IOException e) {
				UpdatesNotifier.showError(getApplicationContext(),"Cannot send the message. Check connection.");
				e.printStackTrace();
			}
		}

		@Override
		public void sendStatus(String status) throws RemoteException {
		}

		@Override
		public void update(int what) throws RemoteException {
			doCheck(what);
		}

		@Override
		public boolean login(String login, String pass) throws RemoteException {
			VkontakteAPI api = ApiCheckingKit.getApi();
			Context ctx = getApplicationContext();
			
			try {
			  if (api.login(login, pass)) {
					CSettings.saveLogin(ctx, login, pass, api.getRemixpassword(), api.getSid());
					return true;
			  }
			} catch (IOException e) {
				e.printStackTrace();
				UpdatesNotifier.showError(ctx, "Connection problems");
				return false;
			} 
			return false;
		}

		@Override
		public boolean loginAuth() throws RemoteException {
			Context ctx = getApplicationContext();
			VkontakteAPI api = ApiCheckingKit.getApi();
			
			if (CSettings.isLogged(ctx)) {
				try {
					Log.d(TAG, "already logged. using existing log/pass");
					if (!TextUtils.isEmpty(CSettings.getSid(ctx))) {
						api.setSid(CSettings.getSid(ctx));
						System.out.println("logged with sid");
						return true;
					} else if (api.login(CSettings.getLogin(ctx), CSettings.getPass(ctx))) {
						// todo: refresh only remix
						CSettings.saveLogin(ctx,
								CSettings.getLogin(ctx), CSettings.getPass(ctx),
								api.getRemixpassword(), api.getSid());
						return true;
					} else {
						UpdatesNotifier.showError(ctx, "Either login or password is incorrect");
						//TODO clear settings
						return false;
					}
				} catch (IOException ex) {
					UpdatesNotifier.showError(ctx, "Connection problems");
					return false;
				}
			} else {
			  return false;	
			}
		}

		@Override
		public void logout() throws RemoteException {
			try {
				ApiCheckingKit.getApi().logout();
				CSettings.clearPrivateInfo(getApplicationContext());
			} catch (IOException e) {
				UpdatesNotifier.showError(getApplicationContext(), "Connection problems");
				e.printStackTrace();
			}
		}
	
	
	
	};
}
