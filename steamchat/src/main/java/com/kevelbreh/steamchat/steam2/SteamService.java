package com.kevelbreh.steamchat.steam2;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.kevelbreh.steamchat.SteamChat;
import com.kevelbreh.steamchat.account.SteamAccount;
import com.kevelbreh.steamchat.steam2.handler.ConnectionHandler;
import com.kevelbreh.steamchat.steam2.handler.FriendHandler;
import com.kevelbreh.steamchat.steam2.handler.MessageHandler;
import com.kevelbreh.steamchat.steam2.handler.UserHandler;

import java.util.ArrayList;


public class SteamService extends Service {

    public static final int REGISTER = -1;
    public static final int DEREGISTER = -2;

    public static final int EVENT_STEAM_CHANNEL_READY = 1;
    public static final int EVENT_STEAM_USER_LOGIN = 2;


    private final Messenger mMessenger = new Messenger(new SteamServiceHandler());
    private final ArrayList<Messenger> mBoundClients = new ArrayList<Messenger>();

    private SteamEventBus mEventBus;
    private SteamConnection mConnection;
    private SteamAccount mAccount;

    @Override
    public void onCreate() {
        super.onCreate();

        mAccount = new SteamAccount(this);

        // Start the event bus with the defined handlers.
        mEventBus = new SteamEventBus(this);
        mEventBus.register(ConnectionHandler.class);
        mEventBus.register(FriendHandler.class);
        mEventBus.register(MessageHandler.class);
        mEventBus.register(UserHandler.class);
        mEventBus.start();

        // Start the steam connection.
        mConnection = new SteamConnection("72.165.61.185", 27017);
        mConnection.setDaemon(true);
        mConnection.setDataReceivedListener(new SteamConnection.OnDataReceivedListener() {
            @Override
            public void onDataReceived(int event, boolean proto, byte[] data) {
                SteamChat.debug(this, "onDataReceived: event=" + event + " proto=" + proto + " length="+data.length);
                mEventBus.handleSteamEvent(event, proto, data);
            }
        });
        mConnection.start();
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
     * @return the current steam connection
     */
    public synchronized SteamConnection getSteamConnection() {
        return mConnection;
    }

    /**
     * Sets a new steam connection to be used by the service.  Every time the connection dies, we need
     * to set a new one.
     *
     * @param connection to be used for steam.
     */
    public synchronized void setSteamConnection(SteamConnection connection) {
        mConnection = connection;
    }

    /**
     * @return the event bus used for handling steam and user events to and from the steam universe.
     */
    public synchronized SteamEventBus getSteamEventBus() {
        return mEventBus;
    }

    /**
     *
     * @param account
     */
    public synchronized void setSteamAccount(SteamAccount account) {
        mAccount = account;
    }

    /**
     *
     * @param account
     * @return
     */
    public synchronized SteamAccount getSteamAccount() {
        return mAccount;
    }

    /**
     *
     */
    public synchronized void resetSteamConnection() {
        if (getSteamConnection().isAlive()) {
            mConnection.close();
        }

		/*
		InetAddress.getByName("72.165.61.174"),
				InetAddress.getByName("72.165.61.175"),
				InetAddress.getByName("72.165.61.176"),
				InetAddress.getByName("72.165.61.185"),
				InetAddress.getByName("72.165.61.187"),
				InetAddress.getByName("72.165.61.188"),

				InetAddress.getByName("208.64.200.202"),
				InetAddress.getByName("208.64.200.203"),
				InetAddress.getByName("208.64.200.204"),
				InetAddress.getByName("208.64.200.205"),
				InetAddress.getByName("208.64.200.201"),

				InetAddress.getByName("146.66.152.12"),
				InetAddress.getByName("146.66.152.13"),
				InetAddress.getByName("146.66.152.14"),
				InetAddress.getByName("146.66.152.15"),

				InetAddress.getByName("81.171.115.34"),
				InetAddress.getByName("81.171.115.35"),
				InetAddress.getByName("81.171.115.36"),
				InetAddress.getByName("81.171.115.37"),

				InetAddress.getByName("209.197.30.36"),
				InetAddress.getByName("205.185.220.134"),
				InetAddress.getByName("209.197.6.233"),
				InetAddress.getByName("209.197.29.196"),
				InetAddress.getByName("209.197.29.197"),
				InetAddress.getByName("103.28.54.10"),
				InetAddress.getByName("103.28.54.11"),
				InetAddress.getByName("208.64.200.137"),
				InetAddress.getByName("183.136.139.25"),
				InetAddress.getByName("183.136.139.27")
		 */

        mConnection = new SteamConnection("72.165.61.185", 27017);
        mConnection.setDaemon(true);
        mConnection.setDataReceivedListener(new SteamConnection.OnDataReceivedListener() {
            @Override
            public void onDataReceived(int event, boolean proto, byte[] data) {
                mEventBus.handleSteamEvent(event, proto, data);
            }
        });
        mConnection.start();
    }

    /**
     * Broadcast a message to all connected {@link com.kevelbreh.steamchat.steam.SteamServiceHandler}
     * who are registered to this service.  The receiver can reply back to the service for everyone
     * or can reply directly to the original message source.
     * @param message the {@link android.os.Message} event.
     */
    public synchronized void sendBroadcast(int event, Bundle data) {
        for (Messenger boundClient : mBoundClients) {
            try {
                Message message = Message.obtain(null, event);
                message.setData(data);
                boundClient.send(message);
            }
            catch(final RemoteException e) {
                SteamChat.debug(this, e.toString());
            }
        }
    }

    private class SteamServiceHandler extends Handler {

        @Override
        public void handleMessage(Message message) {
            switch (message.what) {

                /**
                 * Register the calling messenger
                 */
                case REGISTER:
                    mBoundClients.add(message.replyTo);
                    break;

                /**
                 * Deregister the calling messenger with this service.
                 */
                case DEREGISTER:
                    mBoundClients.remove(message.replyTo);
                    break;

                /**
                 * Pass the message event and data to the event bus for handling.
                 */
                default:
                    SteamChat.debug(this, "Adding event from client");
                    mEventBus.handleUserEvent(message.what, message.getData());
            }
        }
    }
}