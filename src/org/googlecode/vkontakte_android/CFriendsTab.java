package org.googlecode.vkontakte_android;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.TextView;
import android.util.Log;
import android.view.View;

import java.io.IOException;

import org.json.JSONException;
import org.googlecode.userapi.VkontakteAPI;
import org.googlecode.vkontakte_android.database.MessageDao;
import org.googlecode.vkontakte_android.provider.UserapiProvider;
import org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper;
import static org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper.*;

public class CFriendsTab extends ListActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friend_list);
        FriendsListAdapter adapter = new FriendsListAdapter(this, R.layout.friend_row, managedQuery(UserapiProvider.USERS_URI, null, null, null,
                KEY_USER_NEW + " DESC, " + KEY_USER_ONLINE + " DESC"
        ));
        setListAdapter(adapter);

        //todo: use tabcounter
//        TextView tv = (TextView) findViewById(R.id.new_counter);
//        try {
//            long counter = CGuiTest.api.getChangesHistory().getFriendsCount();
//            tv.setText("new friends: " + counter);
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }

    }
}
