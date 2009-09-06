package org.googlecode.vkontakte_android;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;
import org.googlecode.vkontakte_android.database.MessageDao;


public class MessagesListAdapter extends ResourceCursorAdapter {
    public MessagesListAdapter(Context context, int layout, Cursor cursor) {
        super(context, layout, cursor);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        MessageDao messageDao = new MessageDao(cursor);
        TextView name = (TextView) view.findViewById(R.id.name);
        name.setText(messageDao.getSenderId() + " ");
        TextView message = (TextView) view.findViewById(R.id.message);
        message.setText(messageDao.getText());
        View indicator = view.findViewById(R.id.unread_indicator);
        if (!messageDao.isRead()) indicator.setVisibility(View.VISIBLE);
        else indicator.setVisibility(View.INVISIBLE);
    }
}