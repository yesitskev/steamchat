package com.kevelbreh.steamchat.steam;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;

import com.kevelbreh.steamchat.SteamChat;
import com.kevelbreh.steamchat.account.SteamAccount;
import com.kevelbreh.steamchat.steam.handler.AuthenticationHandler;
import com.kevelbreh.steamchat.steam.handler.IEventHandler;
import com.kevelbreh.steamchat.steam.handler.IHandler;
import com.kevelbreh.steamchat.steam.handler.FriendsHandler;
import com.kevelbreh.steamchat.steam.handler.MessageDebugHandler;
import com.kevelbreh.steamchat.steam.network.TCPConnection;
import com.kevelbreh.steamchat.steam.network.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Steam client class to provide interaction with the steam protocol.  Interfacing directly with
 * the connection is scary so this class has been created.
 *
 * Authenticate in this class.
 */
public class SteamClient implements IHandler {

    /**
     * Android context.
     */
    private Context mContext;

    /**
     * The users steam account on the android device.
     */
    private SteamAccount mAccount;

    /**
     * Android service controlling running the client.
     */
    private SteamService mSteamService;

    /**
     * Connection to the Steam network.  This runs on it's own thread.
     */
    private TCPConnection connection;

    /**
     * A list of handlers for the Steam client.
     */
    private List<IEventHandler> handlers;

    private String source;
    private int port;

    /**
     *
     * @param source ip of the server address.
     * @param port of the server address.
     */
    public SteamClient(final SteamService service, final String source, final int port) {
        this.source = source;
        this.port = port;

        mContext = service.getApplicationContext();
        mSteamService = service;

        handlers = new ArrayList<IEventHandler>();
        handlers.add(new MessageDebugHandler(this));
        handlers.add(new FriendsHandler(this));
        handlers.add(new AuthenticationHandler(this));
    }

    /**
     * Set the account of the SteamClient.
     * @param account the account.
     */
    public void setAccount(SteamAccount account) {
        mAccount = account;
    }

    /**
     * @return the steam account of the user.
     */
    public SteamAccount getAccount() {
        return mAccount;
    }

    /**
     * Reconnect to the steam network.
     */
    public void connect() {
        connection = new TCPConnection(this.source, this.port, this);
        connection.setDaemon(true);
        connection.start();
    }

    /**
     * Disconnect from the steam network.
     */
    public void reconnect() {
        if (connection != null) {
            connection.disconnect();
        }

        connect();
    }

    /**
     * @param packet to be added to the queue.
     */
    public void send(Packet packet) {
        connection.addPacketQueue(packet);
    }

    /**
     * @return the Steam connection.
     */
    public TCPConnection getConnection() {
        return connection;
    }

    /**
     * @return the android application context.
     */
    public Context getContext() {
        return mContext;
    }

    /**
     * @return the android service running the steam client.
     */
    public SteamService getService() {
        return mSteamService;
    }

    /**
     * Tell all the listeners that we have authenticated.
     */
    public void authenticated() {
        SteamChat.debug(this, "Authenticated to Steam network");

        for (IEventHandler handler : this.handlers) {
            handler.onAuthenticated(this);
        }
    }

    @Override
    public void onEventReceived(TCPConnection connection, int type, Packet packet) {
        //SteamChat.debug(this, "New event received: " + type);

        for (IEventHandler handler : this.handlers) {
            handler.onEventReceived(this, type, packet);
        }
    }

    @Override
    public void onConnected(TCPConnection connection) {
        SteamChat.debug(this, "Connected to Steam network");

        for (IEventHandler handler : this.handlers) {
            handler.onConnected(this);
        }
    }

    @Override
    public void onDisconnected(TCPConnection connection) {
        SteamChat.debug(this, "Disconnected from Steam network");

        for (IEventHandler handler : this.handlers) {
            handler.onDisconnected(this);
        }

        //reconnect();
    }
}
