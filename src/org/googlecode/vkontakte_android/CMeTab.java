package org.googlecode.vkontakte_android;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import org.googlecode.vkontakte_android.database.ProfileDao;
import org.googlecode.vkontakte_android.provider.UserapiProvider;
import org.googlecode.vkontakte_android.utils.ProfileInfoHelper;
import org.googlecode.vkontakte_android.utils.PropertiesHolder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper.KEY_PROFILE_USERID;

public class CMeTab extends Activity {

    private static final String TAG = "org.googlecode.vkontakte_android.CMeTab";
    public static CMeTab s_instance; // :( not good

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CMeTab.s_instance = this;
        setContentView(R.layout.profile_info);

//        TextView updates = (TextView) findViewById(updates_counter);
//        findViewById(R.id.send_status).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                EditText editor = (EditText) findViewById(R.id.editor);
//                String status = editor.getText().toString();
//                try {
//                    boolean result = ServiceHelper.getService().sendStatus(status);
//                    if (result)
//                        Toast.makeText(getApplicationContext(), R.string.status_update_ok, Toast.LENGTH_SHORT).show();
//                    else Toast.makeText(getApplicationContext(), R.string.status_update_err, Toast.LENGTH_SHORT).show();
//                    //todo: return old text if update fails
//                } catch (RemoteException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//
//        ImageButton b = (ImageButton) findViewById(R.id.ImageButton01);
//        b.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                try {
//                    loadProfile();
//                } catch (RemoteException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//
//            }
//
//        });
//
//
//        TableLayout table = (TableLayout) findViewById(R.id.Wall);
//
//
//        //test
//        //View v1 = CWallMessageFactory.getMessageView(this, "Friendname", "message message message message message message message message message message message message message ");
//        //View v2 = CWallMessageFactory.getMessageView(this, "Friendname", "message message message message message message message message message message message message message ");
//        //View v3 = CWallMessageFactory.getMessageView(this, "Friendname", "message message message message message message message message message message message message message");
//        //View v4 = CWallMessageFactory.getMessageView(this, "Friendname", "message message message message message message message message message message message message message");
//
//        //table.addView(v1);
//        //table.addView(v2);
//        //table.addView(v3);
//        //table.addView(v4);
//
//
//        findViewById(R.id.me_avatar).requestFocus();
    }

    boolean loadProfile() throws RemoteException {

        if (!ServiceHelper.getService().loadMyProfile()) {
            Log.e(TAG, "Cannot load profile");
            return false;
        }
        Cursor c = managedQuery(UserapiProvider.PROFILES_URI, null, KEY_PROFILE_USERID + "=?",
                new String[]{Settings.myId.toString()}, null);

        ProfileDao pd;
        if (c != null && c.moveToFirst()) {
            pd = new ProfileDao(c);
        } else {
            Log.e(TAG, "No such profile in DB");
            return false;
        }
        ArrayList<PropertiesHolder> DATA = new ArrayList<PropertiesHolder>();
        if (pd.birthday != null && pd.birthday != 0) {
            SimpleDateFormat format = new SimpleDateFormat("d MMM yyyy");
            DATA.add(new PropertiesHolder(getString(R.string.info_birthday), format.format(new Date(pd.birthday))));
        }
        if (pd.sex != 0) {
            DATA.add(new PropertiesHolder(getString(R.string.info_sex), getString(pd.sex == ProfileInfoHelper.SEX_FEMALE ? R.string.sex_female : R.string.sex_male)));
        }
        if (pd.phone != null) {
            DATA.add(new PropertiesHolder(getString(R.string.info_phone), pd.phone));
        }
        if (pd.politicalViews != 0) {
            int id = ProfileInfoHelper.getPoliticalViewId(pd.politicalViews);
            String politicalViews = getString(R.string.info_views);
            if (id != -1) {
                DATA.add(new PropertiesHolder(politicalViews, getString(id)));
            } else {
                DATA.add(new PropertiesHolder(politicalViews, ""));
            }
        }
        if (pd.familyStatus != 0) {
            int id = ProfileInfoHelper.getFamilyStatusId(pd.familyStatus, pd.sex);
            String status = getString(R.string.info_status);
            if (id != -1) {
                DATA.add(new PropertiesHolder(status, getString(id)));
            } else {
                DATA.add(new PropertiesHolder(status, ""));
            }
        }
        if (pd.currentCity != null) {
            DATA.add(new PropertiesHolder(getString(R.string.info_city), pd.currentCity));
        }

        android.widget.ListView listView = (android.widget.ListView) findViewById(R.id.my_info);
        listView.setAdapter(new ProfileInfoAdapter(this, DATA));
//        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!! "+ (((TabHost)layoutInflater.inflate(R.id.ProfileTabHost, null))!=null));
//        Uri uri = ContentUris.withAppendedId(PROFILES_URI, pd.rowid);
//        InputStream is = null;
//        try {
//            is = getContentResolver().openInputStream(uri);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }


//        Bitmap bm = BitmapFactory.decodeStream(is);
        //= BitmapFactory.decodeByteArray(photo, 0, photo.length);


//        float ratio = (float) bm.getWidth() / (float) bm.getHeight();
//        Log.d(TAG, "size" + bm.getHeight() + " " + bm.getWidth());
//        Bitmap bface = Bitmap.createScaledBitmap(bm, 100, (int) (100 / ratio), false);
//
//        ImageButton face = (ImageButton) findViewById(R.id.me_avatar);
//        face.setImageBitmap(bface);
//
//        TextView fname = (TextView) findViewById(R.id.firstname);
//        fname.setText(pd.firstname);
//
//        TextView sname = (TextView) findViewById(R.id.surname);
//        sname.setText(pd.surname);
//        EditText edit = (EditText) findViewById(R.id.editor);
//        edit.setText(pd.status);

//        fname.setOnLongClickListener(new EditingListener());
//        sname.setOnLongClickListener(new EditingListener());

        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent.getStringExtra("action").equals("load")) {
            try {
                Log.d(TAG, "load");
                loadProfile();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        super.onNewIntent(intent);
    }

//    class EditingListener implements View.OnLongClickListener {
//
//        @Override
//        public boolean onLongClick(View v) {
//            if (!(v instanceof TextView)) {
//                throw new IllegalArgumentException("Must be TextView");
//            }
//            TextPicker pi = new TextPicker(CMeTab.this, (TextView) v);
//            pi.show();
//            return true;
//        }
//    }

}
//class TextPicker extends Dialog {
//
//    TextView m_view;
//    Button m_ok;
//    Button m_cancel;
//    EditText m_editor;
//
//    public TextPicker(Context context, TextView view) {
//        super(context);
//        m_view = view;
//        setContentView(R.layout.textpicker_dialog);
//        m_ok = (Button) findViewById(R.id.button_settext);
//        m_cancel = (Button) findViewById(R.id.text_cancel);
//        m_editor = (EditText) findViewById(R.id.texttoset);
//
//        m_editor.setText(m_view.getText());
//        m_editor.selectAll();
//
//        m_ok.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                CharSequence t = m_editor.getEditableText();
//                if (t != "") {
//                    m_view.setText(t);
//                }
//                dismiss();
//            }
//
//        });
//
//        m_cancel.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                dismiss();
//            }
//
//        });
//    }
//
//
//}
