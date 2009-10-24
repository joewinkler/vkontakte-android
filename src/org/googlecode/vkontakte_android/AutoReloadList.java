package org.googlecode.vkontakte_android;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

public class AutoReloadList extends ListView {

	private Loader mLoader;

	public AutoReloadList(Context context) {
		super(context);
	}
	
	public void setLoader(Loader loader){
		mLoader=loader;
	}

	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN
				&& event.getAction() == KeyEvent.ACTION_DOWN
				&& getSelectedItemPosition() == getAdapter().getCount() - 1) {
			loadMore();
		}
		return false;
	}

	public void onScrollStateChanged(AbsListView v, int state) {
		if (state == AbsListView.OnScrollListener.SCROLL_STATE_IDLE
				&& getLastVisiblePosition() == getAdapter().getCount() - 1) {
			loadMore();
		}
	}

	private void loadMore() {
		if (!mLoader.equals(null)){
		new AsyncTask<Object, Object, Boolean>() {

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				((Activity) getContext()).setProgressBarIndeterminateVisibility(true);
			}

			@Override
			protected void onPostExecute(Boolean result) {
				((Activity) getContext()).setProgressBarIndeterminateVisibility(false);
			}

			@Override
			protected Boolean doInBackground(Object... params) {
				return mLoader.load();
			}
		}.execute();
		}
	}

	public abstract interface Loader {
		Boolean load();
	}

}
