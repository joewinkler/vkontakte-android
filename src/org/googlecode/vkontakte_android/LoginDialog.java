package org.googlecode.vkontakte_android;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import org.googlecode.vkontakte_android.R;

public class LoginDialog extends Dialog {
    public static final String TAG = "LoginDialog";
	
	private Button button;
    private Button cancel;

    public LoginDialog(final Activity activity) {
        super(activity);
        setContentView(R.layout.login_dialog);
        button = (Button) findViewById(R.id.button_login);
        cancel = (Button) findViewById(R.id.cancel);
        
        setOnCancelClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                	ServiceHelper.getService().stop();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                dismiss();
                activity.finish();
            }
        });
        
        
        setOnLoginClick(new View.OnClickListener() {
            public void onClick(View view) {
               	if (! checkCorrectInput(getLogin(), getPass())) {
                	return;
            	} 
            	showProgress();
                String login = getLogin();
                String pass = getPass();
                Log.i(TAG, login + ":--hidden--" );

                new AsyncTask<String, Void, Boolean>() {
                    @Override
                    protected void onPostExecute(Boolean result) {
                        stopProgress();
                        if (result) {
                            dismiss();
                        } else {
                        	showErrorMessage("Cannot login");
                        }
                    }

                    @Override
                    protected Boolean doInBackground(String... params) {
                        try {
                            return ServiceHelper.getService().login(params[0], params[1]);
                        } catch (RemoteException e) {
                            CGuiTest.fatalError("RemoteException");
                            stopProgress();
                            e.printStackTrace();
                            return false;
                        }
                    }

                }.execute(login, pass);
            }
        });
    }

    public void setOnLoginClick(View.OnClickListener l) {
        button.setOnClickListener(l);
    }
    
    public void setOnCancelClick(View.OnClickListener l) {
        cancel.setOnClickListener(l);
    }

    public String getLogin() {
        return ((TextView) findViewById(R.id.login)).getText().toString();
    }

    public String getPass() {
        return ((TextView) findViewById(R.id.pass)).getText().toString();
    }
    
    public void showProgress() {
    	findViewById(R.id.login).setEnabled(false);
    	findViewById(R.id.pass).setEnabled(false);
    	findViewById(R.id.button_login).setEnabled(false);
    	findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
    }
    
    public void stopProgress() {
    	findViewById(R.id.login).setEnabled(true);
    	findViewById(R.id.pass).setEnabled(true);
    	findViewById(R.id.button_login).setEnabled(true);
    	findViewById(R.id.progress_bar).setVisibility(View.GONE);
    }
    
    public void showErrorMessage(String err) {
    	hideErrorMessage();
    	TextView mess = (TextView)findViewById(R.id.login_error); 
    	mess.setText(err);
    	mess.setVisibility(View.VISIBLE);
    }
    
    public void hideErrorMessage() {
    	findViewById(R.id.login_error).setVisibility(View.GONE);
    }

	public boolean checkCorrectInput(String login, String pass) {
		hideErrorMessage();
	    if (isEmailValid(login)) {
        	Log.d(TAG, "Email format checked");	
        } else if (isLoginValid(login)) {
        	Log.d(TAG, "Login format checked");
        } else {
        	showErrorMessage("Wrong email/login format");
        	return false;
        }
	    
    	if (!TextUtils.isEmpty(pass)) {
    		return true;
    	} else {
    		showErrorMessage("Password shouldn't be empty");
    		return false;
    	}
    	

	}
	
	public static boolean isEmailValid(String s) {
		//Everybody stand back! I know regular expressions! 
		//okay, my email is "some+hello@gmail.com" and it's not pass this regexep, but it's valid.
		//vkontakte doesn't eat such emails. I checked
	    Pattern pattern = Pattern.compile(
		"^[a-zA-Z]{1}[\\w\\.-]*@[a-zA-Z]{1}[\\.\\w-]*\\.[a-zA-Z]{2,7}$");
	    Matcher matcher = pattern.matcher(s);
	    return matcher.matches();
	}
	
	public static boolean isLoginValid(String s) {
		Pattern pattern = Pattern.compile(
		"^\\w+$");
	    Matcher matcher = pattern.matcher(s);
	    return matcher.matches();
	}
	
}
