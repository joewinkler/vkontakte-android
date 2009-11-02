package org.googlecode.vkontakte_android;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.Contacts;
import android.util.Log;
import android.view.*;
import android.widget.*;

import org.googlecode.vkontakte_android.AutoLoadList.Loader;
import org.googlecode.vkontakte_android.database.ProfileDao;
import org.googlecode.vkontakte_android.database.UserDao;
import org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper;
import org.googlecode.vkontakte_android.service.CheckingService;
import org.googlecode.vkontakte_android.utils.Phone;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper.KEY_STATUS_DATE;
import static org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper.KEY_STATUS_USERID;
import static org.googlecode.vkontakte_android.provider.UserapiProvider.PROFILES_URI;
import static org.googlecode.vkontakte_android.provider.UserapiProvider.STATUSES_URI;

public class ProfileViewActivity extends Activity implements TabHost.TabContentFactory {

    private static final String TAG = "org.googlecode.vkontakte_android.ProfileViewActivity";
    private long profileId;
    private ProfileDao friendProfile;
    private Menu menuToRefresh; //menu is disabled until we haven't friend data

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.profile_view);

        initTabHost();
        initInfoTab();
        initWallTab();
        initUpdatesTab();
    }

    private void initTabHost() {
        final TabHost tabHost = (TabHost) findViewById(R.id.ProfileTabHost);
        tabHost.setup();
        tabHost.addTab(tabHost.newTabSpec("info_tab")
                .setIndicator("Info")
                .setContent(this));
        tabHost.addTab(tabHost.newTabSpec("wall_tab")
                .setIndicator("Wall")
                .setContent(this));
        tabHost.addTab(tabHost.newTabSpec("updates_tab")
                .setIndicator("Updates")
                .setContent(this));

    }


    private void initUpdatesTab() {
        Cursor statusesCursor = managedQuery(STATUSES_URI, null, KEY_STATUS_USERID + "=" + profileId, null, KEY_STATUS_DATE + " DESC ");
        if (statusesCursor.getCount() < 2) {
            new AsyncTask<Long, Object, Boolean>() {

                @Override
                protected Boolean doInBackground(Long... params) {
                    try {
                        return ServiceHelper.mVKService.loadStatusesByUser(0, CheckingService.STATUS_NUM_LOAD, profileId);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    return false;
                }

            }.execute(new Long[]{profileId});
        }


    }

    private void initWallTab() {
    }

    private void initInfoTab() {
        profileId = CSettings.myId;
        if (getIntent().getExtras() != null)
            profileId = getIntent().getExtras().getLong(UserapiDatabaseHelper.KEY_PROFILE_USERID, profileId);

        new AsyncTask<Long, Object, ProfileDao>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                setProgressBarIndeterminateVisibility(true);
            }

            @Override
            protected void onPostExecute(ProfileDao result) {
                setProgressBarIndeterminateVisibility(false);
                if (!result.equals(null)) showProfileInfo(result);
            }

            @Override
            protected ProfileDao doInBackground(Long... id) {

                try {
                    if (!ServiceHelper.mVKService.loadProfile(id[0], false)) {
                        Log.e(TAG, "Cannot load profile");
                        return null;
                    } else {
                        Cursor cursor = managedQuery(PROFILES_URI, null, UserapiDatabaseHelper.KEY_PROFILE_USERID + "=?", new String[]{String.valueOf(id[0])}, null);
                        cursor.moveToFirst();
                        return new ProfileDao(cursor);
                    }

                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                return null;
            }
        }.execute(profileId);

    }

    private void showProfileInfo(ProfileDao profile) {
        friendProfile = profile;
        ((TextView) findViewById(R.id.InfoFirstName)).setText(friendProfile.firstname);
        ((TextView) findViewById(R.id.InfoLastName)).setText(friendProfile.surname);
        setTitle(getTitle()+ ": "+friendProfile.firstname + " " + friendProfile.surname);
        ((ImageButton) findViewById(R.id.InfoPhoto)).setImageBitmap(UserHelper.getPhotoByUserId(this, friendProfile.id));
        ((TextView) findViewById(R.id.InfoStatusText)).setText(friendProfile.status);

        if (friendProfile.birthday != null && friendProfile.birthday != 0) {
            findViewById(R.id.birthday_row).setVisibility(View.VISIBLE);
            SimpleDateFormat format = new SimpleDateFormat("d MMM yyyy");
            ((TextView) findViewById(R.id.birthday)).setText(format.format(new Date(friendProfile.birthday)));
        }
//        if (friendProfile.sex != 0) {
//            findViewById(R.id.sex_row).setVisibility(View.VISIBLE);
//            ((TextView) findViewById(R.id.user_sex)).setText(friendProfile.sex);
//        }
        if (friendProfile.phone != null) {
            findViewById(R.id.phone_row).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.phone)).setText(friendProfile.phone);
        }
        refreshMenu();  //TODO: avoid unnecessary calls
    }

    private void refreshMenu() {
        if (menuToRefresh != null) {
            onPrepareOptionsMenu(menuToRefresh);
        }
    }


    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.friend_context_menu, menu);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        UserDao user = UserDao.get(this, info.id);
        if (user.isNewFriend()) {
            menu.removeItem(R.id.remove_from_friends);
        } else {
            menu.removeItem(R.id.add_to_friends);
        }
    }

    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        long rowId = info.id;
        switch (item.getItemId()) {
            case R.id.view_profile:
                UserHelper.viewProfile(this, rowId);
                return true;
            case R.id.remove_from_friends:
                //todo!
                Toast.makeText(this, "not implemented yet", Toast.LENGTH_LONG).show();
                return true;
            case R.id.add_to_friends:
                //todo!
                Toast.makeText(this, "not implemented yet", Toast.LENGTH_LONG).show();
                return true;
            case R.id.send_message:
                UserHelper.sendMessage(this, rowId);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public View createTabContent(String tag) {
        View tv = new View(this);

        if (tag.equals("info_tab")) {
            tv = getLayoutInflater().inflate(R.layout.profile_view_info, null);
        } else if (tag.equals("updates_tab")) {

            final AutoLoadList arl = new AutoLoadList(this);
            Cursor statusesCursor = managedQuery(STATUSES_URI, null, KEY_STATUS_USERID + "=" + profileId, null, KEY_STATUS_DATE + " DESC ");
            arl.setAdapter(new UpdatesListAdapter(this, R.layout.status_row_profile, statusesCursor));
            arl.setLoader(new Loader() {
                @Override
                public Boolean load() {
                    try {
                        return ServiceHelper.mVKService.loadStatusesByUser(arl.getAdapter().getCount(),
                                arl.getAdapter().getCount() + CheckingService.STATUS_NUM_LOAD, profileId);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    return false;
                }
            });
            return arl;
        }
        return tv;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        menuToRefresh = menu;
        inflater.inflate(R.menu.friend_profile_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (friendProfile == null) {
            menu.setGroupEnabled(0, false); // can't add friend as contact without data
        } else {
            menu.setGroupEnabled(0, true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_to_contacts:
                addOrEditContact();
                return true;
            default:
                return true;
        }
    }

    private void addOrEditContact() {
        //TODO add mail and other
        Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
        intent.setType("vnd.android.cursor.item/person");
        intent.putExtra(Contacts.Intents.Insert.PHONE, Phone.formatPhoneNumber(friendProfile.phone));
        intent.putExtra(Contacts.Intents.Insert.NAME, friendProfile.firstname + " " + friendProfile.surname);
        startActivity(intent);
    }

}