package org.googlecode.vkontakte_android;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainGridAdapter extends BaseAdapter {

	protected ArrayList<String> cells ;
	private Context context;

	
	public MainGridAdapter(Context context,ArrayList<String> _cells ) {
		this.cells=_cells;
	    this.context = context;
	}
	
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return this.cells.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		if (position<getCount())return this.cells.get(position);
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
		
		Drawable dr =context.getResources().getDrawable(R.drawable.maingridcell_border);
		//dr.setAlpha(0);
		
		ly.setBackgroundDrawable(dr);
		
		return ly;
	}

}
