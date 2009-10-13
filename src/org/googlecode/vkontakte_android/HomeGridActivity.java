package org.googlecode.vkontakte_android;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.AdapterView.OnItemClickListener;

public class HomeGridActivity extends Activity implements OnItemClickListener {

	private ArrayList<String> cell_titles = new ArrayList<String>();
	private ArrayList<Integer> cell_images = new ArrayList<Integer>();

	protected GridView mainGrid = null;

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

		mainGrid = (GridView) findViewById(R.id.MainGrid);
		mainGrid.setNumColumns(3);

		HomeGridAdapter mga = new HomeGridAdapter(this, cell_titles,
				cell_images);
		mainGrid.setAdapter(mga);
		mainGrid.setOnItemClickListener(this);
		this.setTitle(getResources().getString(R.string.app_name) + " > "
				+ "Home");

	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

		// Toast.makeText(this, String.valueOf(arg2),
		// Toast.LENGTH_SHORT).show();
		setProgressBarIndeterminateVisibility(true);
		this.setTitle(getResources().getString(R.string.app_name) + " > "
				+ cell_titles.get(arg2));
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
		this.setTitle(getResources().getString(R.string.app_name) + " > "
				+ "Home");
		setProgressBarIndeterminateVisibility(false);
	}
}
