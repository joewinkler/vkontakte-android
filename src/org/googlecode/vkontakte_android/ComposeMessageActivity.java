package org.googlecode.vkontakte_android;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.AbsListView;
import org.googlecode.vkontakte_android.database.MessageDao;
import org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper;
import static org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper.KEY_MESSAGE_RECEIVERID;
import static org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper.KEY_MESSAGE_SENDERID;
import org.googlecode.vkontakte_android.provider.UserapiProvider;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class ComposeMessageActivity extends ListActivity implements AbsListView.OnScrollListener {
    private MessagesListAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        long userId = getIntent().getExtras().getLong(UserapiDatabaseHelper.KEY_MESSAGE_SENDERID, -1);

        List<MessageDao> list = new LinkedList<MessageDao>();
        list.add(new MessageDao(2, new Date(), "some text", -1, 1, true));
        MessageDao.bulkSave(this, list);
        setContentView(R.layout.messages);
        adapter = new MessagesListAdapter(this, R.layout.message_row, managedQuery(UserapiProvider.MESSAGES_URI, null, KEY_MESSAGE_SENDERID + "=?" + " OR " + KEY_MESSAGE_RECEIVERID + "=?", new String[]{String.valueOf(userId), String.valueOf(userId)}, null));
        setListAdapter(adapter);
        getListView().setOnScrollListener(this);
    }

    public void onScroll(AbsListView v, int i, int j, int k) {
    }

    public void onScrollStateChanged(AbsListView v, int state) {
        if (state == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && getListView().getLastVisiblePosition() == adapter.getCount() - 1) {
//            adapter.prepareData();
        }
    }

}