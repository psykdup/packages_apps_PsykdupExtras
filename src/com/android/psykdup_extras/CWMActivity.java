package com.android.psykdup_extras;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.content.Intent;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.widget.Toast;

import com.android.psykdup_extras.R;
import com.android.psykdup_extras.ShellInterface;
	public class CWMActivity extends PreferenceActivity implements OnPreferenceClickListener, OnPreferenceChangeListener{
		public enum Pref {
			CWM_REBOOT_PREF ("cwm_reboot"),
			CWM_BACKUP_PREF ("cwm_backup"),
			CWM_RESTORE_PREF ("cwm_restore"),
			CWM_DELETE_PREF ("cwm_delete"),
			CWM_UPDATE_PREF ("cwm_update");
			    private final CharSequence pref;
		    Pref(CharSequence pref) {
		        this.pref = pref;

		    }
		}
		private final static String CWM_REBOOT_CMD = "reboot recovery";
		private Map<String,Preference> myPrefList;
		public static final String DATE_FORMAT_NOW = "yyyy-MM-dd.HH.mm.ss";
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.cwmactivity);
			myPrefList = new HashMap<String,Preference>();
			PreferenceScreen prefSet = getPreferenceScreen();
			for (Pref pref_name: Pref.values()){
				if (!pref_name.pref.equals("cwm_backup"))
				myPrefList.put((String) pref_name.pref, (PreferenceScreen)prefSet.findPreference(pref_name.pref));
				else{
					EditTextPreference etp = (EditTextPreference)prefSet.findPreference(pref_name.pref);
					etp.setText(now());
					etp.setOnPreferenceChangeListener(this);
					myPrefList.put((String) pref_name.pref, etp);
				}
			};
	        for (Preference pref: myPrefList.values()){
	        	pref.setOnPreferenceClickListener(this);
	        }
	        
		}
		public boolean onPreferenceClick(Preference preference) {
			if (preference == myPrefList.get(Pref.CWM_REBOOT_PREF.pref)) {
				if(ShellInterface.isSuAvailable())
					try {
						ShellInterface.runCommand(CWM_REBOOT_CMD);
						return true;
						}
						catch (IOException e){
							Toast.makeText(this, "Failed!", Toast.LENGTH_LONG);
						}
			}
			if (preference == myPrefList.get(Pref.CWM_BACKUP_PREF.pref)) {
								((EditTextPreference)myPrefList.get(Pref.CWM_BACKUP_PREF.pref)).setText(now());
								return true;
			}
			if (preference == myPrefList.get(Pref.CWM_RESTORE_PREF.pref)) {
				Bundle bundle = new Bundle();
				bundle.putString("path", "/mnt/sdcard/clockworkmod/backup"); //CM
				bundle.putString("action", "restore");
				Intent newIntent = new Intent(this.getApplicationContext(), FilePickerActivity.class);
				newIntent.putExtras(bundle);
				startActivity(newIntent);	
				
			return true;
			}
			if (preference == myPrefList.get(Pref.CWM_UPDATE_PREF.pref)) {
				Bundle bundle = new Bundle();
				bundle.putString("path", "/mnt/sdcard/"); //CM
				bundle.putString("action", "update");
				Intent newIntent = new Intent(this.getApplicationContext(), FilePickerActivity.class);
				newIntent.putExtras(bundle);
				startActivity(newIntent);	
				
			return true;
			}
		if (preference == myPrefList.get(Pref.CWM_DELETE_PREF.pref)) {
			Bundle bundle = new Bundle();
			bundle.putString("path", "/mnt/sdcard/clockworkmod/backup"); //CM
			bundle.putString("action", "delete");
			Intent newIntent = new Intent(this.getApplicationContext(), FilePickerActivity.class);
			newIntent.putExtras(bundle);
			startActivity(newIntent);	
			return true;
		}
			
			return false;
		}
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			StringBuilder command = new StringBuilder("echo 'backup_rom(\"/sdcard/clockworkmod/backup/");
			command.append(String.valueOf(newValue));
			command.append("\");' > /cache/recovery/extendedcommand");
			if(ShellInterface.isSuAvailable())
				try {
					ShellInterface.runCommand(command.toString());
					ShellInterface.runCommand(CWM_REBOOT_CMD);
					return true;
					}
			catch (IOException e){
				Toast.makeText(this, "Failed!", Toast.LENGTH_LONG);
			}
			return false;
		}
		 public static String now() {
			    Calendar cal = Calendar.getInstance();
			    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
			    return sdf.format(cal.getTime());

}
	}
