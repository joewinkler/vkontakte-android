package org.googlecode.vkontakte_android.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import org.googlecode.vkontakte_android.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;


public class CaptchaHandlerActivity extends Activity {
    public static final String CAPTCHA_URL = "url";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        String url = getIntent().getStringExtra(CAPTCHA_URL);
        String url = "http://userapi.com/data?act=captcha&csid=123321123";
        if (url == null) throw new IllegalArgumentException("no url provided");
        View mainView = LayoutInflater.from(this).inflate(R.layout.catcha_dialog, null);

        Dialog dialog = new AlertDialog.Builder(this)
                .setTitle("Catcha required")
                .setView(mainView)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        dialog.show();
        ImageView imageView = (ImageView) mainView.findViewById(R.id.iv);

        InputStream is = null;
        try {
            is = new URL(url).openStream();
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            imageView.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
//        setResult(RESULT_OK);
//        finish();
    }
}