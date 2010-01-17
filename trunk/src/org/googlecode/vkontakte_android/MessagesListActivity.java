package org.googlecode.vkontakte_android;

import java.io.IOException;

import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.CursorAdapter;

import org.googlecode.userapi.UserapiLoginException;
import org.googlecode.vkontakte_android.database.MessageDao;
import org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper;
import org.googlecode.vkontakte_android.provider.UserapiProvider;
import org.googlecode.vkontakte_android.service.ApiCheckingKit;
import org.googlecode.vkontakte_android.service.CheckingService;
import org.googlecode.vkontakte_android.service.UpdatesNotifier;
import org.googlecode.vkontakte_android.utils.AppHelper;
import org.googlecode.vkontakte_android.utils.ServiceHelper;
import org.json.JSONException;

import static org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper.KEY_MESSAGE_DATE;


public class MessagesListActivity extends AutoLoadActivity {
    private static final String TAG = "org.googlecode.vkontakte_android.MessagesListTabActivity";

    enum MessagesCursorType {
        ALL, INCOMING, OUTCOMING
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        
        setContentView(R.layout.message_list);
        setupLoader(new AutoLoadActivity.Loader() {

            @Override
            public Boolean load() {
                try {
                    return ServiceHelper.getService().loadPrivateMessages(
                            CheckingService.contentToUpdate.MESSAGES_IN.ordinal(),
                            m_adapter.getCount(), m_adapter.getCount() + CheckingService.MESSAGE_NUM_LOAD);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    AppHelper.showFatalError(MessagesListActivity.this, "While trying to load messages");
                    Log.e(TAG, "Loading messages failed");
                }
                return false;
            }

        }, new MessagesListAdapter(this, R.layout.message_row, getCursor(MessagesCursorType.INCOMING)));

        registerForContextMenu(getListView());

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                MessageDao messageDao = new MessageDao(((CursorAdapter) m_adapter).getCursor());
                Intent intent = new Intent(MessagesListActivity.this, ComposeMessageActivity.class);
                boolean isOutgoing = messageDao.getSenderId() == Settings.myId;
                intent.putExtra(UserapiDatabaseHelper.KEY_MESSAGE_SENDERID, isOutgoing ? messageDao.getReceiverId() : messageDao.getSenderId());
                startActivity(intent);
            }
        });

        getListView().setOnScrollListener(this);
        refreshOnStart();
    }
    
    
    private void refreshOnStart() {
        new AsyncTask<Object, Object, Object>(){
        	
        	@Override
            protected void onPreExecute() {
                super.onPreExecute();
                setProgressBarIndeterminateVisibility(true);
            }
            @Override
            protected void onPostExecute(Object result) {
                setProgressBarIndeterminateVisibility(false);
            }
    		@Override
    		protected Object doInBackground(Object... params) {
    			try {
    				ServiceHelper.getService().update(CheckingService.contentToUpdate.MESSAGES_ALL.ordinal(), true);
    			} catch (RemoteException e) {
    				e.printStackTrace();
    			}
    			return null;
    		}
        }.execute();
    }
    

    @Override
    protected void onResume() {
        super.onResume();
        UpdatesNotifier.clearNotification(getApplicationContext());
    }

	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();
		long rowId = info.id;
		MessageDao messageDao = MessageDao.get(this, rowId);
		switch (item.getItemId()) {
		case R.id.message_view_and_reply:
			Intent intent = new Intent(this, ComposeMessageActivity.class);
			boolean isOutgoing = messageDao.getSenderId() == Settings.myId;
			intent.putExtra(UserapiDatabaseHelper.KEY_MESSAGE_SENDERID,
					isOutgoing ? messageDao.getReceiverId() : messageDao
							.getSenderId());
			startActivity(intent);
			return true;
		case R.id.message_delete:
			try {
				ApiCheckingKit.getApi().deleteMessage(messageDao.getSenderId(), messageDao.id);
				messageDao.delete(this);
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (JSONException e1) {
				e1.printStackTrace();
			} catch (UserapiLoginException e1) {
				e1.printStackTrace();
			}
			return true;
		case R.id.message_mark_as_read:
			try {
				ApiCheckingKit.getApi().markAsRead(messageDao.id);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (UserapiLoginException e) {
				e.printStackTrace();
			}
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

    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.messages_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.inbox:
            	changeAdapter(new MessagesListAdapter(this, R.layout.message_row, getCursor(MessagesCursorType.INCOMING)));
                return true;
                
            case R.id.sent:
            	changeAdapter(new MessagesListAdapter(this, R.layout.message_row, getCursor(MessagesCursorType.OUTCOMING)));
            	return true;

            case R.id.refresh:
            	refreshOnStart();
            	return true;
            	
            default:
                return super.onContextItemSelected(item);
        }
    }
    private Cursor getCursor(MessagesCursorType type) {
        switch (type) {
            case INCOMING:
                return managedQuery(UserapiProvider.MESSAGES_URI, null,
                        UserapiDatabaseHelper.KEY_MESSAGE_RECEIVERID + "="
                                + Settings.myId, null, KEY_MESSAGE_DATE + " DESC");
            case OUTCOMING:
                return managedQuery(UserapiProvider.MESSAGES_URI, null,
                        UserapiDatabaseHelper.KEY_MESSAGE_SENDERID + "="
                                + Settings.myId, null, KEY_MESSAGE_DATE + " DESC");
            default:
                return this.managedQuery(UserapiProvider.MESSAGES_URI, null, null,
                        null, KEY_MESSAGE_DATE + " DESC");

        }
    }


}