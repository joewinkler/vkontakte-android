package org.googlecode.vkontakte_android;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.text.InputType;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;
import org.googlecode.vkontakte_android.service.UpdatesNotifier;
import org.googlecode.vkontakte_android.utils.AppHelper;
import org.googlecode.vkontakte_android.utils.ServiceHelper;

import com.nullwire.trace.ExceptionHandler;

public class HomeGridActivity extends Activity implements OnItemClickListener{

    private final static String TAG = "VK:HomeGridActivity";

    private HomeGridAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //register remote-stacktrace handler
        Handler handler = new Handler();
        ExceptionHandler.register(this, handler);
        
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.homegrid);
        GridView mHomeGrid = (GridView) findViewById(R.id.HomeGrid);
//        mHomeGrid.setNumColumns(3);
        adapter = new HomeGridAdapter(this);
        mHomeGrid.setAdapter(adapter);
        mHomeGrid.setOnItemClickListener(this);
        backToHome();
        initStatus();
    }

    private void initStatus() {
        final EditText statusEdit = (EditText) findViewById(R.id.StatusEditText);
        statusEdit.setInputType(InputType.TYPE_NULL);

        statusEdit.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                statusEdit.setInputType(InputType.TYPE_CLASS_TEXT);
                statusEdit.onTouchEvent(event);
                return true;
            }
        });

        findViewById(R.id.StatusSubmitButton).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String statusText = ((EditText) findViewById(R.id.StatusEditText)).getText().toString();
                new AsyncTask<String, Object, Boolean>() {

                    String m_status = "";

                    @Override
                    protected void onPostExecute(Boolean result) {
                        EditText et = ((EditText) findViewById(R.id.StatusEditText));
                        Toast.makeText(et.getContext(), "\"" + et.getText().toString() + "\" Shared!", Toast.LENGTH_SHORT).show();
                        et.setText(result ? m_status : "");
                    }

                    @Override
                    protected Boolean doInBackground(String... params) {
                        try {
                            m_status = params[0];
                            ServiceHelper.getService().sendStatus(m_status);
                            return true;
                        } catch (RemoteException e) {
                            e.printStackTrace();
                            AppHelper.showFatalError(HomeGridActivity.this, "Error while launching the application");
                        }
                        return false;
                    }
                }.execute(new String[]{statusText});
            }
        });

    }

    private void backToHome() {
    	changeTitle("Home");
        setProgressBarIndeterminateVisibility(false);
    }
    
    private void changeTitle(String uiComponent){
    	this.setTitle(" "+getResources().getString(R.string.app_name) + " -> " + uiComponent);
    }
    
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        setProgressBarIndeterminateVisibility(true);

        HomeGridAdapter.Item item = (HomeGridAdapter.Item) HomeGridActivity.this.adapter.getItem(position);
        Intent intent = item.getIntent();

        changeTitle(item.getTitle());

        if (item.getType() == HomeGridAdapter.ItemType.HELP) {
            AboutDialog.makeDialog(this).show();
            backToHome();
        }
        else if (intent != null) {
            startActivity(intent);
        }
        else {
            Toast.makeText(this, "Not implemented", Toast.LENGTH_SHORT).show();
            backToHome();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v(TAG, "Activity resumed");
        // Here user doesn't need notifications anymore
        UpdatesNotifier.clearNotification(getApplicationContext());
        backToHome();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.LogoutMenuItem:
            	VApplication.logout(this);
            	return true;
            case R.id.AboutMenuItem:
                AboutDialog.makeDialog(this).show();
                return true;
            case R.id.ExitMenuItem:
                //VApplication.stopService(this);
            	VApplication.exit();
                return true;
        }
       return super.onOptionsItemSelected(item);
    }
}
