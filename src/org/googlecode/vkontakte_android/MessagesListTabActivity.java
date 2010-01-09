package org.googlecode.vkontakte_android;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import org.googlecode.vkontakte_android.database.MessageDao;
import org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper;
import org.googlecode.vkontakte_android.provider.UserapiProvider;
import org.googlecode.vkontakte_android.service.CheckingService;

import static org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper.KEY_MESSAGE_DATE;


public class MessagesListTabActivity extends AutoLoadActivity {
    private static final String TAG = "org.googlecode.vkontakte_android.MessagesListTabActivity";

    enum MessagesCursorType {
        ALL, INCOMING, OUTCOMING
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                    AppHelper.showFatalError(MessagesListTabActivity.this, "While trying to load messages");
                    Log.e(TAG, "Loading messages failed");
                }
                return false;
            }

        }, new MessagesListAdapter(this, R.layout.message_row, getCursor(MessagesCursorType.ALL)));

        registerForContextMenu(getListView());

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                MessageDao messageDao = new MessageDao(((CursorAdapter) m_adapter).getCursor());
                Intent intent = new Intent(getApplicationContext(), ComposeMessageActivity.class);
                boolean isOutgoing = messageDao.getSenderId() == Settings.myId;
                intent.putExtra(UserapiDatabaseHelper.KEY_MESSAGE_SENDERID, isOutgoing ? messageDao.getReceiverId() : messageDao.getSenderId());
                startActivity(intent);
            }
        });

        getListView().setOnScrollListener(this);
    }

    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        long rowId = info.id;
        MessageDao messageDao = MessageDao.get(this, rowId);
        switch (item.getItemId()) {
            case R.id.message_view_and_reply:
                Intent intent = new Intent(this, ComposeMessageActivity.class);
                boolean isOutgoing = messageDao.getSenderId() == Settings.myId;
                intent.putExtra(UserapiDatabaseHelper.KEY_MESSAGE_SENDERID, isOutgoing ? messageDao.getReceiverId() : messageDao.getSenderId());
                startActivity(intent);
                return true;
            case R.id.message_delete:
//                VkontakteAPI api = null;
//                boolean result = api.deleteMessage(messageDao.getSenderId(), messageDao.getId())
//                todo: handle result - if true delete from db; if false shouw error to user
                return true;
            case R.id.message_mark_as_read:
//                VkontakteAPI api = null;
//                api.markAsRead(messageDao.getId());
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

    /*
    private Cursor getChatCursor(Long userid) {
        return managedQuery(UserapiProvider.MESSAGES_URI, null,
                UserapiDatabaseHelper.KEY_MESSAGE_RECEIVERID + "=? OR " +
                        UserapiDatabaseHelper.KEY_MESSAGE_SENDERID + "=?",
                new String[]{userid.toString(), userid.toString()},
                KEY_MESSAGE_DATE + " DESC");
    }
    */

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