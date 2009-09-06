package org.googlecode.vkontakte_android;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.*;

public class CSettings extends PreferenceActivity implements Preference.OnPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        PreferenceScreen scr = getPreferenceScreen();
        Preference ps = scr.findPreference("sound");
        ps.setOnPreferenceChangeListener(this);

        Preference pn = scr.findPreference("notif");
        pn.setOnPreferenceChangeListener(this);

        Preference list = scr.findPreference("period");
        list.setOnPreferenceChangeListener(this);

        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
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

    //================  work with login/pass

    public static void saveLogin(Context ctx, String login, String pass, String remixpassword, String sid) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        Editor ed = prefs.edit();
        ed.putString("login", login);
        ed.commit();
        ed.putString("password", pass);
        ed.commit();
        ed.putString("remixpassword", remixpassword);
        ed.commit();
        ed.putString("sid", sid);
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
    }

    public static String getPass(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx).getString("password", "");
    }

    public static String getLogin(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx).getString("login", "");
    }

    public static String getRemixPass(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx).getString("remixpassword", "");
    }

    public static String getSid(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx).getString("sid", "");
    }
    
    public static int getPeriod(Context ctx)
    {
    	 return PreferenceManager.getDefaultSharedPreferences(ctx).getInt("period", 60);
    }
}
