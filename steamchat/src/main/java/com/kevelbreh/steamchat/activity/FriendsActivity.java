package com.kevelbreh.steamchat.activity;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.view.View;

import com.kevelbreh.steamchat.fragment.FriendsFragment;

/**
 * Primary friends activity containing a {@link com.kevelbreh.steamchat.fragment.FriendsFragment}.
 */
public class FriendsActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager()
                .beginTransaction()
                .add(android.R.id.content, new FriendsFragment())
                .commit();
    }
}
