package com.kevelbreh.steamchat.steam;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.kevelbreh.steamchat.SteamChat;
import com.kevelbreh.steamchat.account.SteamAccount;
import com.kevelbreh.steamchat.activity.AuthenticationActivity;

import java.util.ArrayList;

/**
 * Connect to steam network.
 *  -> Hold up the login screen if there is no account found.
 *  -> Ask for account details
 * Log in the user.
 *  -> Show login screen auth or other things if applicable
 *  -> Update account details
 *  Hide screen and go to conversations.
 *
 *  Start service   -> has account -> connect.
 *                  -> no account -> auth activity -> auth-now = start service.
 *
 *  Any failed login will result in the account getting deleted.
 */
public class SteamService extends Service {

    /**
     * Conversation between the service and other things.
     */
    private final Messenger mMessenger = new Messenger(new IncomingHandler());
    private final ArrayList<Messenger> mBoundClients = new ArrayList<Messenger>();

    /**
     * Steam client.
     */
    private SteamClient client;

    @Override
    public void onCreate() {
        client = new SteamClient(this, "72.165.61.174", 27017);

        /*
         * If there is an existing steam account present then connect it to the steam network. If
         * there isn't one then wait for user data to arrive for a connection attempt.
         */
        SteamAccount account = new SteamAccount(this);
        if (account.hasAccount()) {
            client.setAccount(account);
            client.connect();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    /**
     * Broadcast a message to all connected {@link com.kevelbreh.steamchat.steam.SteamServiceHandler}
     * who are registered to this service.  The receiver can reply back to the service for everyone
     * or can reply directly to the original message source.
     * @param message the {@link android.os.Message} event.
     */
    public void sendBroadcast(Message message) {
        for (Messenger boundClient : mBoundClients) {
            try {
                Message temp = Message.obtain(null, message.what);
                temp.replyTo = message.replyTo;
                temp.setData(message.getData());

                //SteamChat.debug(this, "Broadcasting " + message.what + " from " +
                //        message.replyTo.toString() + " -> " + boundClient.toString());
                boundClient.send(temp);
            }
            catch(final RemoteException e) {
                SteamChat.debug(this, e.toString());
            }
        }
    }

    /**
     * Service handler to communicate the UI interactions with this android service.
     */
    public class IncomingHandler extends SteamServiceHandler {

        @Override
        public void onAddHandler(Message message) {
            mBoundClients.add(message.replyTo);
        }

        @Override
        public void onRemoveHandler(Message message) {
            mBoundClients.remove(message.replyTo);
        }

        @Override
        public void onBroadcast(Message message) {
            sendBroadcast(message);
        }

        @Override
        public void onAuthenticateNow(Message message) {
            client.setAccount(new SteamAccount(SteamService.this, message.getData()));
            client.reconnect();
        }

        @Override
        public void onAuthenticationRequired(Message message) {
            startActivity(new Intent(SteamService.this, AuthenticationActivity.class));
        }
    }
}
