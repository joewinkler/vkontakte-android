package org.googlecode.vkontakte_android.ui;

import android.app.Activity;
import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.widget.TextView;
import org.googlecode.vkontakte_android.R;
import org.googlecode.vkontakte_android.utils.PreferenceHelper;
import org.googlecode.vkontakte_android.utils.ServiceHelper;


public class LoginActivity extends Activity implements View.OnClickListener {
    private final int DIALOG_PROGRESS = 0;
    private final int DIALOG_ERROR_PASSWORD = 1;
    private final int DIALOG_ERROR_CAPTCHA = 2;
    private final int DIALOG_ERROR_CONNECTION = 3;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!PreferenceHelper.isLogged(this)) {
            setContentView(R.layout.login_dialog);
            findViewById(R.id.button_login).setOnClickListener(this);
            findViewById(R.id.cancel).setOnClickListener(this);
            login(getLogin(), getPass());
            //todo
        } else {

        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_login:

                break;
            case R.id.cancel:
                finish();
                break;
        }
    }

    private void login(String login, String pass) {
        new AsyncTask<String, Void, Boolean>() {
            @Override
            protected void onPostExecute(Boolean result) {
                if (result) {

                } else {

                }
            }

            @Override
            protected Boolean doInBackground(String... params) {
                try {
//                     LoginResult result = ServiceHelper.getService().login(new Credentials(params[0], params[1], null));
//                    if (result.isSuccess()){
//
//                    } else {
//                        Exception e = result.getCause();
//                        if (e instanceof UserapiLoginException){
//                            UserapiLoginException.ErrorType type = ((UserapiLoginException) e).getType();
//                            switch (type){
//                                case CAPTCHA_INCORRECT:
//                                    //...
//                                    break;
//                            }
//                        }
//                    }
                    return ServiceHelper.getService().login(params[0], params[1], null);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }.execute(login, pass);
    }

    private String getLogin() {
        return ((TextView) findViewById(R.id.login)).getText().toString();
    }

    private String getPass() {
        return ((TextView) findViewById(R.id.pass)).getText().toString();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id){
            case DIALOG_PROGRESS:
            case DIALOG_ERROR_CONNECTION:
            case DIALOG_ERROR_PASSWORD:
            case DIALOG_ERROR_CAPTCHA:
                return null;
            default:
                return super.onCreateDialog(id);
        }
    }
}