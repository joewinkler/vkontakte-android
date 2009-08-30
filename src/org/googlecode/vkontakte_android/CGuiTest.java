package org.googlecode.vkontakte_android;

import android.app.TabActivity;
import android.content.Intent;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.Gallery;
import android.widget.Toast;
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

        final Runnable updater = new Runnable() {
            public void run() {
                try {
//                    List<UserDao> userList = new LinkedList<UserDao>();
                    ListWithTotal<User> f = api.getFriends(api.id, 0, 512, VkontakteAPI.friendsTypes.friends);
                    getContentResolver().delete(USERS_URI, null, null);
                    for (User user : f.getList()) {
                        UserDao userDao = new UserDao(user.getUserId(), user.getUserName(), user.isMale(), user.isOnline(), false);
//                        userList.add(userDao);
                        userDao.saveOrUpdate(CGuiTest.this);
                    }
                    ListWithTotal<User> f1 = api.getFriends(api.id, 0, 512, VkontakteAPI.friendsTypes.friends_new);
                    for (User user : f1.getList()) {
                        UserDao userDao = new UserDao(user.getUserId(), user.getUserName(), user.isMale(), user.isOnline(), true);
//                        userList.add(userDao);
                        userDao.saveOrUpdate(CGuiTest.this);
                    }
//                    UserDao.bulkSave(CGuiTest.this, userList);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        };

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
                        new Thread(updater).start();//todo: move to service
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
        CGuiTest.this.startService(new Intent(CGuiTest.this, CheckingService.class));
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