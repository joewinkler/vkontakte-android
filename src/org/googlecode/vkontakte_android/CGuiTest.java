package org.googlecode.vkontakte_android;

import android.app.TabActivity;
import android.content.Intent;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.*;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.MenuInflater;
import android.view.MenuItem.OnMenuItemClickListener;
import android.util.Log;
import org.googlecode.userapi.VkontakteAPI;
import org.googlecode.userapi.User;
import org.googlecode.userapi.ListWithTotal;
import org.googlecode.vkontakte_android.database.UserDao;
import org.googlecode.vkontakte_android.service.CheckingService;

import static org.googlecode.vkontakte_android.provider.UserapiProvider.USERS_URI;
import org.json.JSONException;

import java.io.IOException;
import java.util.List;
import java.util.LinkedList;

public class CGuiTest extends TabActivity {
    public static VkontakteAPI api;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        api = new VkontakteAPI();

        final LoginDialog ld = new LoginDialog(this);
        ((EditText) ld.findViewById(R.id.login)).setText("fake4test@gmail.com");
        ((EditText) ld.findViewById(R.id.pass)).setText("qwerty");
        ld.show();
        ld.setOnClick(new View.OnClickListener() {
            public void onClick(View view) {
                try {
                    String login = ld.getLogin();
                    String pass = ld.getPass();
                    Log.w(login, pass);
                    if (api.login(login, pass)) {
                        ld.dismiss();
                        refresh();
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

                        tabHost.addTab(tabHost.newTabSpec("Messages")
                                .setIndicator(getResources().getString(R.string.messages))
                                .setContent(new Intent(CGuiTest.this, CMessagesTab.class)));
                        tabHost.addTab(tabHost.newTabSpec("Messages")
                                .setIndicator(getResources().getString(R.string.messages))
                                .setContent(new Intent(CGuiTest.this, CMessagesTab.class)));

                        //todo: remove - just P-o-C here
                        TextView tv = new TextView(getApplicationContext());
                        tv.setText("321");
                        setTabIndicatorView(getTabWidget(), 2, tv);
                        ////////////////////////////////
                    } else {
                        Toast.makeText(getApplicationContext(), "login/pass incorrect", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });


    }

    public void setTabIndicatorView(TabWidget tabWidget, int i, View view) {
        if (tabWidget.getChildAt(i) instanceof RelativeLayout) {
            RelativeLayout relativeLayout = (RelativeLayout) tabWidget.getChildAt(i);
            for (int j = 0; j < relativeLayout.getChildCount(); j++) {
                if (relativeLayout.getChildAt(j) instanceof ImageView) {
                    relativeLayout.removeViewAt(j);
                    //todo: make compound with that remain ImageView and take only TextView as parameter
                    relativeLayout.addView(view, j);
                    break;
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                refresh();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void refresh() {
        Log.d("s", "start!!!");
        Toast.makeText(this, "Update started", Toast.LENGTH_SHORT).show();
        startService(new Intent(this, CheckingService.class));
    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//
//      MenuItem menuitem1 = menu.add(Menu.NONE, 1, Menu.NONE, "Start service");
//      menuitem1.setOnMenuItemClickListener(new OnMenuItemClickListener() {
//
//       @Override
//       public boolean onMenuItemClick(MenuItem item) {
//        Log.d("s", "start!!!");
//        CGuiTest.this.startService(new Intent(CGuiTest.this, CheckingService.class));
//        return false;
//       }
//
//      });

//      MenuItem menuitem2 = menu.add(Menu.NONE, 2, Menu.NONE, "Stop service");
//      menuitem2.setOnMenuItemClickListener(new OnMenuItemClickListener() {
//
//       @Override
//       public boolean onMenuItemClick(MenuItem item) {
//        Log.d("s", "stop!!!");
//        CGuiTest.this.stopService(new Intent(CGuiTest.this, CheckingService.class));
//        return false;
//       }
//
//      });
//
//      return super.onCreateOptionsMenu(menu);
//     }
//
//    
}