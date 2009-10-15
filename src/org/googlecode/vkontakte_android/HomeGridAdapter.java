package org.googlecode.vkontakte_android;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class HomeGridAdapter extends BaseAdapter {

	protected ArrayList<Integer> cell_titles ;
	protected ArrayList<Integer> cell_images ;
	private Context context;

	
	public HomeGridAdapter(Context context,ArrayList<Integer> _cell_titles, ArrayList<Integer> _cell_images ) {
		this.cell_titles=_cell_titles;
		this.cell_images=_cell_images;
	    this.context = context;
	}
	public HomeGridAdapter(Context context ) {
		
		cell_titles= new ArrayList<Integer>();
		cell_titles.add(R.string.my_profile);
		cell_titles.add(R.string.friends);
		cell_titles.add(R.string.messages);
		cell_titles.add(R.string.photos);
		cell_titles.add(R.string.updates);
		cell_titles.add(R.string.requests);
		cell_titles.add(R.string.search);
		cell_titles.add(R.string.settings);
		cell_titles.add(R.string.help);

		cell_images= new ArrayList<Integer>();
		cell_images.add(R.drawable.my_profile);
		cell_images.add(R.drawable.my_friends);
		cell_images.add(R.drawable.my_messages);
		cell_images.add(R.drawable.my_photos);
		cell_images.add(R.drawable.my_updates);
		cell_images.add(R.drawable.my_requests);
		cell_images.add(R.drawable.my_search);
		cell_images.add(R.drawable.my_settings);
		cell_images.add(R.drawable.my_help);		

		this.context = context;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return this.cell_titles.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		if (position<getCount())return context.getResources().getString(this.cell_titles.get(position));
		return "not cell";
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		LinearLayout ly= (LinearLayout) ((Activity)context).getLayoutInflater().inflate(R.layout.maingrid_cell, null);
		
		TextView tv = (TextView) ly.getChildAt(1);
		tv.setText((String) getItem(position));

		ImageView iv = (ImageView) ly.getChildAt(0);	
		iv.setImageResource(cell_images.get(position));
		Drawable dr =context.getResources().getDrawable(R.drawable.maingridcell_border);
		
		
		ly.setBackgroundDrawable(dr);
		ly.setTag(getItem(position));
		return ly;
	}

}
