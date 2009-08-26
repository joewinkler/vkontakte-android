package org.googlecode.vkontakte_android;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TextView;
import static org.googlecode.vkontakte_android.R.id.updates_counter;
import org.googlecode.userapi.VkontakteAPI;
import org.googlecode.userapi.ChangesHistory;
import org.json.JSONException;

import java.io.IOException;

public class CMeTab extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        setContentView(R.layout.main);

        TextView updates = (TextView) findViewById(updates_counter);
        VkontakteAPI api = CGuiTest.api;
        try {
            ChangesHistory history = api.getChangesHistory();
            updates.setText("messages: "+history.getMessagesCount()+", friends: "+history.getFriendsCount()+", photos: "+history.getPhotosCount());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();  
        }

        ImageButton b = (ImageButton) findViewById(R.id.ImageButton01);
        b.setImageDrawable(new BitmapDrawable(CImagesManager.getBitmap("ok")));
        
        TableLayout table = (TableLayout) findViewById(R.id.Wall);
        
        
        //test
        View v1 = CWallMessageFactory.getMessageView(this, "Friendname", "message message message message message message message message message message message message message ");
        View v2 = CWallMessageFactory.getMessageView(this, "Friendname", "message message message message message message message message message message message message message ");
        View v3 = CWallMessageFactory.getMessageView(this, "Friendname","message message message message message message message message message message message message message");
        View v4 = CWallMessageFactory.getMessageView(this, "Friendname", "message message message message message message message message message message message message message");
               
        table.addView(v1);
        table.addView(v2);
        table.addView(v3);
        table.addView(v4);
		
	}

}
