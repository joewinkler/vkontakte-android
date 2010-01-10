package org.googlecode.vkontakte_android;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;
import org.googlecode.vkontakte_android.CImagesManager.Icons;
import org.googlecode.vkontakte_android.database.UserDao;


public class FriendsListAdapter extends ResourceCursorAdapter {
    private static final String TAG = "VK:FriendsListAdapter";

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
        
        if (userDao.newFriend) {view.findViewById(R.id.indicator).setVisibility(View.VISIBLE);
        } else view.findViewById(R.id.indicator).setVisibility(View.INVISIBLE);

        if (userDao.online) statusText += context.getResources().getString(R.string.status_online);
        else statusText += context.getResources().getString(R.string.status_offline);
        status.setText(statusText);

        ImageView photo = (ImageView) view.findViewById(R.id.photo);
        
        if (Settings.shouldLoadPics(context)) {
            if (userDao.getUserPhotoUrl() != null) {
            	Bitmap bm=UserHelper.getPhotoByUser(context, userDao);
                if (bm == null) {
                    photo.setImageBitmap(CImagesManager.getBitmap(context, Icons.STUB));
                } else {
                    photo.setImageBitmap(bm);
                }
            } else {
                Log.e(TAG, "Error: no photo url for " + userDao.userName);
                photo.setImageBitmap(CImagesManager.getBitmap(context, Icons.STUB));
            }
        } else {
            photo.setImageBitmap(CImagesManager.getBitmap(context, Icons.STUB));
            photo.setVisibility(View.GONE);
        }
        
        
    }
}