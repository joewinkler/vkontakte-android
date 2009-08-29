package org.googlecode.vkontakte_android;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.AbsListView;
import android.widget.TextView;
import android.view.View;

import java.io.IOException;

import org.json.JSONException;
import org.googlecode.userapi.VkontakteAPI;
import org.googlecode.vkontakte_android.provider.UserapiProvider;
import org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper;
import static org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper.*;

public class CFriendsTab extends ListActivity implements AbsListView.OnScrollListener {
    private FriendsListAdapter adapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friend_list);
        adapter = new FriendsListAdapter(this, R.layout.friend_row, managedQuery(UserapiProvider.USERS_URI, null, null, null,
                KEY_USER_NEW + " DESC, " + KEY_USER_ONLINE + " DESC"
        ));
        setListAdapter(adapter);
        getListView().setOnScrollListener(this);
        TextView tv = (TextView) findViewById(R.id.new_counter);
        try {
            long counter = CGuiTest.api.getFriends(CGuiTest.api.id, 0, 0, VkontakteAPI.friendsTypes.friends_new).getCount();
            tv.setText("new friends: " + counter);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void onScroll(AbsListView v, int i, int j, int k) {
    }

    public void onScrollStateChanged(AbsListView v, int state) {
        if (state == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && getListView().getLastVisiblePosition() == adapter.getCount() - 1) {
//            adapter.prepareData();
        }
    }

}
