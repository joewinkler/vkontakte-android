package org.googlecode.vkontakte_android;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.graphics.BitmapFactory;
import org.googlecode.userapi.VkontakteAPI;
import org.googlecode.userapi.Photo;
import org.json.JSONException;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class PhotoListAdapter extends BaseAdapter {

    private List<Photo> photos = new LinkedList<Photo>();
    private Context context;
    private int layout;
    private LayoutInflater layoutInflater;
    private boolean loading = false;

    public int getCount() {
        return photos.size();
    }

    public Object getItem(int pos) {
        return (Object) pos;
    }

    public long getItemId(int pos) {
        return pos;
    }

    public PhotoListAdapter(Context context, int layout, VkontakteAPI api) {
        this.context = context;
        this.layout = layout;
        layoutInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        try {
        photos = api.getPhotos(api.myId, 0, 10, VkontakteAPI.photosTypes.photos);
        Log.w("photos:", photos.size() + "");
        notifyDataSetChanged();
    } catch (IOException e) {
        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    } catch (JSONException e) {
        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }
    }

    public View getView(int pos, View v, ViewGroup p) {
        ImageView view = new ImageView(context);
        byte[] image = new byte[0];
        try {
            image = photos.get(pos).getImage();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        view.setImageBitmap(BitmapFactory.decodeByteArray(image, 0, image.length));
        return view;
    }


    public void prepareData() {
//        this.count += 1;
//        loading = true;
//        this.notifyDataSetChanged();
    }
}