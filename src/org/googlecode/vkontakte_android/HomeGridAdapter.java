package org.googlecode.vkontakte_android;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class HomeGridAdapter extends BaseAdapter {

    private ArrayList<Integer> mCellTitles;
    private ArrayList<Integer> mCellImages;
    private Context mContext;

    public HomeGridAdapter(Context context, ArrayList<Integer> cell_titles, ArrayList<Integer> cell_images) {
        this.mCellTitles = cell_titles;
        this.mCellImages = cell_images;
        this.mContext = context;
    }

    public HomeGridAdapter(Context context) {
        mCellTitles = new ArrayList<Integer>();
        mCellTitles.add(R.string.my_profile);
        mCellTitles.add(R.string.friends);
        mCellTitles.add(R.string.messages);
        mCellTitles.add(R.string.photos);
        mCellTitles.add(R.string.updates);
        mCellTitles.add(R.string.requests);
        mCellTitles.add(R.string.search);
        mCellTitles.add(R.string.settings);
        mCellTitles.add(R.string.help);

        mCellImages = new ArrayList<Integer>();
        mCellImages.add(R.drawable.my_profile);
        mCellImages.add(R.drawable.my_friends);
        mCellImages.add(R.drawable.my_messages);
        mCellImages.add(R.drawable.my_photos);
        mCellImages.add(R.drawable.my_updates);
        mCellImages.add(R.drawable.my_requests);
        mCellImages.add(R.drawable.my_search);
        mCellImages.add(R.drawable.my_settings);
        mCellImages.add(R.drawable.my_help);

        this.mContext = context;
    }

    @Override
    public int getCount() {
        return this.mCellTitles.size();
    }

    @Override
    public Object getItem(int position) {
        if (position < getCount()) {
            return mContext.getResources().getString(this.mCellTitles.get(position));
        } else {
            return "title not found";
        }
    }
    
    public Object getItemResourceId(int position) {
        if (position < getCount()) {
            return this.mCellTitles.get(position);
        } else {
            return "title not found";
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LinearLayout ly = (LinearLayout) ((Activity) mContext).getLayoutInflater().inflate(R.layout.homegrid_cell, null);

        TextView tv = (TextView) ly.getChildAt(1);
        tv.setText((String) getItem(position));

        ImageView iv = (ImageView) ly.getChildAt(0);
        iv.setImageResource(mCellImages.get(position));

        ly.setTag(getItemResourceId(position));

        return ly;
    }
}
