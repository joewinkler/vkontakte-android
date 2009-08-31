package org.googlecode.vkontakte_android;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;

public class CSettings extends PreferenceActivity  implements Preference.OnPreferenceChangeListener
{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		
		PreferenceScreen scr = getPreferenceScreen();
		Preference ps  =  scr.findPreference("sound");
		ps.setOnPreferenceChangeListener(this);
		
		Preference pn  =  scr.findPreference("notif");
		pn.setOnPreferenceChangeListener(this);
		
		Preference list  =  scr.findPreference("period");
		list.setOnPreferenceChangeListener(this); 

		//SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		//Log.d("pr",prefs.getString("period", " "));
		
	}

	@Override
	public boolean onPreferenceChange(Preference arg0, Object arg1) {
		String key = arg0.getKey();
		
		if (key.equals("period"))
		{
			ListPreference pr = ((ListPreference)arg0); 
			int pos = pr.findIndexOfValue((String)arg1);
			pr.setSummary(pr.getEntries()[ pos ]);
		}
		return true;
	}

}
