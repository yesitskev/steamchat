package com.kevelbreh.steamchat.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.kevelbreh.steamchat.fragment.InteractionsFragment;

public class InteractionsActivity extends ActionBarActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportFragmentManager().beginTransaction()
                .add(android.R.id.content, new InteractionsFragment())
                .commit();
    }
}
