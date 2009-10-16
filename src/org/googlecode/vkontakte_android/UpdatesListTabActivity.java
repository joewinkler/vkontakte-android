package org.googlecode.vkontakte_android;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import org.googlecode.vkontakte_android.database.StatusDao;
import org.googlecode.vkontakte_android.database.UserDao;
import static org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper.*;
import static org.googlecode.vkontakte_android.provider.UserapiProvider.STATUSES_URI;

public class UpdatesListTabActivity extends ListActivity implements AdapterView.OnItemClickListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.status_list);
        final Cursor statusesCursor = managedQuery(STATUSES_URI, null, null, null, KEY_STATUS_DATE + " DESC ");
        UpdatesListAdapter adapter = new UpdatesListAdapter(this, R.layout.status_row, statusesCursor);
        setListAdapter(adapter);
        registerForContextMenu(getListView());
        getListView().setOnItemClickListener(this);
        //todo: use tabcounter?
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