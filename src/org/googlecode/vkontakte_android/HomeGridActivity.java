package org.googlecode.vkontakte_android;

import org.googlecode.vkontakte_android.service.CheckingService;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class HomeGridActivity extends Activity implements OnItemClickListener, ServiceConnection  {

    private final static String TAG = "org.googlecode.vkontakte_android.HomeGridActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.homegrid);
        bindService(new Intent(this,CheckingService.class), this, Context.BIND_AUTO_CREATE);


        GridView mHomeGrid = (GridView) findViewById(R.id.HomeGrid);
        mHomeGrid.setNumColumns(3);
        mHomeGrid.setAdapter(new HomeGridAdapter(this));
        mHomeGrid.setOnItemClickListener(this);
        this.setTitle(getResources().getString(R.string.app_name) + " > " + "Home");
        initStatus();
    }

    
    private void initStatus(){

        final EditText statusEdit =(EditText) findViewById(R.id.StatusEditText);
        statusEdit.setInputType(InputType.TYPE_NULL);        
        
        statusEdit.setOnTouchListener(new OnTouchListener(){
        	@Override
        	public boolean onTouch(View v, MotionEvent event) {
        		statusEdit.setInputType(InputType.TYPE_CLASS_TEXT);
        		statusEdit.onTouchEvent(event);
        		return true;
        		}
        	});
    	
    	  findViewById(R.id.StatusSubmitButton).setOnClickListener(new OnClickListener() {
  			
  			@Override
  			public void onClick(View v) {
  				 String statusText=((EditText) findViewById(R.id.StatusEditText)).getText().toString();
  				new AsyncTask<String, Object, Boolean>(){
  					
  					String m_status = "";
  					
  					@Override
  					protected void onPostExecute(Boolean result) {
  						EditText et=((EditText) findViewById(R.id.StatusEditText));
  						Toast.makeText(et.getContext(),"\""+et.getText().toString()+"\" Shared!", Toast.LENGTH_SHORT).show();
  						et.setText( result ? m_status : "");
  					}
  					
  					@Override
  					protected Boolean doInBackground(String... params) {
  						try {
  							m_status = params[0];
  							return ServiceHelper.getService().sendStatus(m_status);
  						} catch (RemoteException e) {
  							e.printStackTrace();
  							AppHelper.showFatalError(HomeGridActivity.this, "Error while launching the application");
  						}
  						return false;
  					}
  				}.execute(new String[]{statusText});
  			}
  		});    	
    	
    }
    
    private void backToHome() {
        this.setTitle(getResources().getString(R.string.app_name) + " > " + "Home");
        setProgressBarIndeterminateVisibility(false);
    }

    private void showRequests() {
        Intent i = new Intent(this, FriendListActivity.class);
//        i.putExtra(FriendsListTabActivity.SHOW_ONLY_NEW, true);
        startActivity(i);
    }

    /*
    private void showFriends() {
        Intent i = new Intent(this, FriendsListTabActivity.class);
//        i.putExtra(FriendsListTabActivity.SHOW_ONLY_NEW, false);
        startActivity(i);
    }
    */

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        setProgressBarIndeterminateVisibility(true);
        this.setTitle(getResources().getString(R.string.app_name) + " > " + arg1.getTag());

        if (arg1.getTag().equals("Settings")) {
            startActivity(new Intent(this, CSettings.class));
        } else if (arg1.getTag().equals("Requests")) {
            showRequests();
        } else if (arg1.getTag().equals("Help")) {
            AboutDialog.makeDialog(this).show();
            backToHome();
        }
        // Not implemented
        else if (arg1.getTag().equals("Help")
                || arg1.getTag().equals("Search")
                || arg1.getTag().equals("Photos")) {
            Toast.makeText(this, "Not implemented", Toast.LENGTH_SHORT).show();
            backToHome();
        } else {
            Intent i = new Intent(this, CGuiTest.class);
            i.putExtra("tabToShow", (String) arg1.getTag());
            startActivity(i);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "Activity Resumed");
        backToHome();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "Activity Stopped");
        //try {m_vkService.stop();} catch (RemoteException e) {e.printStackTrace();}
    }

    @Override
    public void onDestroy(){
    	super.onDestroy();
        Log.d(TAG, "Activity Destroyed");
    	unbindService(this);
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
			try {
				// todo async				
				ServiceHelper.getService().logout();
				login();
				return true;
			} catch (RemoteException e) {
				e.printStackTrace();
				AppHelper.showFatalError(this, "Error while logging out");
			}
		case R.id.AboutMenuItem:
			AboutDialog.makeDialog(this).show();
			return true;
		case R.id.ExitMenuItem:
			try {
				ServiceHelper.getService().stop();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			finish();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

    private void login() throws RemoteException {
        if (ServiceHelper.getService().loginAuth()) {
            Log.d(TAG, "Already authorized");
            return;
        }

        final LoginDialog ld = new LoginDialog(this);
        ld.setTitle(R.string.please_login);
        ld.setCancelable(false);
        ld.show();
   }

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
        ServiceHelper.connect(service);
		try {
			login();
		} catch (RemoteException e) {
			e.printStackTrace();
			AppHelper.showFatalError(this, "Error while launching the application");
		}
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		ServiceHelper.disconnect();
	}
}
