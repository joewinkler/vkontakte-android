package org.googlecode.vkontakte_android;

import android.app.Dialog;
import android.content.Context;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.view.View;
import org.googlecode.vkontakte_android.R;

public class LoginDialog extends Dialog {
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
}
