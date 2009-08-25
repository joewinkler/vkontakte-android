package org.googlecode.vkontakte_android;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.AbsListView;

public class CFriendsTab extends ListActivity implements AbsListView.OnScrollListener {
    private FriendsListAdapter adapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friend_list);
        adapter = new FriendsListAdapter(this, R.layout.friend_row);
        setListAdapter(adapter);
        getListView().setOnScrollListener(this);
    }

    public void onScroll(AbsListView v, int i, int j, int k) {
    }

    public void onScrollStateChanged(AbsListView v, int state) {
        if (state == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && getListView().getLastVisiblePosition() == adapter.getCount() - 1) {
            adapter.prepareData();
        }
    }

}
