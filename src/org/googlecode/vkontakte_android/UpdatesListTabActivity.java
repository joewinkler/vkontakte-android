package org.googlecode.vkontakte_android;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import org.googlecode.vkontakte_android.database.UserDao;
import org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper;
import static org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper.*;
import static org.googlecode.vkontakte_android.provider.UserapiProvider.STATUSES_URI;

public class UpdatesListTabActivity extends ListActivity {
    private UpdatesListAdapter adapter;
//    private boolean showAll = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.status_list);
        final Cursor statusesCursor = managedQuery(STATUSES_URI, null, null, null, KEY_STATUS_DATE + " DESC ");
        adapter = new UpdatesListAdapter(this, R.layout.status_row, statusesCursor);
//        setCursor(showAll);
        setListAdapter(adapter);
        registerForContextMenu(getListView());

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                viewProfile(l);
            }
        });
        //todo: use tabcounter?
    }

    public void viewProfile(long rowId) {
        //attention, rowId, not userId
        //todo: implement
        Toast.makeText(this, "view profile not yet implemented", Toast.LENGTH_SHORT).show();
    }

//    private void setCursor(boolean showAll) {
//        if (showAll) {
//            final Cursor allFriendsCursor = managedQuery(UserapiProvider.USERS_URI, null, null, null,
//                    KEY_USER_NEW + " DESC, " + KEY_USER_ONLINE + " DESC"
//            );
//            adapter.changeCursor(allFriendsCursor);
//        } else {
//            final Cursor onlineFriendsCursor = managedQuery(UserapiProvider.USERS_URI, null, KEY_USER_ONLINE + "=1", null,
//                    KEY_USER_NEW + " DESC, " + KEY_USER_ONLINE + " DESC"
//            );
//            adapter.changeCursor(onlineFriendsCursor);
//        }
//    }

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
        switch (item.getItemId()) {
            case R.id.view_profile:
                viewProfile(info.id);
                return true;
            case R.id.remove_from_friends:
                //todo
                return true;
            case R.id.send_message:
                UserDao user = UserDao.get(this, info.id);
                Intent intent = new Intent(this, ComposeMessageActivity.class);
                intent.putExtra(UserapiDatabaseHelper.KEY_MESSAGE_SENDERID, user.getUserId());
                startActivity(intent);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
}