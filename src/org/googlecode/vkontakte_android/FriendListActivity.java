package org.googlecode.vkontakte_android;


import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.TabHost;

public class FriendListActivity extends TabActivity {
    public static final int ALL = 1;
    public static final int ONLINE = ALL + 1;
    public static final int REQUESTS = ONLINE + 1;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.friends_view);

        final TabHost tabHost = getTabHost();
        Intent allFriends = new Intent(this, FriendsListTabActivity.class);
        allFriends.putExtra("type", ALL);
        Intent onlineFriends = new Intent(this, FriendsListTabActivity.class);
        onlineFriends.putExtra("type", ONLINE);
        Intent requestFriends = new Intent(this, FriendsListTabActivity.class);
        requestFriends.putExtra("type", REQUESTS);

        tabHost.addTab(tabHost.newTabSpec("All").setIndicator(
                "All").setContent(
                allFriends));

        tabHost.addTab(tabHost.newTabSpec("Requests").setIndicator(
                "Requests").setContent(
                onlineFriends));

        tabHost.addTab(tabHost.newTabSpec("Online").setIndicator(
                "Online").setContent(
                requestFriends));

    }


//    private void initializeUserStuff() {
//        // todo: possibly move to tabs activities itself
//        final TextView friendsCounter = TabHelper.injectTabCounter(getTabWidget(), 1, getApplicationContext());
//
//        // todo: register/unregister onResume/onPause
//        // users content
//        getContentResolver().registerContentObserver(UserapiProvider.USERS_URI, false, new ContentObserver(new Handler()) {
//            @Override
//            public void onChange(boolean b) {
//
//                Cursor cursor = managedQuery(UserapiProvider.USERS_URI, null, UserapiDatabaseHelper.KEY_USER_NEW + "=1", null, null);
//                if (cursor.getCount() == 0)
//                    friendsCounter.setVisibility(View.INVISIBLE);
//                else {
//                    friendsCounter.setText(String.valueOf(cursor.getCount()));
//                    friendsCounter.setVisibility(View.VISIBLE);
//                }
//                setProgressBarIndeterminateVisibility(false);
//            }
//        });
//        getContentResolver().notifyChange(UserapiProvider.USERS_URI, null);
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.main_menu, menu);
//        return super.onCreateOptionsMenu(menu);
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.refresh:
//                switch (getTabHost().getCurrentTab()) {
//                    case MY_FRIENDS:
//                        refresh(contentToUpdate.FRIENDS);
//                        break;
//                    case MY_MESSAGES:
//                        refresh(contentToUpdate.MESSAGES_ALL);
//                        break;
//                    case MY_UPDATES:
//                        refresh(contentToUpdate.STATUSES);
//                        break;
//                    default:
//                        refresh(contentToUpdate.ALL);
//                }
//                return true;
//            case R.id.settings:
//                startActivity(new Intent(this, CSettings.class));
//                return true;
//            case R.id.about:
//                AboutDialog.makeDialog(this).show();
//
//                return true;
//            case R.id.logout:
//                try {
//                    m_vkService.logout();
//                    m_vkService.stop();
//                } catch (RemoteException e) {
//                    e.printStackTrace();
//                }
//                unbindService(m_connection);
//                finish();
//                return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
}