package org.googlecode.vkontakte_android;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TableLayout;

import java.util.List;
import java.io.IOException;

import org.googlecode.userapi.Message;
import org.googlecode.userapi.VkontakteAPI;
import org.json.JSONException;

public class CMessagesTab extends Activity {
    private VkontakteAPI api;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

       setContentView(R.layout.messages);
       TableLayout table = (TableLayout) findViewById(R.id.messages_pane);



        //test
        View v1 = CMessageFactory.getMessageView(this, "Friend #1", " message -  message -  message -  message -  message -  message -  message -  message -  message   ");
        View v2 = CMessageFactory.getMessageView(this, "Friend #1", " message -  message -  message -  message -  message -  message -  message -  message -  message   ");
        View v3 = CMessageFactory.getMessageView(this, "Friend #1", " message -  message -  message -  message -  message -  message -  message -  message -  message   ");
        View v4 = CMessageFactory.getMessageView(this, "Friend #1", " message -  message -  message -  message -  message -  message -  message -  message -  message   ");
        View v5 = CMessageFactory.getMessageView(this, "Friend #1", " message -  message -  message -  message -  message -  message -  message -  message -  message   ");
        View v6 = CMessageFactory.getMessageView(this, "Friend #1", " message -  message -  message -  message -  message -  message -  message -  message -  message   ");
        View v7 = CMessageFactory.getMessageView(this, "Friend #1", " message -  message -  message -  message -  message -  message -  message -  message -  message   ");

        table.addView(v1);
        table.addView(v2);
        table.addView(v3);
        table.addView(v4);
        table.addView(v5);
        table.addView(v6);
        table.addView(v7);

    }

}
