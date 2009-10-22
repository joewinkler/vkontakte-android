package org.googlecode.vkontakte_android;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import org.googlecode.vkontakte_android.database.StatusDao;
import org.googlecode.vkontakte_android.database.UserDao;
import org.googlecode.vkontakte_android.service.CheckingService;

import static org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper.*;
import static org.googlecode.vkontakte_android.provider.UserapiProvider.STATUSES_URI;

public class UpdatesListTabActivity extends ListActivity implements AdapterView.OnItemClickListener {
	private static final String TAG = "UpdatesListTabActivity";
	private UpdatesListAdapter m_adapter;
	 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.status_list);
        final Cursor statusesCursor = managedQuery(STATUSES_URI, null, null, null, KEY_STATUS_DATE + " DESC ");
        m_adapter = new UpdatesListAdapter(this, R.layout.status_row, statusesCursor);
        setListAdapter(m_adapter);
        registerForContextMenu(getListView());
        getListView().setOnItemClickListener(this);
        //todo: use tabcounter?
        
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

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.status_context_menu, menu);
    }

    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        long rowId = info.id;
        StatusDao status = StatusDao.get(this, rowId);
        long userId = status.getUserId();
        switch (item.getItemId()) {
            case R.id.view_profile:
                UserHelper.viewProfile(this, userId);
                return true;
            case R.id.send_message:
                UserHelper.sendMessage(this, userId);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

     
    private void loadMore() {
    		Log.d(TAG, "loading more statuses");
            setProgressBarIndeterminateVisibility(true);
        	new AsyncTask<Object, Object, Boolean>() {

				@Override
				protected void onPostExecute(Boolean result) {
					setProgressBarIndeterminateVisibility(false);
				}
 
				@Override
				protected Boolean doInBackground(Object... params) {
					try {
						return CGuiTest.s_instance.m_vkService.loadStatuses(m_adapter.getCount(), 
								m_adapter.getCount()+CheckingService.STATUS_NUM_LOAD);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
					return null;
				}
        		
        	}.execute();
    }
    
    public void onScrollStateChanged(AbsListView v, int state) {
        if (state == AbsListView.OnScrollListener.SCROLL_STATE_IDLE
                && getListView().getLastVisiblePosition() == m_adapter.getCount() - 1) {
            loadMore();
        }
    }
    
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long rowId) {
        StatusDao status = StatusDao.get(this, rowId);
        UserHelper.viewProfile(this, status.getUserId());
    }
}