package org.googlecode.vkontakte_android;

import android.app.ListActivity;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.widget.AbsListView;
import android.widget.TextView;
import android.widget.Toast;
import org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper;
import org.googlecode.vkontakte_android.provider.UserapiProvider;
import org.googlecode.vkontakte_android.service.CheckingService;
import org.googlecode.vkontakte_android.utils.AppHelper;
import org.googlecode.vkontakte_android.utils.ServiceHelper;

import static org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper.KEY_MESSAGE_RECEIVERID;
import static org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper.KEY_MESSAGE_SENDERID;

public class ComposeMessageActivity extends ListActivity implements AbsListView.OnScrollListener {
    private MessagesListAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        long userId = getIntent().getExtras().getLong(UserapiDatabaseHelper.KEY_MESSAGE_SENDERID,
                getIntent().getExtras().getLong(UserapiDatabaseHelper.KEY_USER_USERID, -1));
        if (userId == -1) {
            userId = Long.parseLong(getIntent().getData().getLastPathSegment()); // toDo new
        }

        setContentView(R.layout.messages_compose);
        adapter = new MessagesListAdapter(this, R.layout.message_thread_row, managedQuery(UserapiProvider.MESSAGES_URI, null, KEY_MESSAGE_SENDERID + "=?" + " OR " + KEY_MESSAGE_RECEIVERID + "=?", new String[]{String.valueOf(userId), String.valueOf(userId)}, UserapiDatabaseHelper.KEY_MESSAGE_DATE + " ASC"));
        setListAdapter(adapter);
        getListView().setStackFromBottom(true);
        getListView().setOnScrollListener(this);
        final TextView textView = (TextView) findViewById(R.id.mess_to_send);
        final long finalUserId = userId;
        findViewById(R.id.send_reply).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                try {
                    ServiceHelper.getService().sendMessage(textView.getText().toString(), finalUserId);
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.message_sent), Toast.LENGTH_SHORT).show();
                    textView.setText("");
                    
                    //todo: scroll
                } catch (RemoteException e) {
                    e.printStackTrace();
                    AppHelper.showFatalError(ComposeMessageActivity.this, "While trying to send the message");
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