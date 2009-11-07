package org.googlecode.vkontakte_android;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.widget.*;

import org.googlecode.vkontakte_android.CImagesManager.Icons;
import org.googlecode.vkontakte_android.database.UserDao;

import java.io.FileNotFoundException;


public class FriendsListAdapter extends ResourceCursorAdapter {
   private static final String TAG = "org.googlecode.vkontakte_android.FriendsListAdapter";

    public FriendsListAdapter(Context context, int layout, Cursor cursor) {
        super(context, layout, cursor);
    }

    @Override
    public void bindView(View view, Context context, final Cursor cursor) {
        UserDao userDao = new UserDao(cursor);
        TextView name = (TextView) view.findViewById(R.id.name);
        TextView status = (TextView) view.findViewById(R.id.status);
        name.setText(userDao.userName);
        String statusText = "";
        if (userDao.newFriend) {
            view.findViewById(R.id.indicator).setVisibility(View.VISIBLE);
        } else view.findViewById(R.id.indicator).setVisibility(View.INVISIBLE);

        if (userDao.online) statusText += context.getResources().getString(R.string.status_online);
        else statusText += context.getResources().getString(R.string.status_offline);
        status.setText(statusText);

        ImageView photo = (ImageView) view.findViewById(R.id.photo);
        if (CSettings.shouldLoadPics(context)) {
        	Log.d(TAG, "Setting photo for " + userDao.userName);
        	if (userDao.getUserPhotoUrl() != null) {
        		Bitmap bm = UserHelper.getPhotoByUserId(context, userDao.userId);
        		if (bm == null) {
        			Log.d(TAG, "Can't get photo of "+userDao.userName);
        			photo.setImageBitmap(CImagesManager.getBitmap(context, Icons.STUB));
                } else {
                    photo = (ImageView) view.findViewById(R.id.photo);
                    photo.setImageBitmap(bm);
                }
        	} else {
        		Log.e(TAG, "Error: no photo url for "+userDao.userName);
        	}
        } else {
            photo.setImageBitmap(CImagesManager.getBitmap(context, Icons.STUB));
            photo.setVisibility(View.GONE);
        }
    }
}