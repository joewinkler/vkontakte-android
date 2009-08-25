package org.googlecode.vkontakte_android;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import org.googlecode.userapi.User;
import org.googlecode.userapi.VkontakteAPI;
import org.googlecode.vkontakte_android.R;
import org.json.JSONException;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class FriendsListAdapter extends BaseAdapter {

    private List<User> friends = new LinkedList<User>();
    private Context context;
    private int layout;
    private LayoutInflater layoutInflater;
    private boolean loading = false;

    public int getCount() {
        return friends.size();
    }

    public Object getItem(int pos) {
        return (Object) pos;
    }

    public long getItemId(int pos) {
        return pos;
    }

    public FriendsListAdapter(Context context, int layout) {
        this.context = context;
        this.layout = layout;
        layoutInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        VkontakteAPI api = CGuiTest.api;
        try {
            friends = api.getFriends(api.id, 0, 15, VkontakteAPI.friendsTypes.friends);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (JSONException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public View getView(int pos, View v, ViewGroup p) {
        View view = layoutInflater.inflate(layout, null);
        TextView name = (TextView) view.findViewById(R.id.name);
        name.setText(friends.get(pos).getUserName());
        return view;
    }


    public void prepareData() {
//        this.count += 1;
//        loading = true;
//        this.notifyDataSetChanged();
    }
}