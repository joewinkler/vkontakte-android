package org.googlecode.vkontakte_android;




import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import org.googlecode.vkontakte_android.CImagesManager.Icons;
import org.googlecode.vkontakte_android.database.UserDao;
import org.googlecode.vkontakte_android.utils.PreferenceHelper;
import org.googlecode.vkontakte_android.utils.UserHelper;


public class FriendsListAdapter extends ResourceCursorAdapter {
    private static final String TAG = "VK:FriendsListAdapter";
    
    private   String ONLINE_STATUS;
    private   String OFFLINE_STATUS;
    
    
    public FriendsListAdapter(Context context, int layout, Cursor cursor) {
    	super(context, layout, cursor);
    	ONLINE_STATUS=context.getResources().getString(R.string.status_online);
    	OFFLINE_STATUS=context.getResources().getString(R.string.status_offline);
    	//fillPhotoCache(context, cursor);
    	
    }
    
    
    @SuppressWarnings("unused")
	private void fillPhotoCache(Context context,Cursor cursor){
    	while (cursor.moveToNext()){
    		UserDao userDao = new UserDao(cursor);
    		UserHelper.getPhotoByUser2(context, userDao);
    	}
    	Log.d(TAG,"photos cached:"+UserHelper.bitmapCache.size());
    }
    

    @Override
    public void bindView(View view, Context context, final Cursor cursor) {
     //   Long startViewtime=java.lang.System.currentTimeMillis();
    	UserDao userDao = new UserDao(cursor);
        TextView name = (TextView) view.findViewById(R.id.name);
        TextView status = (TextView) view.findViewById(R.id.status);
        name.setText(userDao.userName);
        status.setText(userDao.online?ONLINE_STATUS:OFFLINE_STATUS);

        //if (userDao.newFriend) {view.findViewById(R.id.indicator).setVisibility(View.VISIBLE);
        //} else view.findViewById(R.id.indicator).setVisibility(View.INVISIBLE);

        String photoViewTag="photoview"+userDao.userId;
        ImageView photo = (ImageView)view.findViewWithTag(photoViewTag);
        if (photo==null){
        	photo = (ImageView) view.findViewById(R.id.photo);
        	photo.setTag(photoViewTag);
        }
        
        if (PreferenceHelper.shouldLoadPics(context)) {
        	photo.setImageBitmap(UserHelper.getPhotoByUser2(context, userDao));
        } else {
            photo.setImageBitmap(CImagesManager.getBitmap(context, Icons.STUB));
            photo.setVisibility(View.GONE);
        }
       // Long diffTime=java.lang.System.currentTimeMillis()-startViewtime;
        //Log.d(TAG,"BindView done:"+diffTime);
    }
}