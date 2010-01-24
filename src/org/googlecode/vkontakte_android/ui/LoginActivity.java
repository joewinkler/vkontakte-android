package org.googlecode.vkontakte_android.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.*;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.widget.TextView;
import org.googlecode.userapi.Credentials;
import org.googlecode.userapi.UserapiLoginException;
import org.googlecode.vkontakte_android.HomeGridActivity;
import org.googlecode.vkontakte_android.R;
import org.googlecode.vkontakte_android.service.CheckingService;
import org.googlecode.vkontakte_android.service.MyRemoteException;
import org.googlecode.vkontakte_android.utils.PreferenceHelper;
import org.googlecode.vkontakte_android.utils.ServiceHelper;

import java.io.IOException;
import java.util.concurrent.Semaphore;


public class LoginActivity extends Activity implements View.OnClickListener, ServiceConnection {
    private final int DIALOG_PROGRESS = 0;
    private final int DIALOG_ERROR_PASSWORD = 1;
    private final int DIALOG_ERROR_CAPTCHA = 2;
    private final int DIALOG_ERROR_CONNECTION = 3;
    private final int DIALOG_ERROR_REMOTE = 4;

    private Semaphore serviceWaitLock = new Semaphore(0);
    private AsyncTask<String, Void, RemoteException> currentTask;

    private boolean viewIsLoaded = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindService(new Intent(this, CheckingService.class), this, Context.BIND_AUTO_CREATE);
        if (!PreferenceHelper.isLogged(this)) {
            setupMainView();
        } else {
            Credentials credentials = PreferenceHelper.getCredentials(this);
            login(credentials.getLogin(), credentials.getPass(), credentials.getRemixpass());
        }
    }

    private void setupMainView() {
        setContentView(R.layout.login_dialog);
        findViewById(R.id.button_login).setOnClickListener(this);
        findViewById(R.id.cancel).setOnClickListener(this);
    }

    private void startHome() {
        Intent intent = new Intent(this, HomeGridActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_login:
                login(getLogin(), getPass(), null);
                break;
            case R.id.cancel:
                finish();
                break;
        }
    }

    private void login(String login, String pass, String remixpass) {
        currentTask = new AsyncTask<String, Void, RemoteException>() {
            @Override
            protected void onPreExecute() {
                LoginActivity.this.showDialog(DIALOG_PROGRESS);
            }

            @Override
            protected void onCancelled() {
                //todo: cancel connection?
            }

            @Override
            protected void onPostExecute(RemoteException e) {
                dismissDialog(DIALOG_PROGRESS);
                if (e == null) {
                    PreferenceHelper.setLogged(LoginActivity.this, true);
                    startHome();
                } else {
                    if (!viewIsLoaded){
                        setupMainView();
                    }
                    if (e instanceof MyRemoteException) {
                        Exception exception = ((MyRemoteException) e).innerException;
                        if (exception instanceof UserapiLoginException) {
                            switch (((UserapiLoginException) exception).getType()) {
                                case LOGIN_INCORRECT:
                                case LOGIN_INCORRECT_CAPTCHA_NOT_REQUIRED:
                                    LoginActivity.this.showDialog(DIALOG_ERROR_PASSWORD);
                                    PreferenceHelper.setLogged(LoginActivity.this, false);
                                    break;
                                case CAPTCHA_INCORRECT:
                                case LOGIN_INCORRECT_CAPTCHA_REQUIRED:
                                    LoginActivity.this.showDialog(DIALOG_ERROR_CAPTCHA);
                                    break;
                            }
                        }
                        if (exception instanceof IOException) {
                            LoginActivity.this.showDialog(DIALOG_ERROR_CONNECTION);
                        }
                    } else {
                        LoginActivity.this.showDialog(DIALOG_ERROR_REMOTE);
                    }
                }
            }

            @Override
            protected RemoteException doInBackground(String... params) {
                if (ServiceHelper.getService() == null) {
                    try {
                        serviceWaitLock.acquire();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    ServiceHelper.getService().login(params[0], params[1], params[2]);
                } catch (RemoteException e) {
                    return e;
                }
                return null;
            }
        }.execute(login, pass, remixpass);

    }

    private String getLogin() {
        return ((TextView) findViewById(R.id.login)).getText().toString();
    }

    private String getPass() {
        return ((TextView) findViewById(R.id.pass)).getText().toString();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_PROGRESS:
                ProgressDialog pd = new ProgressDialog(this);
                pd.setMessage("please wait");
                pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                pd.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        if (currentTask != null) currentTask.cancel(true);
                    }
                });
                return pd;
            case DIALOG_ERROR_CONNECTION:
                return new AlertDialog.Builder(this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Connection error!")
                        .setMessage("Connection error!")
                        .setPositiveButton(android.R.string.ok, null)
                        .create();
            //todo: retry
            case DIALOG_ERROR_PASSWORD:
                return new AlertDialog.Builder(this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Password incorrect!")
                        .setMessage("No such e-mail address has been registered or your password is incorrect.")
                        .setPositiveButton(android.R.string.ok, null)
                        .create();
            case DIALOG_ERROR_CAPTCHA:
                return new AlertDialog.Builder(this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Captcha required, please try again later")
                        .setMessage("Captcha required, please try again later or login with captcha on durov.ru")
                        .setPositiveButton(android.R.string.ok, null)
                        .create();
            case DIALOG_ERROR_REMOTE:
                return new AlertDialog.Builder(this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Internal error!")
                        .setMessage("Internal error!")
                        .setPositiveButton(android.R.string.ok, null)
                        .create();
            default:
                return super.onCreateDialog(id);
        }
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder service) {
        ServiceHelper.connect(service);
        serviceWaitLock.release();
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        ServiceHelper.disconnect();
    }


    @Override
    public void onStop() {
        unbindService(this);
        super.onStop();
    }


}