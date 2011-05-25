package com.android.psykdup_extras;

import com.android.psykdup_extras.R;

import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.os.Bundle;
import android.util.Log;

public class Extras extends PreferenceActivity {

    private static final String TAG = "PsykdupExtras";

    private static final String NOTIF_ADB = "display_adb_usb_debugging_notif";

    private CheckBoxPreference mNotifADBPref;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.extras);

        final PreferenceScreen prefSet = getPreferenceScreen();

        mNotifADBPref = (CheckBoxPreference)prefSet.findPreference(NOTIF_ADB);
        mNotifADBPref.setChecked(Settings.Secure.getInt(
                getContentResolver(),
                Settings.Secure.DISPLAY_ADB_USB_DEBUGGING_NOTIFICATION, 1) != 0);
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
}
