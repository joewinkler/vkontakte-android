package org.googlecode.vkontakte_android;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;

import org.googlecode.vkontakte_android.database.MessageDao;
import org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper;
import static org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper.KEY_MESSAGE_DATE;
import org.googlecode.vkontakte_android.provider.UserapiProvider;


public class MessagesListTabActivity extends ListActivity implements AbsListView.OnScrollListener {
    private MessagesListAdapter adapter;

    enum MessagesCursorType {ALL, INCOMING, OUTCOMING};
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_list);
                
        adapter = new MessagesListAdapter(this, R.layout.message_row, getCursor(MessagesCursorType.ALL));
        setListAdapter(adapter);
        registerForContextMenu(getListView());
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                MessageDao messageDao = new MessageDao(adapter.getCursor());
                Intent intent = new Intent(getApplicationContext(), ComposeMessageActivity.class);
                boolean isOutgoing = messageDao.getSenderId() == CSettings.myId;
                intent.putExtra(UserapiDatabaseHelper.KEY_MESSAGE_SENDERID, isOutgoing ? messageDao.getReceiverId() : messageDao.getSenderId());
                startActivity(intent);
            }
        });
        getListView().setOnScrollListener(this);
    }


    public void onScroll(AbsListView v, int i, int j, int k) {
    }

    public void onScrollStateChanged(AbsListView v, int state) {
        if (state == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && getListView().getLastVisiblePosition() == adapter.getCount() - 1) {
            //todo: download more messages?
        }
    }

    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        long rowId = info.id;
        switch (item.getItemId()) {
            case R.id.message_view:
                return true;

            case R.id.message_reply:
                return true;

            case R.id.message_mark_as_spam:
                return true;

            case R.id.message_delete:
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.message_context_menu, menu);
    }
    
    
    private Cursor getChatCursor(Long userid) {
    	return managedQuery(UserapiProvider.MESSAGES_URI, null, 
        		UserapiDatabaseHelper.KEY_MESSAGE_RECEIVERID + "=? OR " +
        		UserapiDatabaseHelper.KEY_MESSAGE_SENDERID + "=?",
        		new String[]{userid.toString(), userid.toString()},
        		KEY_MESSAGE_DATE + " DESC");
    }
    
    private Cursor getCursor(MessagesCursorType type) {
		switch (type) {

		case INCOMING:
			return managedQuery(UserapiProvider.MESSAGES_URI, null,
					UserapiDatabaseHelper.KEY_MESSAGE_RECEIVERID + "="
							+ CSettings.myId, null, KEY_MESSAGE_DATE + " DESC");
		case OUTCOMING:
			return managedQuery(UserapiProvider.MESSAGES_URI, null,
					UserapiDatabaseHelper.KEY_MESSAGE_SENDERID + "="
							+ CSettings.myId, null, KEY_MESSAGE_DATE + " DESC");
		default:
			return this.managedQuery(UserapiProvider.MESSAGES_URI, null, null,
					null, KEY_MESSAGE_DATE + " DESC");

		}
	}
    
}