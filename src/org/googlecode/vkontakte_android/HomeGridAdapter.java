package org.googlecode.vkontakte_android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.googlecode.vkontakte_android.provider.UserapiProvider;

import java.util.ArrayList;
import java.util.List;

public class HomeGridAdapter extends BaseAdapter {

    public enum ItemType {PROFILE, FRIENDS, MESSAGES, PHOTOS, UPDATES, REQUESTS, SEARCH, SETTINGS, HELP}

    private List<Item> items;

    private Context context;

    public HomeGridAdapter(Context context, List<Item> items) {
        this.items = items;
        this.context = context;
    }

    public HomeGridAdapter(Context context) {
        items = new ArrayList<Item>();

        Intent intent;
        
        intent = new Intent(context, ProfileViewActivity.class);
        items.add(new Item(ItemType.PROFILE, context.getString(R.string.my_profile), R.drawable.my_profile,intent));

        intent = new Intent(context, FriendListActivity.class).putExtra("type", FriendListActivity.ALL);
        items.add(new Item(ItemType.FRIENDS, context.getString(R.string.friends), R.drawable.my_friends,intent));

        intent = new Intent(Intent.ACTION_VIEW, UserapiProvider.MESSAGES_URI);
        items.add(new Item(ItemType.MESSAGES, context.getString(R.string.messages), R.drawable.my_messages, intent));

        items.add(new Item(ItemType.PHOTOS, context.getString(R.string.photos), R.drawable.my_photos));

        intent = new Intent(context, UpdatesListActivity.class);
        items.add(new Item(ItemType.UPDATES, context.getString(R.string.updates), R.drawable.my_updates, intent));

        intent = new Intent(context, FriendListActivity.class).putExtra("type", FriendListActivity.REQUESTS);
        items.add(new Item(ItemType.REQUESTS, context.getString(R.string.requests), R.drawable.my_requests,intent));

        items.add(new Item(ItemType.SEARCH, context.getString(R.string.search), R.drawable.my_search));

        intent = new Intent(context, Settings.class);
        items.add(new Item(ItemType.SETTINGS, context.getString(R.string.settings), R.drawable.my_settings, intent));

        items.add(new Item(ItemType.HELP, context.getString(R.string.help), R.drawable.my_help));

        this.context = context;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        if (position < items.size())
            return items.get(position);
        else
            return null;
    }
    
    @Override
    public long getItemId(int position) {
        if (position < items.size())
            return items.get(position).getType().ordinal();
        else
            return -1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position >= items.size())
            return null;

        LinearLayout ly = (LinearLayout) ((Activity) context).getLayoutInflater().inflate(R.layout.homegrid_cell, null);

        TextView tv = (TextView) ly.getChildAt(1);
        tv.setText(getItem(position).toString());

        ImageView iv = (ImageView) ly.getChildAt(0);
        iv.setImageResource(items.get(position).getImageId());

        return ly;
    }

    public class Item {

        private ItemType type;
        private String title;
        private int imageId;
        private Intent intent;

        public Item(ItemType type, String title, int imageId) {
            this(type, title, imageId, null);
        }

        public Item(ItemType type, String title, int imageId, Intent intent) {
            this.type = type;
            this.title = title;
            this.imageId = imageId;
            this.intent = intent;
        }

        public ItemType getType() {
            return type;
        }

        public void setType(ItemType type) {
            this.type = type;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public int getImageId() {
            return imageId;
        }

        public void setImageId(int imageId) {
            this.imageId = imageId;
        }

        public Intent getIntent() {
            return intent;
        }

        public void setIntent(Intent intent) {
            this.intent = intent;
        }

        @Override
        public String toString() {
            return title;
        }
    }

}
