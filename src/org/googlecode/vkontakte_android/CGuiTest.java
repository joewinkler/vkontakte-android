package org.googlecode.vkontakte_android;


import android.app.TabActivity;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.*;
import android.util.Log;
import android.view.*;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper;
import org.googlecode.vkontakte_android.provider.UserapiProvider;
import org.googlecode.vkontakte_android.service.CheckingService;
import org.googlecode.vkontakte_android.service.CheckingService.contentToUpdate;
import org.googlecode.vkontakte_android.utils.ServiceHelper;


public class CGuiTest extends TabActivity {

    private static String TAG = "VK:Old UI";
   
    //todo: use map(?)
   // public static final int MY_PAGE = 0;
    public static final int MY_FRIENDS = 1;
   // public static final int MY_MESSAGES = 2;
   // public static final int MY_UPDATES = 3;


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
  
        initializeActivity();
        initializeUserStuff();
   
        // For calls from HomeGrid
        if (getIntent().hasExtra("tabToShow")) {
            getTabHost().setCurrentTabByTag(getIntent().getStringExtra("tabToShow"));
        }

    }
    private void initializeActivity() {
        //refresh(contentToUpdate.ALL);
        // load icons from the files
        setContentView(R.layout.friends_view);
        final TabHost tabHost = getTabHost();
       
        /*
        tabHost.addTab(tabHost.newTabSpec("My Profile").setIndicator(
                getResources().getString(R.string.i)).setContent(
                new Intent(CGuiTest.this, CMeTab.class)));
                */

        tabHost.addTab(tabHost.newTabSpec("Friends").setIndicator(
                getResources().getString(R.string.friends),
                getResources().getDrawable(R.drawable.ic_menu_friendslist))
                .setContent(new Intent(CGuiTest.this, FriendsListTabActivity.class)));

      /*
        tabHost.addTab(tabHost.newTabSpec("Messages").setIndicator(
                getResources().getString(R.string.messages)).setContent(
                new Intent(CGuiTest.this, MessagesListTabActivity.class)));
*/
        /*
        tabHost.addTab(tabHost.newTabSpec("Updates").setIndicator(
                getResources().getString(R.string.updates)).setContent(
                new Intent(CGuiTest.this, UpdatesListTabActivity.class)));
                */
    }


    private void initializeUserStuff() {
        // todo: possibly move to tabs activities itself
        final TextView friendsCounter = TabHelper.injectTabCounter(getTabWidget(), 1, getApplicationContext());
        //final TextView messagesCounter = TabHelper.injectTabCounter(getTabWidget(), 0, getApplicationContext());

        // todo: register/unregister onResume/onPause
        // users content
        getContentResolver().registerContentObserver(UserapiProvider.USERS_URI, false, new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean b) {

                Cursor cursor = managedQuery(UserapiProvider.USERS_URI, null, UserapiDatabaseHelper.KEY_USER_NEW + "=1", null, null);
                if (cursor.getCount() == 0)
                    friendsCounter.setVisibility(View.INVISIBLE);
                else {
                    friendsCounter.setText(String.valueOf(cursor.getCount()));
                    friendsCounter.setVisibility(View.VISIBLE);
                }
                setProgressBarIndeterminateVisibility(false);
            }
        });
        getContentResolver().notifyChange(UserapiProvider.USERS_URI, null);

        
        /*
        //messages content
        getContentResolver().registerContentObserver(UserapiProvider.MESSAGES_URI, false, new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean b) {
                Cursor cursor = managedQuery(UserapiProvider.MESSAGES_URI, null,
                        UserapiDatabaseHelper.KEY_MESSAGE_READ + "=?",
                        new String[]{"0"},
                        null);
                if (cursor.getCount() == 0)
                    messagesCounter.setVisibility(View.INVISIBLE);
                else {
                    messagesCounter.setText(String.valueOf(cursor.getCount()));
                    messagesCounter.setVisibility(View.VISIBLE);
                }
                setProgressBarIndeterminateVisibility(false);
            }
        });
        getContentResolver().notifyChange(UserapiProvider.MESSAGES_URI, null);
*/

        /*
        //statuses content
        getContentResolver().registerContentObserver(UserapiProvider.STATUSES_URI, false, new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean b) {
                setProgressBarIndeterminateVisibility(false);
            }
        });
        getContentResolver().notifyChange(UserapiProvider.STATUSES_URI, null);
        
        */
        /* 
        try {
            CMeTab.s_instance.loadProfile();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        */
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
                switch (getTabHost().getCurrentTab()) {
                    case MY_FRIENDS:
                        refresh(contentToUpdate.FRIENDS);
                        break;
                    /*
                    case MY_MESSAGES:
                        refresh(contentToUpdate.MESSAGES_ALL);
                        break;
                        */
                  
                    /*    
                    case MY_UPDATES:
                        refresh(contentToUpdate.STATUSES);
                        break;
                        */
                        
                    default:
                        refresh(contentToUpdate.ALL);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Makes Service to refresh given content
     */
    private void refresh(CheckingService.contentToUpdate what) {
        Log.d(TAG, "request to refresh");
        //Toast.makeText(this, "Update started", Toast.LENGTH_SHORT).show();
        setProgressBarIndeterminateVisibility(true);

        try {
        	ServiceHelper.getService().update(what.ordinal(), false);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        //startService(new Intent(this, CheckingService.class).putExtra("action", what.ordinal()));

    }

    public  void fatalError(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
   
}