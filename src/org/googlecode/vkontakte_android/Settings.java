package org.googlecode.vkontakte_android;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import org.googlecode.vkontakte_android.utils.PreferenceHelper;

public class Settings extends PreferenceActivity implements Preference.OnPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        PreferenceScreen scr = getPreferenceScreen();
        
        Preference ps = scr.findPreference("sound");
        ps.setOnPreferenceChangeListener(this);

        Preference pn = scr.findPreference("notif");
        pn.setOnPreferenceChangeListener(this);

        ListPreference list = (ListPreference)scr.findPreference(PreferenceHelper.SYNC_PERIOD);
        list.setOnPreferenceChangeListener(this);
        list.setSummary(list.getEntry());
    }

    @Override
    public boolean onPreferenceChange(Preference arg0, Object arg1) {
        String key = arg0.getKey();

        if (key.equals(PreferenceHelper.SYNC_PERIOD)) {
            ListPreference pr = ((ListPreference) arg0);
            int pos = pr.findIndexOfValue((String) arg1);
            pr.setSummary(pr.getEntries()[pos]);
            setResult(RESULT_OK);
        }
        return true;
    }
    public static Long myId = 0L;
}
