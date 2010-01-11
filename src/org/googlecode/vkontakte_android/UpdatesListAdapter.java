package org.googlecode.vkontakte_android;

import android.content.Context;
import android.database.Cursor;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;
import org.googlecode.vkontakte_android.CImagesManager.Icons;
import org.googlecode.vkontakte_android.database.StatusDao;
import org.googlecode.vkontakte_android.utils.PreferenceHelper;
import org.googlecode.vkontakte_android.utils.UserHelper;

import java.text.SimpleDateFormat;

public class UpdatesListAdapter extends ResourceCursorAdapter {
    private static final String TAG = "VK:UpdatesListAdapter";

    public static final  SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm ");//todo: get rid of extra space by using padding(?)
    public static final SimpleDateFormat weektimeFormat = new SimpleDateFormat("EEE, HH:mm ");
    
    public UpdatesListAdapter(Context context, int layout, Cursor cursor) {
        super(context, layout, cursor);
        //fillPhotoCache(context, cursor);
    }

    @SuppressWarnings("unused")
	private void fillPhotoCache(Context context,Cursor cursor){
    	while (cursor.moveToNext()){
    		StatusDao status = new StatusDao(cursor);
    		UserHelper.getPhotoByUserId2(context, status.getUserId());
    	}
    	Log.d(TAG,"photos cached:"+UserHelper.bitmapCache.size());
    }
    
    @Override
    public void bindView(View view, Context context, final Cursor cursor) {
        StatusDao status = new StatusDao(cursor);
        TextView nameLine = (TextView) view.findViewById(R.id.name);
        nameLine.setText(status.getUserName());
        TextView statusLine = (TextView) view.findViewById(R.id.status);
        statusLine.setText(Html.fromHtml(status.getText()));
        TextView timeLine = (TextView) view.findViewById(R.id.time);
        timeLine.setText(weektimeFormat.format(status.getDate()));
     
        //caching photoViews for faster access via findViewWithTag 
        String photoViewTag="photoview"+status.getUserId();
        ImageView photo = (ImageView)view.findViewWithTag(photoViewTag);
        if (photo==null){
        	photo = (ImageView) view.findViewById(R.id.photo);
        }
        if(photo!=null && PreferenceHelper.shouldLoadPics(context)){
        	photo.setTag(photoViewTag);
        	photo.setImageBitmap(UserHelper.getPhotoByUserId2(context, status.getUserId()));
        }else if(photo!=null) {
            photo.setImageBitmap(CImagesManager.getBitmap(context, Icons.STUB));
            photo.setVisibility(View.GONE);
        }
   }
}