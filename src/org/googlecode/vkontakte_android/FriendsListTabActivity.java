package org.googlecode.vkontakte_android;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import org.googlecode.vkontakte_android.database.UserDao;
import static org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper.*;
import static org.googlecode.vkontakte_android.provider.UserapiProvider.USERS_URI;

public class FriendsListTabActivity extends ListActivity implements AdapterView.OnItemClickListener {
    private FriendsListAdapter adapter;

    enum MessagesCursorType {
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
        Cursor cursor = onlyNew ? makeCursor(MessagesCursorType.NEW) : makeCursor(MessagesCursorType.ALL);
        adapter = new FriendsListAdapter(this, R.layout.friend_row, cursor);
        setListAdapter(adapter);
        registerForContextMenu(getListView());

        getListView().setOnItemClickListener(this);


        //todo: use tabcounter
//        TextView tv = (TextView) findViewById(R.id.new_counter);
//        try {
//            long counter = CGuiTest.api.getChangesHistory().getFriendsCount();
//            tv.setText("new friends: " + counter);
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
    }

    private Cursor makeCursor(MessagesCursorType type) {
        switch (type) {
            case NEW:
                return managedQuery(USERS_URI, null, KEY_USER_NEW + "=1", null,
                        KEY_USER_NEW + " DESC, " + KEY_USER_ONLINE + " DESC"
                );
            case ONLINE:
                return managedQuery(USERS_URI, null, KEY_USER_ONLINE + "=1", null,
                        KEY_USER_NEW + " DESC, " + KEY_USER_ONLINE + " DESC"
                );
            case ALL:
                return managedQuery(USERS_URI, null,
                        KEY_USER_IS_FRIEND + "=?", new String[]{"1"},
                        KEY_USER_NEW + " DESC, " + KEY_USER_ONLINE + " DESC"
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
