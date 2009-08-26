package org.googlecode.vkontakte_android;

import android.app.TabActivity;
import android.content.Intent;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.Gallery;
import android.widget.Toast;
import android.view.View;
import org.googlecode.userapi.VkontakteAPI;

import java.io.IOException;

public class CGuiTest extends TabActivity {
    public static VkontakteAPI api;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        api = new VkontakteAPI();
        final LoginDialog ld = new LoginDialog(this);
        ((EditText)ld.findViewById(R.id.login)).setText("fake4test@gmail.com");
        ((EditText)ld.findViewById(R.id.pass)).setText("qwerty");
        ld.show();
        ld.setOnClick(new View.OnClickListener() {
            public void onClick(View view) {
                try {
                    if (api.login(ld.getLogin(), ld.getPass())) {
                        ld.dismiss();
// load icons from the files
                        CImagesManager.loadImages(CGuiTest.this);

                        final TabHost tabHost = getTabHost();

                        tabHost.addTab(tabHost.newTabSpec("I")
                                .setIndicator(getResources().getString(R.string.i))
                                .setContent(new Intent(CGuiTest.this, CMeTab.class)));

                        tabHost.addTab(tabHost.newTabSpec("Friends")
                                .setIndicator(getResources().getString(R.string.friends))
                                .setContent(new Intent(CGuiTest.this, CFriendsTab.class)));

                        tabHost.addTab(tabHost.newTabSpec("Messages")
                                .setIndicator(getResources().getString(R.string.messages))
                                .setContent(new Intent(CGuiTest.this, CMessagesTab.class)));
                        
                    } else {
                        Toast.makeText(getApplicationContext(), "login/pass incorrect", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        

    }
}