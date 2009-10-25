package org.googlecode.vkontakte_android;

import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import org.googlecode.vkontakte_android.database.StatusDao;
import static org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper.KEY_STATUS_DATE;
import static org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper.KEY_STATUS_PERSONAL;
import static org.googlecode.vkontakte_android.provider.UserapiProvider.STATUSES_URI;
import org.googlecode.vkontakte_android.service.CheckingService;

public class UpdatesListTabActivity extends AutoLoadActivity implements AdapterView.OnItemClickListener {
    private static final String TAG = "org.googlecode.vkontakte_android.UpdatesListTabActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.status_list);
        final Cursor statusesCursor = managedQuery(STATUSES_URI, null, KEY_STATUS_PERSONAL + "=0", null, KEY_STATUS_DATE + " DESC ");

        setupLoader(new UpdatesListTabActivity.Loader() {
            @Override
            public Boolean load() {
                try {
                    return CGuiTest.s_instance.m_vkService.loadStatuses(m_adapter.getCount(),
                            m_adapter.getCount() + CheckingService.STATUS_NUM_LOAD);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                return false;
            }
        }, new UpdatesListAdapter(this, R.layout.status_row, statusesCursor));

        registerForContextMenu(getListView());
        getListView().setOnItemClickListener(this);
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

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long rowId) {
        StatusDao status = StatusDao.get(this, rowId);
        UserHelper.viewProfile(this, status.getUserId());
    }
}