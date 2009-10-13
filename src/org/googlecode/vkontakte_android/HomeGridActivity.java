package org.googlecode.vkontakte_android;


import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;


public class HomeGridActivity  extends Activity implements OnItemClickListener{
	
	private ArrayList<String> cell_titles= new ArrayList<String>() ;
	private ArrayList<Integer> cell_images= new ArrayList<Integer>() ;

    protected GridView mainGrid = null; 
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maingrid);
        
        cell_titles.add("My Profile");
        cell_titles.add("Friends");
        cell_titles.add("Messages");
        cell_titles.add("Photos");
        cell_titles.add("Updates");
        cell_titles.add("Settings");
        
        cell_images.add(R.drawable.my_profile);
        cell_images.add(R.drawable.my_friends);
        cell_images.add(R.drawable.my_messages);
        cell_images.add(R.drawable.my_photos);
        cell_images.add(R.drawable.my_updates);
        cell_images.add(R.drawable.my_settings);
        
        
        mainGrid = (GridView) findViewById(R.id.MainGrid);
        mainGrid.setNumColumns(3);

        HomeGridAdapter mga= new HomeGridAdapter(this, cell_titles,cell_images);
        mainGrid.setAdapter(mga);
        mainGrid.setOnItemClickListener(this);
        
        
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
         
		Toast.makeText(this, String.valueOf(arg2), Toast.LENGTH_SHORT).show();
		startActivity(new Intent(this,CGuiTest.class));
	}

	
}
