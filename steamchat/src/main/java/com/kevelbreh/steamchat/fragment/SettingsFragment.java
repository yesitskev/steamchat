package com.kevelbreh.steamchat.fragment;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.kevelbreh.steamchat.R;

/**
 * Stock settings fragment for our shared preferences interface.
 */
public class SettingsFragment extends PreferenceFragment {

    /**
     * Preference key returned boolean value of whether the hide offline contacts or not.
     */
    public static final String PREF_FRIENDS_HIDE_OFFLINE = "pref_friends_hide_offline";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
