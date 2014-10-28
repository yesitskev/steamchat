package com.kevelbreh.steamchat.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.kevelbreh.steamchat.SteamChat;
import com.kevelbreh.steamchat.steam2.SteamService;

/**
 * Created by Kevin on 2014/08/28.
 */
public class TestActivity extends Activity {

    /**
     * Service connection which connects to {@link com.kevelbreh.steamchat.steam.SteamService} for
     * us to send messages.  This can also be used for receiving the "typing..." signal.
     */
    private Messenger mService = null;
    private Messenger mMessenger = new Messenger(new Handler());
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            mService = null;
        }
    };

    /**
     * @return a messenger to connect or relay information to the running
     * {@link com.kevelbreh.steamchat.steam.SteamService}.
     */
    protected Messenger getService() {
        return mService;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bindService(new Intent(this, SteamService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
    }

}
