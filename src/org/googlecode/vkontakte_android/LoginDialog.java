package org.googlecode.vkontakte_android;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Dialog;
import android.content.Context;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import org.googlecode.vkontakte_android.R;

public class LoginDialog extends Dialog {
    public static final String TAG = "LoginDialog";
	
	private Button button;
    private Button cancel;

    public LoginDialog(Context context) {
        super(context);
        setContentView(R.layout.login_dialog);
        button = (Button) findViewById(R.id.button_login);
        cancel = (Button) findViewById(R.id.cancel);
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
    	((ProgressBar)findViewById(R.id.progress_bar)).setVisibility(View.VISIBLE);
    }
    
    public void stopProgress() {
    	((ProgressBar)findViewById(R.id.progress_bar)).setVisibility(View.GONE);
    }
    
    public void showErrorMessage(String err) {
    	hideErrorMessage();
    	TextView mess = (TextView)findViewById(R.id.login_error); 
    	mess.setText(err);
    	mess.setVisibility(View.VISIBLE);
    }
    
    public void hideErrorMessage() {
    	((TextView)findViewById(R.id.login_error)).setVisibility(View.GONE);
    }

	public boolean checkCorrectInput(String login, String pass) {
		//Everybody stand back! I know regular expressions! 
	    Pattern pattern = Pattern.compile(
		"^\\w{1}+[\\w\\d\\.-]*@[\\w\\d]+[\\.\\w\\d-]*\\.[\\w&&[^\\-]]{2,7}$");
	        
	    Matcher matcher = pattern.matcher(login);
	    if (matcher.matches()) {
        	Log.d(TAG, "Email format checked");	
        	if (!TextUtils.isEmpty(pass)) {
        		return true;
        	}
        	showErrorMessage("Password shouldn't be empty");
        	return false;
        } else {
        	showErrorMessage("Wrong email format");
        	Log.d(TAG, "Email wrong format");
        	return false;
        }
	}
}
