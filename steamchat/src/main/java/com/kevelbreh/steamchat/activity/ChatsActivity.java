package com.kevelbreh.steamchat.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;

import com.kevelbreh.steamchat.fragment.ChatsFragment;
import com.kevelbreh.steamchat.steam2.SteamService;

/**
 * Primary chat activity containing a {@link com.kevelbreh.steamchat.fragment.ChatsFragment}.
 */
public class ChatsActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        getFragmentManager().beginTransaction()
                .add(android.R.id.content, new ChatsFragment())
                .commit();
    }
}
