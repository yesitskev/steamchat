package com.kevelbreh.steamchat.steam.handler2;

import com.kevelbreh.steamchat.steam.SteamClient;
import com.kevelbreh.steamchat.steam.language.Language;
import com.kevelbreh.steamchat.steam.network.packet.Packet;


public class FriendHandler {

    @Handler.Handle(message = Language.Message.CLIENT_ACCOUNT_INFO)
    public void onAccountInfo(SteamClient client, Packet packet) {

    }

    @Handler.Handle(message = Language.Message.CLIENT_FRIENDS_LIST)
    public void onFriendsList(SteamClient client, Packet packet) {

    }

    @Handler.Handle(message = Language.Message.CLIENT_FRIEND_MESSAGE_ECHO_TO_SENDER)
    public void onClientFriendMessageEchoToServer(SteamClient client, Packet packet) {

    }

    @Handler.Handle(message = Language.Message.CLIENT_FRIEND_MSG_INCOMING)
    public void onClientFriendMessageIncoming(SteamClient client, Packet packet) {

    }

    @Handler.Handle(message = Language.Message.CLIENT_PLAYER_NICKNAME_LIST)
    public void onClientPlayerNicknameList(SteamClient client, Packet packet) {

    }

    @Handler.Handle(message = Language.Message.CLIENT_PERSONA_STATE)
    public void onPersonaState(SteamClient client, Packet packet) {

    }
}
