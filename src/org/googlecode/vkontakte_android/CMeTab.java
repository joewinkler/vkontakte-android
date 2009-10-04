package org.googlecode.vkontakte_android;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.View;
import android.widget.*;
import android.widget.SimpleAdapter.ViewBinder;
import static org.googlecode.vkontakte_android.R.id.updates_counter;
import org.googlecode.userapi.VkontakteAPI;
import org.googlecode.userapi.ChangesHistory;
import org.googlecode.userapi.VkontakteAPI.photosTypes;
import org.googlecode.vkontakte_android.database.ProfileDao;
import org.googlecode.vkontakte_android.provider.UserapiProvider;
import org.json.JSONException;
import static org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper.*;

import java.io.IOException;

public class CMeTab extends Activity {

    private static final String TAG = "org.googlecode.vkontakte_android.CMeTab";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        TextView updates = (TextView) findViewById(updates_counter);
//        VkontakteAPI api = CGuiTest.api;
//        try {
//            ChangesHistory history = api.getChangesHistory();
//            updates.setText("messages: "+history.getMessagesCount()+", friends: "+history.getFriendsCount()+", photos: "+history.getPhotosCount());
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }

        Button b1 = (Button) findViewById(R.id.send_status);
        b1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                EditText editor = (EditText) findViewById(R.id.editor);
                String status = editor.getText().toString();
                try {
                    boolean result = CGuiTest.s_instance.m_vkService.sendStatus(status);
                    if (result) Toast.makeText(getApplicationContext(), "update success", Toast.LENGTH_SHORT).show();
                    else Toast.makeText(getApplicationContext(), "update failed", Toast.LENGTH_SHORT).show();
                    //todo: return old text if update fails
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

        ImageButton b = (ImageButton) findViewById(R.id.ImageButton01);
        b.setImageDrawable(new BitmapDrawable(CImagesManager.getBitmap("ok")));
        b.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    loadProfile();
                } catch (RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }

        });

        TableLayout table = (TableLayout) findViewById(R.id.Wall);


        //test
        View v1 = CWallMessageFactory.getMessageView(this, "Friendname", "message message message message message message message message message message message message message ");
        View v2 = CWallMessageFactory.getMessageView(this, "Friendname", "message message message message message message message message message message message message message ");
        View v3 = CWallMessageFactory.getMessageView(this, "Friendname", "message message message message message message message message message message message message message");
        View v4 = CWallMessageFactory.getMessageView(this, "Friendname", "message message message message message message message message message message message message message");

        table.addView(v1);
        table.addView(v2);
        table.addView(v3);
        table.addView(v4);

    }

    boolean loadProfile() throws RemoteException {

        if (!CGuiTest.s_instance.m_vkService.loadMyProfile()) {
            return false;
        }
        Cursor c = managedQuery(UserapiProvider.PROFILES_URI, null, KEY_PROFILE_USER + "=?",
                new String[]{CSettings.myId.toString()}, null);

        ProfileDao pd = null;
        if (c != null && c.moveToFirst()) {
            pd = new ProfileDao(c);
        } else {
            Log.e(TAG, "No such profile in DB");
            return false;
        }

        byte photo[] = pd.photo;
        Bitmap bm = BitmapFactory.decodeByteArray(photo, 0, photo.length);


        float ratio = (float) bm.getWidth() / (float) bm.getHeight();
        Log.d(TAG, "size" + bm.getHeight() + " " + bm.getWidth() + " " + ratio);
        Bitmap bface = Bitmap.createScaledBitmap(bm, 100, (int) (100 / ratio), false);

        ImageButton face = (ImageButton) findViewById(R.id.me_avatar);
        face.setImageBitmap(bface);

        TextView fname = (TextView) findViewById(R.id.firstname);
        fname.setText(pd.firstname);

        TextView sname = (TextView) findViewById(R.id.surname);
        sname.setText(pd.surname);

        EditText edit = (EditText) findViewById(R.id.editor);
        edit.setText(pd.status);

        fname.setOnLongClickListener(new EditingListener());
        sname.setOnLongClickListener(new EditingListener());

        return true;
    }

    class EditingListener implements View.OnLongClickListener {

        @Override
        public boolean onLongClick(View v) {
            if (!(v instanceof TextView)) {
                throw new IllegalArgumentException("Must be TextView");
            }
            TextPicker pi = new TextPicker(CMeTab.this, (TextView) v);
            pi.show();
            return true;
        }
    }

}

class TextPicker extends Dialog {

    TextView m_view;
    Button m_ok;
    Button m_cancel;
    EditText m_editor;

    public TextPicker(Context context, TextView view) {
        super(context);
        m_view = view;
        setContentView(R.layout.textpicker_dialog);
        m_ok = (Button) findViewById(R.id.button_settext);
        m_cancel = (Button) findViewById(R.id.text_cancel);
        m_editor = (EditText) findViewById(R.id.texttoset);

        m_editor.setText(m_view.getText());
        m_editor.selectAll();

        m_ok.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                CharSequence t = m_editor.getEditableText();
                if (t != "") {
                    m_view.setText(t);
                }
                dismiss();
            }

        });

        m_cancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
            }

        });
    }


}
