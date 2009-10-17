package org.googlecode.vkontakte_android;

import android.app.ListActivity;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import org.googlecode.userapi.Message;
import org.googlecode.vkontakte_android.database.MessageDao;
import org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper;
import static org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper.KEY_MESSAGE_RECEIVERID;
import static org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper.KEY_MESSAGE_SENDERID;
import org.googlecode.vkontakte_android.provider.UserapiProvider;
import org.googlecode.vkontakte_android.service.CheckingService;
import org.googlecode.vkontakte_android.service.IVkontakteService;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

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

        setContentView(R.layout.messages);
        adapter = new MessagesListAdapter(this, R.layout.message_row, managedQuery(UserapiProvider.MESSAGES_URI, null, KEY_MESSAGE_SENDERID + "=?" + " OR " + KEY_MESSAGE_RECEIVERID + "=?", new String[]{String.valueOf(userId), String.valueOf(userId)}, UserapiDatabaseHelper.KEY_MESSAGE_DATE+" ASC"));
        setListAdapter(adapter);
        getListView().setStackFromBottom(true);
        getListView().setOnScrollListener(this);
        final TextView textView = (TextView) findViewById(R.id.mess_to_send);
        final long finalUserId = userId;
        findViewById(R.id.send_reply).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                try {
                	IVkontakteService v = CGuiTest.s_instance.m_vkService;
                	v.sendMessage(textView.getText().toString(), finalUserId);
                    if (true) {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.message_sent), Toast.LENGTH_SHORT).show();
                        textView.setText("");
                        CGuiTest.s_instance.m_vkService.update(CheckingService.contentToUpdate.MESSAGES_OUT.ordinal());
                        //todo: scroll
                    }
                }  catch (RemoteException e) {
					Toast.makeText(ComposeMessageActivity.this, R.string.err_msg_fatal_error, Toast.LENGTH_SHORT);
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