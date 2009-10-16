package org.googlecode.vkontakte_android;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;
import org.googlecode.vkontakte_android.database.MessageDao;
import org.googlecode.vkontakte_android.database.UserDao;
import static org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper.KEY_USER_USERID;
import org.googlecode.vkontakte_android.provider.UserapiProvider;


public class MessagesListAdapter extends ResourceCursorAdapter {
    private static final String TAG = "MessagesListAdapter";

    public MessagesListAdapter(Context context, int layout, Cursor cursor) {
        super(context, layout, cursor);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        MessageDao messageDao = new MessageDao(cursor);
        
        //TODO optimize
        String header = "From ";
        MessageDao md = new MessageDao(cursor);

        Long senderid = md.getSenderId();
        Long receiverid = md.getReceiverId();

        header += getNameById(context, senderid);
        header += " to ";
        header += getNameById(context, receiverid);


        TextView name = (TextView) view.findViewById(R.id.name);
        name.setText(header);
        TextView message = (TextView) view.findViewById(R.id.message);
//        message.setText(Html.fromHtml(messageDao.text));
        message.setText(messageDao.text);
        View indicator = view.findViewById(R.id.unread_indicator);
        if (!messageDao.read) indicator.setVisibility(View.VISIBLE);
        else indicator.setVisibility(View.INVISIBLE);
    }

    private String getNameById(Context context, Long userid) {
        String username = "";
        Cursor sc = context.getContentResolver().query(
                UserapiProvider.USERS_URI, null, KEY_USER_USERID + "=?",
                new String[]{userid.toString()}, null);
        if (sc.moveToNext()) {
            UserDao ud = new UserDao(sc);
            if (ud.userName == null) {
                if (ud.userId == CSettings.myId) {
                    username = "me";
                } else {
                    username = userid.toString();
                }
            } else {
                username = ud.userName;
            }
        } else {
            Log.e(TAG, "No such user in DB");
            username = userid.toString();
        }
        sc.close();
        return username;
    }
}