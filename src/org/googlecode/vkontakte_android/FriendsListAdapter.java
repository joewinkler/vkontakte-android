package org.googlecode.vkontakte_android;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import org.googlecode.vkontakte_android.database.MessageDao;
import org.googlecode.vkontakte_android.database.UserDao;
import org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper;


public class FriendsListAdapter extends ResourceCursorAdapter {
    private Context context;
    private boolean loading = false;

    public FriendsListAdapter(Context context, int layout, Cursor cursor) {
        super(context, layout, cursor);
        this.context = context;
    }

    @Override
    public void bindView(View view, Context context, final Cursor cursor) {
        UserDao userDao = new UserDao(cursor);
//    	Bitmap bm = null;
//    	try {
//            byte[] photoByteArray = userDao.getUserPhoto();
//            bm = BitmapFactory.decodeByteArray(photoByteArray, 0, photoByteArray.length);
//        } catch (IOException e) {
//            e.printStackTrace(); 
//        } 
        TextView name = (TextView) view.findViewById(R.id.name);
        TextView status = (TextView) view.findViewById(R.id.status);
        name.setText(userDao.getUserName());
        String statusText = "";
        if (userDao.isNewFriend()) statusText+="new ";
        if (userDao.isOnline()) statusText+="online";
        else statusText+="offline";
        status.setText(statusText);
        
        ImageButton send = (ImageButton)view.findViewById(R.id.send_message);
        send.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {

		        UserDao userdao = new UserDao(cursor);
		        Intent intent = new Intent(FriendsListAdapter.this.context, ComposeMessageActivity.class);
		        intent.putExtra(UserapiDatabaseHelper.KEY_USER_USERID, userdao.getUserId());
		        FriendsListAdapter.this.context.startActivity(intent);
				
			}
        	
        });
        
        
    }
}