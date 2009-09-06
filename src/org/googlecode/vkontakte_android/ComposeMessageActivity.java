package org.googlecode.vkontakte_android;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.TextView;
import org.googlecode.userapi.Message;
import org.googlecode.vkontakte_android.database.MessageDao;
import org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper;
import static org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper.KEY_MESSAGE_RECEIVERID;
import static org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper.KEY_MESSAGE_SENDERID;
import org.googlecode.vkontakte_android.provider.UserapiProvider;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class ComposeMessageActivity extends ListActivity implements AbsListView.OnScrollListener {
    private MessagesListAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final long userId = getIntent().getExtras().getLong(UserapiDatabaseHelper.KEY_MESSAGE_SENDERID, -1);
        final String vkId = getIntent().getData().getLastPathSegment(); // toDo new

        List<MessageDao> list = new LinkedList<MessageDao>();
        list.add(new MessageDao(2, new Date(), "some text", -1, 1, true));
        MessageDao.bulkSave(this, list);
        setContentView(R.layout.messages);
        adapter = new MessagesListAdapter(this, R.layout.message_row, managedQuery(UserapiProvider.MESSAGES_URI, null, KEY_MESSAGE_SENDERID + "=?" + " OR " + KEY_MESSAGE_RECEIVERID + "=?", new String[]{String.valueOf(userId), String.valueOf(userId)}, null));
        setListAdapter(adapter);
        getListView().setOnScrollListener(this);
        final TextView textView = (TextView) findViewById(R.id.mess_to_send);
        findViewById(R.id.send_reply).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Message message = new Message();
                message.setDate(new Date());
//                message.setReceiverId(userId);
                message.setReceiverId(11723128);
                message.setText(textView.getText().toString());
                try {
                    String result = CGuiTest.api.sendMessageToUser(message);
                    System.out.println("send result: " + result);
                } catch (IOException e) {
                    e.printStackTrace();
                }
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