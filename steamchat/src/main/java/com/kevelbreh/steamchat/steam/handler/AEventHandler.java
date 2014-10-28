package com.kevelbreh.steamchat.steam.handler;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.kevelbreh.steamchat.steam.SteamClient;
import com.kevelbreh.steamchat.steam.SteamService;
import com.kevelbreh.steamchat.steam.SteamServiceHandler;

public abstract class AEventHandler {

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
                // Intentionally unhandled.
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            mService = null;
        }
    };

    /**
     * The steam client.
     */
    private SteamClient client;

    /**
     * Create an EventHandler for the steam network. This also registers a handler to interact
     * with the steam service.
     *
     * @param client for the steam connection.
     */
    public AEventHandler(SteamClient client) {
        this.client = client;
        this.client.getService().bindService(new Intent(client.getContext(), SteamService.class),
                mConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * @return the steam client.
     */
    public SteamClient getSteamClient() {
        return client;
    }

    protected Messenger getService() {
        return mService;
    }

    /**
     * @return the service handler for this steam event handler.
     */
    public abstract SteamServiceHandler getHandler();

}
