package org.googlecode.vkontakte_android.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;
import org.googlecode.userapi.UrlBuilder;
import org.googlecode.vkontakte_android.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Random;


public class CaptchaHandlerActivity extends Activity implements View.OnClickListener {
    public static final String CAPTCHA_TEXT = "captcha_text";
    private Random random = new Random();
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View mainView = LayoutInflater.from(this).inflate(R.layout.catcha_dialog, null);
        dialog = new AlertDialog.Builder(this)
                .setTitle("Catcha required")
                .setView(mainView)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        dialog.show();

        View refresh = dialog.findViewById(R.id.captcha_refresh);
        refresh.setOnClickListener(this);
        loadCaptcha();
    }

    private String generateCaptchaUrl() {
        String captcha_sid = String.valueOf(Math.abs(random.nextLong()));
        String captcha_url = UrlBuilder.makeUrl("captcha") + "&csid=" + captcha_sid;
        return captcha_url;
    }

    private void loadCaptcha() {
        new AsyncTask<Void, Void, Bitmap>() {
            TextView textView = (TextView) dialog.findViewById(R.id.loading);
            ViewSwitcher vs = (ViewSwitcher) dialog.findViewById(R.id.vs);
            ImageView imageView = (ImageView) dialog.findViewById(R.id.iv);

            @Override
            protected void onPreExecute() {
                vs.setDisplayedChild(1);
                textView.setText("loading captcha");
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                if (bitmap != null) {
                    vs.setDisplayedChild(0);
                    imageView.setImageBitmap(bitmap);
                } else {
                    vs.setDisplayedChild(1);
                    textView.setText("failed to load captcha");
                }
            }

            @Override
            protected Bitmap doInBackground(Void... voids) {
                InputStream is = null;
                Bitmap bitmap = null;
                try {
                    String captchaUrl = generateCaptchaUrl();
                    is = new URL(captchaUrl).openStream();
                    bitmap = BitmapFactory.decodeStream(is);
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
                return bitmap;
            }
        }.execute();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.captcha_refresh:
                loadCaptcha();
                break;
            case android.R.id.button1:
                EditText editText = (EditText) dialog.findViewById(R.id.captcha_text);
                String captchaText = editText.getText().toString();
                Intent data = new Intent();
                data.putExtra(CAPTCHA_TEXT, captchaText);
                setResult(RESULT_OK, data);
                finish();
        }
    }
}