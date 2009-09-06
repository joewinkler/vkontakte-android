package org.googlecode.vkontakte_android;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.view.View;
import android.content.Intent;
import org.googlecode.vkontakte_android.database.MessageDao;
import static org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper.KEY_MESSAGE_DATE;
import org.googlecode.vkontakte_android.provider.UserapiProvider;
import org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;


public class CMessagesTab extends ListActivity implements AbsListView.OnScrollListener {
    private MessagesListAdapter adapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_list);
        adapter = new MessagesListAdapter(this, R.layout.message_row, managedQuery(UserapiProvider.MESSAGES_URI, null, null, null, KEY_MESSAGE_DATE + " DESC"));
        setListAdapter(adapter);
        getListView().setOnScrollListener(this);
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                MessageDao messageDao = new MessageDao(adapter.getCursor());
                Intent intent = new Intent(getApplicationContext(), ComposeMessageActivity.class);
                intent.putExtra(UserapiDatabaseHelper.KEY_MESSAGE_SENDERID, messageDao.getSenderId());
                startActivity(intent);
            }
        });
    }

    public void onScroll(AbsListView v, int i, int j, int k) {
    }

    public void onScrollStateChanged(AbsListView v, int state) {
        if (state == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && getListView().getLastVisiblePosition() == adapter.getCount() - 1) {
//            adapter.prepareData();
        }
    }

}