package com.kevelbreh.steamchat.activity;

import android.app.Activity;
import android.os.Bundle;

import com.kevelbreh.steamchat.fragment.SettingsFragment;

/**
 * Pretty stock settings activity with preference fragment.
 */
public class SettingsActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager()
                .beginTransaction()
                .add(android.R.id.content, new SettingsFragment())
                .commit();
    }
}
