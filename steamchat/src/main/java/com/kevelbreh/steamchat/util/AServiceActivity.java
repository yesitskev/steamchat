package com.kevelbreh.steamchat.util;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.kevelbreh.steamchat.SteamChat;
import com.kevelbreh.steamchat.steam.SteamService;
import com.kevelbreh.steamchat.steam.SteamServiceHandler;

/**
 * Abstract class that allows an activity to easily communicate to the service.
 */
public abstract class AServiceActivity extends Activity {

    /**
     * Service connection which connects to {@link com.kevelbreh.steamchat.steam.SteamService} for
     * us to send messages.  This can also be used for receiving the "typing..." signal.
     */
    private Messenger mService = null;
    private Messenger mMessenger = new Messenger(getHandler());
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);

            try {
                Message message = Message.obtain(null, SteamServiceHandler.HANDLER_ADD);
                message.replyTo = mMessenger;
                mService.send(message);
            }
            catch(final RemoteException e) {
                SteamChat.debug(this, e.toString());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            mService = null;
        }
    };

    /**
     * @return a service handler.
     */
    public abstract SteamServiceHandler getHandler();

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

        if (mService != null) try {
            Message msg = Message.obtain(null, SteamServiceHandler.HANDLER_REMOVE);
            msg.replyTo = mService;
            mService.send(msg);
        } catch (RemoteException e) {
            // Left empty.
        }

        unbindService(mConnection);
    }
}
