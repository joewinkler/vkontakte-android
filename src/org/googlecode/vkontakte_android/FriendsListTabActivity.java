package org.googlecode.vkontakte_android;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import org.googlecode.vkontakte_android.database.UserDao;
import org.googlecode.vkontakte_android.service.CheckingService.contentToUpdate;

import java.util.LinkedList;
import java.util.List;

import static org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper.*;
import static org.googlecode.vkontakte_android.provider.UserapiProvider.USERS_URI;

public class FriendsListTabActivity extends AutoLoadActivity implements AdapterView.OnItemClickListener {
    private FriendsListAdapter adapter;
    private static String TAG = "FriendsListTabActivity";

    enum FriendsCursorType {
        ALL, NEW, ONLINE
    }

    public static final String SHOW_ONLY_NEW = "SHOW_ONLY_NEW";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friend_list);
        boolean onlyNew = false;
        Bundle extras = getIntent().getExtras();
        if (extras != null) onlyNew = extras.getBoolean(SHOW_ONLY_NEW);
        Cursor cursor = onlyNew ? makeCursor(FriendsCursorType.NEW) : makeCursor(FriendsCursorType.ONLINE);
        adapter = new FriendsListAdapter(this, R.layout.friend_row, cursor);

        final Handler handler = new Handler();
        setupLoader(new AutoLoadActivity.Loader() {

            @Override
            public Boolean load() {
                try {
                    ServiceHelper.getService().update(contentToUpdate.FRIENDS.ordinal(), true);
                    ServiceHelper.getService().loadUsersPhotos(getVisibleUsers());
                    handler.post(new Runnable() {

                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                        }
                    });

                } catch (RemoteException e) {
                    e.printStackTrace();
                    AppHelper.showFatalError(FriendsListTabActivity.this, "While trying to load friends photos");
                    Log.e(TAG,"Can not load friends photos" );
                }

                return false;
            }

        }, adapter);
        ACTION_FLAGS = AutoLoadActivity.ACTION_ON_START;
        registerForContextMenu(getListView());
        getListView().setOnItemClickListener(this);
    }

    /**
     * Get list of ids of shown users
     */
    private List<String> getVisibleUsers() {
        List<String> us = new LinkedList<String>();
        for (int i = 0; i < adapter.getCount(); ++i) {
            UserDao ud = new UserDao((Cursor) adapter.getItem(i));
            us.add(String.valueOf(ud.userId));
        }
        return us;
    }

    /**
     * Get list of ids of users that appeared on the current screen
     *
     * @return list
     */
    /*
    private List<String> getScreenVisibleUsers() {
        List<String> us = new LinkedList<String>();

        int f = getListView().getFirstVisiblePosition();
        int l = getListView().getLastVisiblePosition();
        for (int i = f; i <= l; ++i) {
            Cursor c = (Cursor) getListView().getItemAtPosition(i);
            if (c == null || c.isAfterLast()) {
                break;
            }
            UserDao ud = new UserDao(c);
            us.add(String.valueOf(ud.userId));
        }
        return us;
    }
    */


    private Cursor makeCursor(FriendsCursorType type) {

        switch (type) {
            case NEW:
                return managedQuery(USERS_URI, null, KEY_USER_NEW + "=1", null,
                        KEY_USER_USERID + " ASC," + KEY_USER_NEW + " DESC, " + KEY_USER_ONLINE + " DESC"

                );
            case ONLINE:
                return managedQuery(USERS_URI, null, KEY_USER_ONLINE + "=1", null,
                        KEY_USER_USERID + " ASC," + KEY_USER_NEW + " DESC, " + KEY_USER_ONLINE + " DESC"
                );
            case ALL:
                return managedQuery(USERS_URI, null,
                        KEY_USER_IS_FRIEND + "=?", new String[]{"1"},
                        KEY_USER_USERID + " ASC," + KEY_USER_NEW + " DESC, " + KEY_USER_ONLINE + " DESC"
                );
            default:
                return managedQuery(USERS_URI, null, null, null, null);
        }
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.friend_context_menu, menu);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        UserDao user = UserDao.get(this, info.id);
        if (user.isNewFriend()) {
            menu.removeItem(R.id.remove_from_friends);
        } else {
            menu.removeItem(R.id.add_to_friends);
        }
    }

    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        long rowId = info.id;
        UserDao user = UserDao.get(this, rowId);
        long userId = user.userId;
        switch (item.getItemId()) {
            case R.id.view_profile:
                UserHelper.viewProfile(this, userId);
                return true;
            case R.id.remove_from_friends:
                //todo
                return true;
            case R.id.send_message:
                UserHelper.sendMessage(this, userId);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long rowId) {
        UserDao user = UserDao.get(this, rowId);
        UserHelper.viewProfile(this, user.userId);
    }
}
