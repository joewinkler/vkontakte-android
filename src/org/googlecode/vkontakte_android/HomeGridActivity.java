package org.googlecode.vkontakte_android;

import java.util.ArrayList;

import org.googlecode.vkontakte_android.service.CheckingService;
import org.googlecode.vkontakte_android.service.IVkontakteService;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class HomeGridActivity extends Activity implements OnItemClickListener {

	private ArrayList<String> cell_titles = new ArrayList<String>();
	private ArrayList<Integer> cell_images = new ArrayList<Integer>();

	private GridView homeGrid = null;
	private static String TAG = "VK:HomeGrid";
	
    public IVkontakteService m_vkService;
    private VkontakteServiceConnection m_vkServiceConnection = new VkontakteServiceConnection();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		setContentView(R.layout.maingrid);
        
		cell_titles.add("My Profile");
		cell_titles.add("Friends");
		cell_titles.add("Messages");
		cell_titles.add("Photos");
		cell_titles.add("Updates");
		cell_titles.add("Requests");
		cell_titles.add("Search");
		cell_titles.add("Settings");
		cell_titles.add("Help");

		cell_images.add(R.drawable.my_profile);
		cell_images.add(R.drawable.my_friends);
		cell_images.add(R.drawable.my_messages);
		cell_images.add(R.drawable.my_photos);
		cell_images.add(R.drawable.my_updates);
		cell_images.add(R.drawable.my_requests);
		cell_images.add(R.drawable.my_search);
		cell_images.add(R.drawable.my_settings);
		cell_images.add(R.drawable.my_help);

		homeGrid = (GridView) findViewById(R.id.MainGrid);
		homeGrid.setNumColumns(3);
		homeGrid.setAdapter(new HomeGridAdapter(this, cell_titles, cell_images));
		homeGrid.setOnItemClickListener(this);
		this.setTitle(getResources().getString(R.string.app_name) + " > " + "Home");
		
		// Binding service
		bindService();
        
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		setProgressBarIndeterminateVisibility(true);
		this.setTitle(getResources().getString(R.string.app_name) + " > " + cell_titles.get(arg2));
		if (cell_titles.get(arg2) == "Settings") {
			startActivity(new Intent(this, CSettings.class));
		} else {
			Intent i = new Intent(this, CGuiTest.class);
			i.putExtra("tabToShow", cell_titles.get(arg2));
			startActivity(i);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "Resumed");
		this.setTitle(getResources().getString(R.string.app_name) + " > "+ "Home");
		setProgressBarIndeterminateVisibility(false);
		bindService();
	}
	
	@Override
	public void onStop(){
		super.onStop();
		Log.d(TAG, "Stopped");
    	//try {m_vkService.stop();} catch (RemoteException e) {e.printStackTrace();}
		Log.d(TAG, "Unbinding the service");
    	unbindService(m_vkServiceConnection);
	}

	  @Override
	    public boolean onCreateOptionsMenu(Menu menu) {
	        MenuInflater inflater = getMenuInflater();
	        inflater.inflate(R.menu.home_menu, menu);
	        return super.onCreateOptionsMenu(menu);
	    }
	
	
	
	  public boolean onOptionsItemSelected(MenuItem item) {
	        switch (item.getItemId()) {
	            case R.id.LogoutMenuItem:
	            	try {m_vkService.logout();} catch (RemoteException e) {e.printStackTrace();}
	        		try {login();} catch (RemoteException e) {e.printStackTrace();}
	            	return true;
	            case R.id.AboutMenuItem:
	            	AboutDialog.makeDialog(this).show();
	            return true;
	            case R.id.ExitMenuItem:
	            	try {m_vkService.stop();} catch (RemoteException e) {e.printStackTrace();}
	            	unbindService(m_vkServiceConnection);
	            	finish();
	            	return true;
	        }
			return super.onOptionsItemSelected(item);
	  }
	  
    private void login() throws RemoteException {
        // TODO handle JSONException in api methods

        if (m_vkService.loginAuth()) {
            Log.d(TAG, "Already authorized");
            //initializeUserStuff();
            return;
        }

        final LoginDialog ld = new LoginDialog(this);
        ld.show();
        ld.setCancelable(false);
        ld.setOnLoginClick(new View.OnClickListener() {
            public void onClick(View view) {

                String login = ld.getLogin();
                String pass = ld.getPass();
                Log.i(TAG, login + ":" + pass);
                try {
                	Log.d(TAG, "Service logging in");
                    if (m_vkService.login(login, pass)) {
                        ld.dismiss();
                        //initializeUserStuff();
                    } else {
                        Toast.makeText(getApplicationContext(),
                                R.string.login_err, Toast.LENGTH_SHORT).show();
                    }
                } catch (RemoteException e) {
                    CGuiTest.fatalError("RemoteException");
                    e.printStackTrace();
                }
            }
        });

        ld.setOnCancelClick(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ld.dismiss();
                finish();
            }
        });
    }
	
	
	
	// =========  RPC stuff ====================
    /**
     * Binds the service
     */
    private void bindService() {
        Intent i = new Intent(this,CheckingService.class);
        bindService(i, m_vkServiceConnection, Context.BIND_AUTO_CREATE);
        Log.d(TAG, "Binding the service");
    }
    class VkontakteServiceConnection implements ServiceConnection {
        public void onServiceConnected(ComponentName className,IBinder boundService) {
            m_vkService = IVkontakteService.Stub.asInterface((IBinder) boundService);
            Log.d(TAG, "Service has been connected");
    		// Try to login by saved prefs or show Login Dialog
    		try {login();} catch (RemoteException e) {e.printStackTrace();}
        }
        public void onServiceDisconnected(ComponentName className) {
            Log.d(TAG, "Service has been disconnected");
        }
    }
}
