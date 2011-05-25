package com.android.psykdup_extras;

import com.android.psykdup_extras.R;

import java.io.File;
import java.io.IOException;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class Extras extends PreferenceActivity 
implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "PsykdupExtras";

    private static final String NOTIF_ADB = "display_adb_usb_debugging_notif";

    private final static String MEDIA_SCANNER_PREF = "media_scanner";

    private final static String MEDIA_SCANNER_ENABLE_CMD = "/system/bin/psykdup ms enable";
	
    private static final String MEDIA_SCANNER_DISABLE_CMD = "/system/bin/psykdup ms disable";

    private CheckBoxPreference mNotifADBPref;
    CheckBoxPreference mMediaScannerPref;
    PackageManager pm;
    ComponentName mediaComponentName;

    @Override
    public void onCreate(Bundle icicle) {
       super.onCreate(icicle);
       addPreferencesFromResource(R.xml.extras);

       final PreferenceScreen prefSet = getPreferenceScreen();
       pm = getPackageManager();
       mMediaScannerPref = (CheckBoxPreference)prefSet.findPreference(MEDIA_SCANNER_PREF);
       mMediaScannerPref.setOnPreferenceChangeListener(this);
       mediaComponentName = new ComponentName("com.android.providers.media", "com.android.providers.media.MediaScannerReceiver");
		mMediaScannerPref.setChecked((pm.getComponentEnabledSetting(mediaComponentName)
        				==PackageManager.COMPONENT_ENABLED_STATE_DISABLED) ? false:true);

       mNotifADBPref = (CheckBoxPreference)prefSet.findPreference(NOTIF_ADB);
       mNotifADBPref.setChecked(Settings.Secure.getInt(
                getContentResolver(),
                Settings.Secure.DISPLAY_ADB_USB_DEBUGGING_NOTIFICATION, 1) != 0);
    }

	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference == mMediaScannerPref) {
			//TODO this awaits for better times: pm.setComponentEnabledSetting(mediaComponentName, (Boolean)newValue ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED , 0);
			String command;
			command = (Boolean) newValue ? MEDIA_SCANNER_ENABLE_CMD : MEDIA_SCANNER_DISABLE_CMD; 
			if(ShellInterface.isSuAvailable())
				try {
					ShellInterface.runCommand(command);
					
				} catch (IOException e) {
					Toast.makeText(this, "Media scanner setting failed!", Toast.LENGTH_LONG);
					return false;
				}
				if((Boolean) newValue) scanMedia();
				return true;
		}
		
		return false;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if(preference == mNotifADBPref) {
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.DISPLAY_ADB_USB_DEBUGGING_NOTIFICATION,
                    mNotifADBPref.isChecked() ? 1 : 0);
            return true;
        }

        return false;
    }
    private void scanMedia() {

	    File localFile = Environment.getExternalStorageDirectory();
	    Uri localUri1 = Uri.fromFile(localFile);
	    Intent localIntent1 = new Intent("android.intent.action.MEDIA_MOUNTED", localUri1);
	    sendBroadcast(localIntent1);
	    Uri localUri2 = Uri.parse("file:///mnt/sdcard/external_sd/");
	    Intent localIntent2 = new Intent("android.intent.action.MEDIA_MOUNTED", localUri2);
	    sendBroadcast(localIntent2);
    }
}
