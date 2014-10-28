package com.kevelbreh.steamchat.steam.handler;

import com.kevelbreh.steamchat.steam.SteamClient;
import com.kevelbreh.steamchat.steam.network.packet.Packet;

/**
 * Handler Interface {@link com.kevelbreh.steamchat.steam.network.TCPConnection} events.
 */
public interface IEventHandler {

    /**
     * A new packet has been received.
     * @param client which is connected to the network.
     * @param type {@link com.kevelbreh.steamchat.steam.language.Message} of the packet.
     * @param packet received.
     */
    public void onEventReceived(SteamClient client, int type, Packet packet);

    /**
     * The connection has been connected to the steam network.
     * @param client which is connected to the network.
     */
    public void onConnected(SteamClient client);

    /**
     * The connection has been disconnected from the Steam network.
     * @param client which is connected to the network.
     */
    public void onDisconnected(SteamClient client);

    /**
     * The user has logged in with their steam account.
     * @param client which is connected to the network.
     */
    public void onAuthenticated(SteamClient client);
}
