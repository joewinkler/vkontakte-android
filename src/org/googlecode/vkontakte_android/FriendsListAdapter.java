package org.googlecode.vkontakte_android;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.widget.*;

import org.googlecode.vkontakte_android.database.UserDao;

import java.io.FileNotFoundException;


public class FriendsListAdapter extends ResourceCursorAdapter {
    private boolean loading = false;
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

        if (userDao.getUserPhotoUrl() != null) {
            Log.d(TAG, "setting photo");
            Bitmap bm = UserHelper.getPhoto(context, userDao.rowId);
            if (bm == null) {
                //todo: seems that photo was not downloaded - we should download it
            } else {
                ImageView photo = (ImageView) view.findViewById(R.id.photo);
                photo.setImageBitmap(bm);
            }
        } else {
            //todo: use default avatar
        }
    }
}