package com.kevelbreh.steamchat.steam.handler;

import com.kevelbreh.steamchat.steam.network.TCPConnection;
import com.kevelbreh.steamchat.steam.network.packet.Packet;

/**
 * Handler Interface {@link com.kevelbreh.steamchat.steam.network.TCPConnection} events.
 */
public interface IHandler {

    /**
     * A new packet has been received.
     * @param connection to the steam network.
     * @param type {@link com.kevelbreh.steamchat.steam.language.Message} of the packet.
     * @param packet received.
     */
    public void onEventReceived(TCPConnection connection, int type, Packet packet);

    /**
     * The connection has been connected to the steam network.
     * @param connection to the steam network.
     */
    public void onConnected(TCPConnection connection);

    /**
     * The connection has been disconnected from the Steam network.
     * @param connection to the steam network.
     */
    public void onDisconnected(TCPConnection connection);

}
