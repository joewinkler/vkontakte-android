package org.googlecode.vkontakte_android;

import android.app.ListActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.ListAdapter;

/**
 * ListActivity that can load more records when user scrolls down the main ListView. 
 * Setup it with setupLoader() function.
 * 
 * @author bea
 *
 */
public class AutoLoadActivity extends ListActivity  {

	private static String TAG = "AutoLoadActivity";
	protected ListAdapter m_adapter;
	private Loader m_loader;

	boolean m_doLoad = true;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
	}
	
	/**
	 * Call it only after child performs setContentView() 
	 * @param l - callback to be performed when needed.
	 * @param ad - adapter to load more data from it. 
	 */
	public void setupLoader(Loader l, ListAdapter ad) {
		m_loader = l;
		m_adapter = ad;
		setListAdapter(m_adapter);
		getListView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && event.getAction() == KeyEvent.ACTION_DOWN
                        && getListView().getSelectedItemPosition() == m_adapter.getCount() - 1) {
                	//to prevent multiple loading
					if (m_doLoad) {
                	    loadMore();
					}
                }
                return false;
            }
        });
	}
	
	public void onScrollStateChanged(AbsListView v, int state) {
        if (state == AbsListView.OnScrollListener.SCROLL_STATE_IDLE
                && getListView().getLastVisiblePosition() == m_adapter.getCount() - 1) {
        	//to prevent multiple loading
			if (m_doLoad) {
        	    loadMore();
			}
        }
    }
	
    private void loadMore() {
    	if (m_loader == null) {
    		Log.e(TAG, "Callback undefined. Use setupLoader() at first");
    		return;
    	}
    	new AsyncTask<Object, Object, Boolean>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                setProgressBarIndeterminateVisibility(true);
                m_doLoad = false;
            } 
    		
			@Override
			protected void onPostExecute(Boolean result) {
				setProgressBarIndeterminateVisibility(false);
				m_doLoad = true;
			}

			@Override
			protected Boolean doInBackground(Object... params) {
				Log.d(TAG, "loading more info...");
				return m_loader.load();			
			}
    	}.execute();
}
	
    //TODO make template
	public abstract interface Loader {
		Boolean load(Long ...longs);
	}
}
