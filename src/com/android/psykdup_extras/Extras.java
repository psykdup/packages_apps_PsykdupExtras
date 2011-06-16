package com.android.psykdup_extras;

import com.android.psykdup_extras.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.SharedPreferences;
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

import com.android.psykdup_extras.ShellInterface;

public class Extras extends PreferenceActivity 
implements Preference.OnPreferenceChangeListener, OnPreferenceClickListener {

    private static final String TAG = "PsykdupExtras";

    private static final String NOTIF_ADB = "display_adb_usb_debugging_notif";

    private final static String MEDIA_SCANNER_PREF = "media_scanner";

    private final static String MEDIA_SCANNER_ENABLE_CMD = "/system/bin/psykdup ms enable";
	
    private static final String MEDIA_SCANNER_DISABLE_CMD = "/system/bin/psykdup ms disable";
 
    private static final String CLEAN_THUMBS_CMD = "/system/bin/psykdup ctn";

    private static final CharSequence CLEAN_THUMBS_PREF = "clean_thumbnails";

    private static String local_storage_root;
    public static String getStorageRoot() {
	return local_storage_root;	
    }
	public static String getTag() {
		return TAG;
	}

    private CheckBoxPreference mNotifADBPref;
    CheckBoxPreference mMediaScannerPref;
    PreferenceScreen clean_thumbs;
    PackageManager pm;
    ComponentName mediaComponentName;

    @Override
    public void onCreate(Bundle icicle) {
       super.onCreate(icicle);
       addPreferencesFromResource(R.xml.extras);
       local_storage_root = "/psykdup/extras/";
       local_storage_root = Environment.getExternalStorageDirectory().toString()+local_storage_root;
         Resources res = getResources();
         AssetManager assets = res.getAssets();
    
         File f = new File (local_storage_root);
			if(!f.exists())
			if (f.mkdirs())
			Log.d(TAG, "Local repository dir not found, recreated");
			else
				Log.e(TAG, "Local repository dir not found, could not recreate!");
			 
        InputStream is= null;
        OutputStream os = null;
        try {
        is = assets.open("scripts/psykdup");
        os = new FileOutputStream(local_storage_root+"psykdup");
        copyFile(is, os);
        is.close();
        os.flush();
        os.close();
        os = null;
        if(ShellInterface.isSuAvailable()) { 
        ShellInterface.runCommand("busybox mount -o remount,rw  /system");
        ShellInterface.runCommand("cp " +local_storage_root +"nitrality /system/bin/psykdup");
        ShellInterface.runCommand("chmod a+x /system/bin/psykdup");}
        Log.d(TAG, "Copied psykdup script successfully");
		 
		} catch (IOException e) {
			Log.e(TAG, "Problem copying the script: "+ e.getMessage());
		}

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

	clean_thumbs = (PreferenceScreen)prefSet.findPreference(CLEAN_THUMBS_PREF);
	clean_thumbs.setOnPreferenceClickListener(this);
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

    public boolean onPreferenceClick(Preference preference) {
		if (preference == clean_thumbs) {
			if(ShellInterface.isSuAvailable())
				try {
					ShellInterface.runCommand(CLEAN_THUMBS_CMD);
					return true;
					}
					catch (IOException e){
						Toast.makeText(this, "Cleaning thumbnails cache failed!", Toast.LENGTH_LONG);
					}
		}
		return false;
    }
	public static <T> T last(T[] array) {
	    return array[array.length - 1];
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
    private void copyFile(InputStream in, OutputStream out) throws IOException {
	    byte[] buffer = new byte[1024];
	    int read;
	    while((read = in.read(buffer)) != -1) {
	      out.write(buffer, 0, read);
    	    }
    }
}
