package com.kevelbreh.steamchat.activity;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;

import com.kevelbreh.steamchat.fragment.ConversationFragment;
import com.kevelbreh.steamchat.fragment.FriendsFragment;

/**
 * Primary conversation activity containing a {@link com.kevelbreh.steamchat.fragment.ConversationFragment}.
 *
 * It is required that the intent starting this activity contains a data {@link android.net.Uri}
 * pointing towards a valid {@link com.kevelbreh.steamchat.provider.SteamProvider.User}.
 */
public class ConversationActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Uri uri = getIntent().getData();
        getFragmentManager()
                .beginTransaction()
                .add(android.R.id.content, ConversationFragment.forUser(uri))
                .commit();
    }
}
