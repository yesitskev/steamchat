package com.kevelbreh.steamchat.steam.handler2;

import com.kevelbreh.steamchat.steam.SteamClient;
import com.kevelbreh.steamchat.steam.language.Language;
import com.kevelbreh.steamchat.steam.network.packet.Packet;

public class UserHandler {

    @Handler.Handle(message = Language.Message.CLIENT_LOG_ON)
    public void onClientLogOn(SteamClient client, Packet packet) {

    }

    @Handler.Handle(message = Language.Message.CLIENT_LOG_ON_RESPONSE)
    public void onClientLogOnResponse(SteamClient client, Packet packet) {

    }

    @Handler.Handle(message = Language.Message.CLIENT_LOGGED_OFF)
    public void onClientLogOff(SteamClient client, Packet packet) {

    }

    @Handler.Handle(message = Language.Message.CLIENT_NEW_LOGIN_KEY)
    public void onClientNewLoginKey(SteamClient client, Packet packet) {

    }

    @Handler.Handle(message = Language.Message.CLIENT_UPDATE_MACHINE_AUTH)
    public void onClientUpdateMachineAuth(SteamClient client, Packet packet) {

    }
}
