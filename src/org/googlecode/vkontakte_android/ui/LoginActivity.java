package org.googlecode.vkontakte_android.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import org.googlecode.vkontakte_android.HomeGridActivity;
import org.googlecode.vkontakte_android.utils.PreferenceHelper;

public class LoginActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!PreferenceHelper.isLogged(this)){
            //        setContentView(R.layout.login_dialog);
            //todo
        } else {

        }
        startActivity(new Intent(this, HomeGridActivity.class));
        finish();
    }
}
