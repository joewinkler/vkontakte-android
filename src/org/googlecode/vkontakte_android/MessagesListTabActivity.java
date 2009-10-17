package org.googlecode.vkontakte_android;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.*;
import android.widget.AbsListView;
import android.widget.AdapterView;
import org.googlecode.vkontakte_android.database.MessageDao;
import org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper;
import static org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper.KEY_MESSAGE_DATE;
import org.googlecode.vkontakte_android.provider.UserapiProvider;
import org.googlecode.vkontakte_android.service.CheckingService;


public class MessagesListTabActivity extends ListActivity implements AbsListView.OnScrollListener {
    private MessagesListAdapter adapter;

    private static final String TAG = "org.googlecode.vkontakte_android.MessagesListTabActivity";

    enum MessagesCursorType {
        ALL, INCOMING, OUTCOMING
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
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

        getListView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && event.getAction() == KeyEvent.ACTION_DOWN
                        && getListView().getSelectedItemPosition() == adapter.getCount() - 1) {
                    loadMore();
                }
                return false;
            }

        });

        getListView().setOnScrollListener(this);
    }

    @SuppressWarnings("unchecked") 
    private void loadMore() {
        Log.d(TAG, "loading more messages: " + adapter.getCount() + "-" + (adapter.getCount() + CheckingService.MESSAGE_NUM_LOAD));
            setProgressBarIndeterminateVisibility(true);
        	new AsyncTask() {

				@Override
				protected void onPostExecute(Object result) {
					setProgressBarIndeterminateVisibility(false);
				}

				@Override
				protected Object doInBackground(Object... params) {
					try {
						CGuiTest.s_instance.m_vkService.loadPrivateMessages(
						        CheckingService.contentToUpdate.MESSAGES_IN.ordinal(),
						        adapter.getCount(), adapter.getCount() + CheckingService.MESSAGE_NUM_LOAD);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
					return null;
				}
        		
        	}.execute();
    }

    
    
    public void onScrollStateChanged(AbsListView v, int state) {
        if (state == AbsListView.OnScrollListener.SCROLL_STATE_IDLE
                && getListView().getLastVisiblePosition() == adapter.getCount() - 1) {
            loadMore();
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


    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
    }

}