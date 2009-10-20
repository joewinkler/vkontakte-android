package org.googlecode.vkontakte_android;


import android.app.TabActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import org.googlecode.vkontakte_android.provider.UserapiProvider;
import org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper;
import org.googlecode.vkontakte_android.service.CheckingService;
import org.googlecode.vkontakte_android.service.IVkontakteService;
import org.googlecode.vkontakte_android.service.CheckingService.contentToUpdate;

public class CGuiTest extends TabActivity {

    public static CGuiTest s_instance; //TODO refactor

    private static String TAG = "VK-Gui ";
    //public static VkontakteAPI api;
    public IVkontakteService m_vkService;

    private VkontakteServiceConnection m_connection = new VkontakteServiceConnection();

    //todo: use map(?)
    public static final int MY_PAGE = 0;
    public static final int MY_FRIENDS = 1;
    public static final int MY_MESSAGES = 2;
    public static final int MY_UPDATES = 3;


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        s_instance = this;
        initializeActivity();
        bindService();
        // For calls from HomeGrid
        if (getIntent().hasExtra("tabToShow")) {
            getTabHost().setCurrentTabByTag(getIntent().getStringExtra("tabToShow"));
        }

    }


    private void login() throws RemoteException {
        // TODO handle JSONException in api methods

        if (m_vkService.loginAuth()) {
            Log.d(TAG, "Already authorized");
            initializeUserStuff();
            return;
        }

        final LoginDialog ld = new LoginDialog(this);
        ld.setTitle(R.string.please_login);
//        ((EditText) ld.findViewById(R.id.login)).setText("fake4test@gmail.com");
//        ((EditText) ld.findViewById(R.id.pass)).setText("qwerty");
        ld.show();
        ld.setOnLoginClick(new View.OnClickListener() {


            public void onClick(View view) {
                ld.showProgress();
                String login = ld.getLogin();
                String pass = ld.getPass();
                Log.i(TAG, login + ":" + pass);

                new AsyncTask<String, Void, Boolean>() {
                    @Override
                    protected void onPostExecute(Boolean result) {
                        ld.stopProgress();
                        if (result) {
                            ld.dismiss();
                            initializeUserStuff();
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    R.string.login_err, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    protected Boolean doInBackground(String... params) {
                        try {
                            return m_vkService.login(params[0], params[1]);
                        } catch (RemoteException e) {
                            CGuiTest.fatalError("RemoteException");
                            ld.stopProgress();
                            e.printStackTrace();
                            return false;
                        }
                    }

                }.execute(login, pass);
            }
        });

        ld.setOnCancelClick(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    m_vkService.stop();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                unbindService(m_connection);
                ld.dismiss();
                finish();
            }
        });
    }


    private void initializeActivity() {
        //refresh(contentToUpdate.ALL);
        // load icons from the files
        CImagesManager.loadImages(getApplicationContext());

        final TabHost tabHost = getTabHost();
        tabHost.addTab(tabHost.newTabSpec("My Profile").setIndicator(
                getResources().getString(R.string.i)).setContent(
                new Intent(CGuiTest.this, CMeTab.class)));

        tabHost.addTab(tabHost.newTabSpec("Friends").setIndicator(
                getResources().getString(R.string.friends),
                getResources().getDrawable(R.drawable.ic_menu_friendslist))
                .setContent(new Intent(CGuiTest.this, FriendsListTabActivity.class)));

        tabHost.addTab(tabHost.newTabSpec("Messages").setIndicator(
                getResources().getString(R.string.messages)).setContent(
                new Intent(CGuiTest.this, MessagesListTabActivity.class)));

        tabHost.addTab(tabHost.newTabSpec("Updates").setIndicator(
                getResources().getString(R.string.updates)).setContent(
                new Intent(CGuiTest.this, UpdatesListTabActivity.class)));
    }


    private void initializeUserStuff() {
        // todo: possibly move to tabs activities itself
        final TextView friendsCounter = TabHelper.injectTabCounter(getTabWidget(), 1, getApplicationContext());
        final TextView messagesCounter = TabHelper.injectTabCounter(getTabWidget(), 0, getApplicationContext());

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


        //statuses content
        getContentResolver().registerContentObserver(UserapiProvider.STATUSES_URI, false, new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean b) {
                setProgressBarIndeterminateVisibility(false);
            }
        });
        getContentResolver().notifyChange(UserapiProvider.STATUSES_URI, null);

        try {
            CMeTab.s_instance.loadProfile();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
//    	if (intent.hasExtra("error")) {
//    		GuiUtils.error(intent);
//    	} else if (intent.hasExtra("message")) {
//    		GuiUtils.message(intent);
//    	} else if (intent.hasExtra("tabToShow")) {
//    		String tag = intent.getStringExtra("tabToShow");
//    		Log.d(TAG, "onNewIntent:: " + tag);
//    		getTabHost().setCurrentTabByTag(tag);
//    	}

        super.onNewIntent(intent);
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
                    case MY_MESSAGES:
                        refresh(contentToUpdate.MESSAGES_ALL);
                        break;
                    case MY_UPDATES:
                        refresh(contentToUpdate.STATUSES);
                        break;
                    default:
                        refresh(contentToUpdate.ALL);
                }
                return true;
            case R.id.settings:
                startActivity(new Intent(this, CSettings.class));
                return true;
            case R.id.about:
                AboutDialog.makeDialog(this).show();

                return true;
            case R.id.logout:
                try {
                    m_vkService.logout();
                    m_vkService.stop();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                unbindService(m_connection);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Makes Service to refresh given content
     *
     * @throws RemoteException
     */
    private void refresh(CheckingService.contentToUpdate what) {
        Log.d(TAG, "request to refresh");
        //Toast.makeText(this, "Update started", Toast.LENGTH_SHORT).show();
        setProgressBarIndeterminateVisibility(true);

        try {
            m_vkService.update(what.ordinal());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        //startService(new Intent(this, CheckingService.class).putExtra("action", what.ordinal()));

    }

    public static void fatalError(String text) {
        Toast.makeText(CGuiTest.s_instance, text, Toast.LENGTH_SHORT).show();
    }

    

    @Override
    public void onStop(){
    	super.onStop();
        unbindService(m_connection);
    }
    
    @Override
    public void onResume(){
    	super.onResume();
    	bindService();
    }
    
    // =========  RPC stuff ====================

    /**
     * Binds the service
     */
    private void bindService() {
        Intent i = new Intent();
        i.setClassName(this, CheckingService.class.getName());
        bindService(i, m_connection, Context.BIND_AUTO_CREATE);
        Log.d(TAG, "Binding the service");
    }

    class VkontakteServiceConnection implements ServiceConnection {
        public void onServiceConnected(ComponentName className,
                                       IBinder boundService) {
            m_vkService = IVkontakteService.Stub.asInterface((IBinder) boundService);
            Log.d(TAG, "Service has been connected");
            try {
                login();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            m_vkService = null;
            Log.d(TAG, "Service has been disconnected");
        }
    }
}