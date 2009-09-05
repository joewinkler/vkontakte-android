package org.googlecode.vkontakte_android;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.AbsListView;
import org.googlecode.vkontakte_android.database.MessageDao;
import static org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper.KEY_MESSAGE_DATE;
import org.googlecode.vkontakte_android.provider.UserapiProvider;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;


public class CMessagesTab extends ListActivity implements AbsListView.OnScrollListener {
    private MessagesListAdapter adapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        List<MessageDao> list = new LinkedList<MessageDao>();
        list.add(new MessageDao(2, new Date(), "some text", -1, 1, true));
        MessageDao.bulkSave(this, list);
        setContentView(R.layout.message_list);
        adapter = new MessagesListAdapter(this, R.layout.message_row, managedQuery(UserapiProvider.MESSAGES_URI, null, null, null, KEY_MESSAGE_DATE + " DESC"));
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