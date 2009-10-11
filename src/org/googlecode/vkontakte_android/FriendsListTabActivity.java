package org.googlecode.vkontakte_android;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.*;
import android.widget.AdapterView;

import android.widget.Toast;
import org.googlecode.vkontakte_android.database.UserDao;
import org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper;
import org.googlecode.vkontakte_android.provider.UserapiProvider;
import static org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper.*;

public class FriendsListTabActivity extends ListActivity implements AdapterView.OnItemClickListener {
    private FriendsListAdapter adapter;
    private boolean showAll = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friend_list);
        adapter = new FriendsListAdapter(this, R.layout.friend_row, null);
        setCursor(showAll);
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

    private void setCursor(boolean showAll) {
        if (showAll) {
            final Cursor allFriendsCursor = managedQuery(UserapiProvider.USERS_URI, null, KEY_USER_IS_FRIEND+"=?", new String[]{"1"},
                    KEY_USER_NEW + " DESC, " + KEY_USER_ONLINE + " DESC"
            );
            adapter.changeCursor(allFriendsCursor);
        } else {
            final Cursor onlineFriendsCursor = managedQuery(UserapiProvider.USERS_URI, null, KEY_USER_ONLINE + "=1", null,
                    KEY_USER_NEW + " DESC, " + KEY_USER_ONLINE + " DESC"
            );
            adapter.changeCursor(onlineFriendsCursor);
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
        switch (item.getItemId()) {
            case R.id.view_profile:
                UserHelper.viewProfile(this, rowId);
                return true;
            case R.id.remove_from_friends:
                //todo
                return true;
            case R.id.send_message:
                UserHelper.sendMessage(this, rowId);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long rowId) {
        UserHelper.viewProfile(this, rowId);
    }
}
