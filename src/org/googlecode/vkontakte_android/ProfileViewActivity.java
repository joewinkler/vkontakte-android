package org.googlecode.vkontakte_android;

import android.app.Activity;
import android.app.TabActivity;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.view.*;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;
import android.widget.TabHost.TabSpec;

import org.googlecode.vkontakte_android.database.ProfileDao;
import org.googlecode.vkontakte_android.database.UserDao;
import org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper;
import org.googlecode.vkontakte_android.provider.UserapiProvider;
import static org.googlecode.vkontakte_android.provider.UserapiProvider.PROFILES_URI;

public class ProfileViewActivity  extends Activity implements TabHost.TabContentFactory{
    private ContentObserver observer;
    private static final String TAG = "org.googlecode.vkontakte_android.ProfileViewActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        
        setContentView(R.layout.profile_view);
        
        final TabHost tabHost = (TabHost) findViewById(R.id.ProfileTabHost);
        
        tabHost.addTab(tabHost.newTabSpec("tab1")
                .setIndicator("tab1")
                .setContent(this));
        tabHost.addTab(tabHost.newTabSpec("tab2")
                .setIndicator("tab2")
                .setContent(this));
        tabHost.addTab(tabHost.newTabSpec("tab3")
                .setIndicator("tab3")
                .setContent(this));
        
        
        
        
        /*
        Bundle extras = getIntent().getExtras();
        long userId = CSettings.myId;
        if (extras != null)
            userId = extras.getLong(UserapiDatabaseHelper.KEY_PROFILE_USERID, userId);
        final long finalProfileId = userId;
        observer = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean b) {
                Cursor cursor = managedQuery(PROFILES_URI, null, UserapiDatabaseHelper.KEY_PROFILE_USERID + "=?", new String[]{String.valueOf(finalProfileId)}, null);
                if (cursor.getCount() == 0)
                    setProgressBarIndeterminateVisibility(true);
                else {
                    cursor.moveToFirst();
                    ProfileDao profile = new ProfileDao(cursor);
                    loadAndShowProfile(profile);
                }
            }
        };
        downloadProfile(userId);
        */
    }
//
//    private void downloadProfile(long userId) {
//        System.out.println("userId = " + userId);
//        try {
//            if (!CGuiTest.s_instance.m_vkService.loadProfile(userId,false)) {
//                Log.e(TAG, "Cannot load profile");
//            }
//        } catch (RemoteException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }
//
//    }
//
//    private void loadAndShowProfile(ProfileDao profile) {
//       // findViewById(R.id.main_profile_view).setVisibility(View.VISIBLE);
//        ((TextView) findViewById(R.id.firstname)).setText(profile.firstname);
//        ((TextView) findViewById(R.id.surname)).setText(profile.surname);
//        ((ImageView) findViewById(R.id.photo)).setImageBitmap(UserHelper.getPhotoByUserId(this, profile.id));
////        System.out.println(profile.status);
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//       // getContentResolver().registerContentObserver(PROFILES_URI, false, observer);
//      //  getContentResolver().notifyChange(PROFILES_URI, null);
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//       // getContentResolver().unregisterContentObserver(observer);
//    }
//
//    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
//        super.onCreateContextMenu(menu, v, menuInfo);
//        MenuInflater menuInflater = getMenuInflater();
//        menuInflater.inflate(R.menu.friend_context_menu, menu);
//        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
//        UserDao user = UserDao.get(this, info.id);
//        if (user.isNewFriend()) {
//            menu.removeItem(R.id.remove_from_friends);
//        } else {
//            menu.removeItem(R.id.add_to_friends);
//        }
//    }
//
//    public boolean onContextItemSelected(MenuItem item) {
//        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
//        long rowId = info.id;
//        switch (item.getItemId()) {
//            case R.id.view_profile:
//                UserHelper.viewProfile(this, rowId);
//                return true;
//            case R.id.remove_from_friends:
//                //todo!
//                Toast.makeText(this, "not implemented yet", Toast.LENGTH_LONG).show();
//                return true;
//            case R.id.add_to_friends:
//                //todo!
//                Toast.makeText(this, "not implemented yet", Toast.LENGTH_LONG).show();
//                return true;
//            case R.id.send_message:
//                UserHelper.sendMessage(this, rowId);
//                return true;
//            default:
//                return super.onContextItemSelected(item);
//        }
//    }

	@Override
	public View createTabContent(String tag) {
		//final TextView tv = new TextView(this);
		//tv.setText("Content for tab with tag " + tag);
		View tv= new View(this);
		
		if (tag.equals("tab1"))
		{
			//tv=getLayoutInflater().inflate(R.id.main_profile_view, null);
			
		}
		
		return tv;
	}
}