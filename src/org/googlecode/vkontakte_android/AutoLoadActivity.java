package org.googlecode.vkontakte_android;

import android.app.ListActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.ListAdapter;

public class AutoLoadActivity extends ListActivity  {

	protected ListAdapter m_adapter;
	private Loader m_loader;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        
	}
	
	public void setupLoader(Loader l, ListAdapter ad) {
		m_loader = l;
		m_adapter = ad;
		setListAdapter(m_adapter);
		getListView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && event.getAction() == KeyEvent.ACTION_DOWN
                        && getListView().getSelectedItemPosition() == m_adapter.getCount() - 1) {
                    loadMore();
                }
                return false;
            }
        });
	}
	
	public void onScrollStateChanged(AbsListView v, int state) {
        if (state == AbsListView.OnScrollListener.SCROLL_STATE_IDLE
                && getListView().getLastVisiblePosition() == m_adapter.getCount() - 1) {
            loadMore();
        }
    }
	
    private void loadMore() {
		setProgressBarIndeterminateVisibility(true);
    	new AsyncTask<Object, Object, Boolean>() {

			@Override
			protected void onPostExecute(Boolean result) {
				setProgressBarIndeterminateVisibility(false);
			}

			@Override
			protected Boolean doInBackground(Object... params) {
					return m_loader.load();
			}
    	}.execute();
}
	
	public abstract interface Loader {
		Boolean load();
	}
}
