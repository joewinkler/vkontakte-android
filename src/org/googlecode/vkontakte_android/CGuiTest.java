package org.googlecode.vkontakte_android;


import android.app.TabActivity;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.widget.*;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.MenuInflater;
import android.util.Log;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.RectF;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.shapes.RoundRectShape;
import org.googlecode.userapi.VkontakteAPI;
import org.googlecode.vkontakte_android.service.CheckingService;
import org.googlecode.vkontakte_android.service.CheckingService.contentToUpdate;

import org.googlecode.vkontakte_android.provider.UserapiProvider;

import java.io.IOException;

public class CGuiTest extends TabActivity { 
	private static String TAG = "VK-Gui";
    public static VkontakteAPI api;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        
        //TODO handle JSONException in api methods
        
        api = new VkontakteAPI();
        if (CSettings.isLogged(this))
        {
        	try{
        	Log.d(TAG,"already logged. using existing log/pass");
        	if (api.login(CSettings.getLogin(this), CSettings.getPass(this)))
        	{
        	  initializeActivity(this);
        	  return;
        	}
        	else 
             Toast.makeText(getApplicationContext(), "Either login or password is incorrect", Toast.LENGTH_SHORT).show();	
        	}
        	catch (IOException ex)
        	{
        		//show toast and then login dialog
        		Toast.makeText(getApplicationContext(), "Connection problems", Toast.LENGTH_SHORT).show();
        	}
        }
        
        
        final LoginDialog ld = new LoginDialog(this);
        ((EditText) ld.findViewById(R.id.login)).setText("fake4test@gmail.com");
        ((EditText) ld.findViewById(R.id.pass)).setText("qwerty");
        ld.show();
        ld.setOnClick(new View.OnClickListener() {
            public void onClick(View view) {
                try {
                    String login = ld.getLogin();
                    String pass = ld.getPass();
                    Log.i(TAG, login+":"+pass);
                    if (api.login(login, pass)) {
                        ld.dismiss();
                        CSettings.saveLogin(CGuiTest.this, login, pass);
                        initializeActivity(CGuiTest.this);
                    } else {
                        Toast.makeText(getApplicationContext(), "login/pass incorrect", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });


    }
    
    
    private void initializeActivity(Context ctx)
    {
    	refresh(contentToUpdate.ALL);
        
        // load icons from the files
		CImagesManager.loadImages(ctx);

        final TabHost tabHost = getTabHost();

		tabHost.addTab(tabHost.newTabSpec("I").setIndicator(
				getResources().getString(R.string.i)).setContent(
				new Intent(CGuiTest.this, CMeTab.class)));

		tabHost.addTab(tabHost.newTabSpec("Friends").setIndicator(
				getResources().getString(R.string.friends),
				getResources().getDrawable(R.drawable.ic_menu_friendslist))
				.setContent(new Intent(CGuiTest.this, CFriendsTab.class)));

		tabHost.addTab(tabHost.newTabSpec("Messages").setIndicator(
				getResources().getString(R.string.messages)).setContent(
				new Intent(CGuiTest.this, CMessagesTab.class)));

		// todo: remove - just P-o-C here
		final TextView tv = TabHelper.injectTabCounter(getTabWidget(), 1,
				getApplicationContext());

		// todo: register/unregister onResume/onPause
		getContentResolver().registerContentObserver(UserapiProvider.USERS_URI,
				false, new ContentObserver(new Handler()) {
					@Override
					public void onChange(boolean b) {
						Cursor cursor = managedQuery(UserapiProvider.USERS_URI,
								null, null, null, null);// todo: change cursor
														// to new only
						if (cursor.getCount() == 0)
							tv.setVisibility(View.INVISIBLE);
						else {
							tv.setText(String.valueOf(cursor.getCount()));
							tv.setVisibility(View.VISIBLE);
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
                refresh(contentToUpdate.ALL);
                return true;
            case R.id.settings:
            	startActivity(new Intent(this, CSettings.class));
            	return true;
            case R.id.logout:
                try {
					api.logout();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				stopService(new Intent(this, CheckingService.class));
				CSettings.clearPrivateInfo(this);
            	finish();
                
            	
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /*
     *  makes Service to refresh given content
     */
    private void refresh(CheckingService.contentToUpdate what) {
		Log.d(TAG, "request to refresh");
		Toast.makeText(this, "Update started", Toast.LENGTH_SHORT).show();
		startService(new Intent(this, CheckingService.class).putExtra("action",what.ordinal()));
				//CheckingService.contentToUpdate.FRIENDS));
	}
}