package org.googlecode.vkontakte_android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import org.googlecode.vkontakte_android.utils.PropertiesHolder;

import java.util.ArrayList;


public class ProfileInfoAdapter extends BaseAdapter {
    private ArrayList<PropertiesHolder> propertiesHolder;
    private LayoutInflater mInflater;

    public ProfileInfoAdapter(Context context, ArrayList<PropertiesHolder> arrayList) {
        mInflater = LayoutInflater.from(context);
        propertiesHolder = arrayList;
    }

    @Override
    public int getCount() {
        return propertiesHolder.size();
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View v = mInflater.inflate(R.layout.profile_view_item, null);
        ((TextView) v.findViewById(R.id.property_name)).setText(propertiesHolder.get(i).getProperty());
        ((TextView) v.findViewById(R.id.property)).setText(propertiesHolder.get(i).getMeaning());
        return v;
    }

}
