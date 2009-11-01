package org.googlecode.vkontakte_android;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import org.googlecode.vkontakte_android.CImagesManager.Icons;
import org.googlecode.vkontakte_android.database.StatusDao;

import java.text.SimpleDateFormat;

public class UpdatesListAdapter extends ResourceCursorAdapter {
	private static final String TAG = "org.googlecode.vkontakte_android.UpdatesListAdapter";
	
    public static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm ");//todo: get rid of extra space by using padding(?)
    public static final SimpleDateFormat weektimeFormat= new SimpleDateFormat("EEE, HH:mm ");
    private static final int PHOTO_SIZE = 90;
    
    public UpdatesListAdapter(Context context, int layout, Cursor cursor) {
        super(context, layout, cursor);
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
        
        if (CSettings.shouldLoadPics(context)) {
        	Bitmap bm = UserHelper.getPhotoByUserId(context, status.getUserId());
        	if (bm != null && view.findViewById(R.id.photo)!=null ) {
            	ImageView photo = (ImageView) view.findViewById(R.id.photo);
                int srcWidth = bm.getWidth();
                int srcHeight = bm.getHeight();
                int dstWidth = PHOTO_SIZE;
                int dstHeight = srcHeight * PHOTO_SIZE / srcWidth;
                //Bitmap scaledBitmap = Bitmap.createScaledBitmap(bm,dstWidth,dstHeight,true);
                Bitmap croppedBitmap = Bitmap.createBitmap(bm, 0, 0, Math.min(PHOTO_SIZE, dstWidth), Math.min(PHOTO_SIZE, dstHeight));
                photo.setImageBitmap(croppedBitmap);
            } else {
            	Log.e(TAG, "Can't get photo for status "+status.getStatusId());
            }
        } else {
        	ImageView photo = (ImageView) view.findViewById(R.id.photo);
            photo.setImageBitmap(CImagesManager.getBitmap(context, Icons.STUB));
            photo.setVisibility(View.GONE);
        }
        
        
        
    }
}