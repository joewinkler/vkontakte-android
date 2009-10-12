package org.googlecode.vkontakte_android;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;
import org.googlecode.vkontakte_android.database.StatusDao;

import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;


public class UpdatesListAdapter extends ResourceCursorAdapter {
    public static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm ");//todo: get rid of extra space by using padding(?)

    public UpdatesListAdapter(Context context, int layout, Cursor cursor) {
        super(context, layout, cursor);
    }

    @Override
    public void bindView(View view, Context context, final Cursor cursor) {
        StatusDao status = new StatusDao(cursor);
        TextView nameLine = (TextView) view.findViewById(R.id.name);
        nameLine.setText(status.getUserName());
        TextView statusLine = (TextView) view.findViewById(R.id.status);
        statusLine.setText(reformatText(status.getText()));
        TextView timeLine = (TextView) view.findViewById(R.id.time);
        timeLine.setText(timeFormat.format(status.getDate()));
        try {
            Bitmap bm = UserHelper.getPhotoByUserId(context, status.getUserId());
            ImageView photo = (ImageView) view.findViewById(R.id.photo);
            photo.setImageBitmap(bm);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private String reformatText(String text) {
        return text.replaceAll("&quot;", "\"");
    }

}