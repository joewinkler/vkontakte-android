package org.googlecode.vkontakte_android;

import android.app.Activity;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TableLayout;

import android.widget.AbsListView;

import java.util.List;
import java.io.IOException;

import org.googlecode.userapi.Message;
import org.googlecode.userapi.VkontakteAPI;
import org.json.JSONException;


public class CMessagesTab extends ListActivity implements AbsListView.OnScrollListener {
    private MessagesListAdapter adapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messages); 
        adapter = new MessagesListAdapter(this, R.layout.messages_record);
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