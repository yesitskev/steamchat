package com.kevelbreh.steamchat.steam.handler2;

import com.kevelbreh.steamchat.steam.SteamClient;
import com.kevelbreh.steamchat.steam.language.Language;
import com.kevelbreh.steamchat.steam.network.packet.Packet;

/**
 * Created by Kevin on 2014/08/24.
 */
public class ConnectionHandler {

    @Handler.Handle(message = Language.Message.CHANNEL_ENCRYPT_REQUEST)
    public void onClientLogOn(SteamClient client, Packet packet) {

    }

    @Handler.Handle(message = Language.Message.CHANNEL_ENCRYPT_RESULT)
    public void onClientLogOnResponse(SteamClient client, Packet packet) {

    }

    @Handler.Handle(message = Language.Message.MULTI)
    public void onClientLogOff(SteamClient client, Packet packet) {

    }
}
