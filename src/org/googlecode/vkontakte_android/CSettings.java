package org.googlecode.vkontakte_android;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.*;
import org.googlecode.userapi.Credentials;

public class CSettings extends PreferenceActivity implements Preference.OnPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        
        PreferenceScreen scr = getPreferenceScreen();
        
        Preference ps = scr.findPreference("sound");
        ps.setOnPreferenceChangeListener(this);

        Preference pn = scr.findPreference("notif");
        pn.setOnPreferenceChangeListener(this);

        ListPreference list = (ListPreference)scr.findPreference("period");
        list.setOnPreferenceChangeListener(this);
        list.setSummary(list.getEntry());
        

        
        //Log.d("pr",prefs.getString("period", " "));

    }

    @Override
    public boolean onPreferenceChange(Preference arg0, Object arg1) {
        String key = arg0.getKey();

        if (key.equals("period")) {
            ListPreference pr = ((ListPreference) arg0);
            int pos = pr.findIndexOfValue((String) arg1);
            pr.setSummary(pr.getEntries()[pos]);
        }
        return true;
    }

    public static boolean shouldLoadPics(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean("pics", true);
    }


    //================  work with login/pass

    public static Long myId = 0L;

    public static void saveLogin(Context ctx, Credentials cred) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        Editor ed = prefs.edit();
        ed.putString("login", cred.getLogin());
        ed.commit();
        ed.putString("password", cred.getPass());
        ed.commit();
        ed.putString("remixpassword", cred.getRemixpass());
        ed.commit();
    }

    public static void saveLogin(Context ctx, String login, String pass, String remixpassword) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        Editor ed = prefs.edit();
        ed.putString("login", login);
        ed.commit();
        ed.putString("password", pass);
        ed.commit();
        ed.putString("remixpassword", remixpassword);
        ed.commit();
    }

    public static boolean isLogged(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.contains("login") && prefs.contains("password");
    }

    public static void clearPrivateInfo(Context ctx) {
        Editor ed = PreferenceManager.getDefaultSharedPreferences(ctx).edit();
        ed.remove("login");
        ed.commit();
        ed.remove("password");
        ed.commit();
        ed.remove("remixpassword");
        ed.commit();
    }

    public static String getPass(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx).getString("password", null);
    }

    public static String getLogin(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx).getString("login", null);
    }

    public static String getRemixPass(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx).getString("remixpassword", null);
    }

    public static int getPeriod(Context ctx) {
        return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(ctx).getString("period", "60"))*1000;
    }
}
