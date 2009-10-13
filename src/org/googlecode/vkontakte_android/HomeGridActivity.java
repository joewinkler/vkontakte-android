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
	
	private ArrayList<String> cells= new ArrayList<String>() ;

    protected GridView mainGrid = null; 
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maingrid);
        
        cells.add("My Profile");
        cells.add("Friends");
        cells.add("Messages");
        cells.add("Photos");
        cells.add("Updates");
        cells.add("Settings");
        
        mainGrid = (GridView) findViewById(R.id.MainGrid);
        mainGrid.setNumColumns(3);

        MainGridAdapter mga= new MainGridAdapter(this, cells);
        mainGrid.setAdapter(mga);
        mainGrid.setOnItemClickListener(this);
        
        
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
         
		Toast.makeText(this, String.valueOf(arg2), Toast.LENGTH_SHORT).show();
		startActivity(new Intent(this,CGuiTest.class));
	}

	
}
