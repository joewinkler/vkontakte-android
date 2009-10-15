package org.googlecode.vkontakte_android;

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

	private GridView homeGrid = null;
	private static String TAG = "VK:HomeGrid";
	
    public IVkontakteService m_vkService;
    private VkontakteServiceConnection m_vkServiceConnection = new VkontakteServiceConnection();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		setContentView(R.layout.maingrid);
        
		

		homeGrid = (GridView) findViewById(R.id.MainGrid);
		homeGrid.setNumColumns(3);
		homeGrid.setAdapter(new HomeGridAdapter(this));
		homeGrid.setOnItemClickListener(this);
		this.setTitle(getResources().getString(R.string.app_name) + " > " + "Home");
		
		// Binding service
		bindService();
        
	}

	private void backToHome(){
		this.setTitle(getResources().getString(R.string.app_name) + " > "+ "Home");
		setProgressBarIndeterminateVisibility(false);
	}
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		setProgressBarIndeterminateVisibility(true);
		this.setTitle(getResources().getString(R.string.app_name) + " > " + (String)arg1.getTag());
		
		if (arg1.getTag().equals("Settings") ) {
			startActivity(new Intent(this, CSettings.class));
		}
		// Not implemented
		else if(  arg1.getTag().equals("Help")
				||arg1.getTag().equals("Search") 
				||arg1.getTag().equals("Requests")
				||arg1.getTag().equals("Photos") ){
			Toast.makeText(this, "Not implemented", Toast.LENGTH_SHORT).show();
		    backToHome();
			return;
		}
		else{
			Intent i = new Intent(this, CGuiTest.class);
			i.putExtra("tabToShow", (String)arg1.getTag());
			startActivity(i);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "Activity Resumed");
		backToHome();
		bindService();
		
	}
	
	@Override
	public void onStop(){
		super.onStop();
		Log.d(TAG, "Activity Stopped");
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
