package com.kevelbreh.steamchat.steam2.handler;

import com.kevelbreh.steamchat.provider.SteamProvider;
import com.kevelbreh.steamchat.steam.language.Language;
import com.kevelbreh.steamchat.steam.proto.SteamMessagesClientServerProto;
import com.kevelbreh.steamchat.steam2.SteamEventBus;
import com.kevelbreh.steamchat.steam2.SteamService;
import com.kevelbreh.steamchat.steam.proto.SteamMessagesClientServerProto.CMsgClientFriendMsgIncoming;
import com.kevelbreh.steamchat.steam2.packet.ProtoPacket;

import java.io.IOException;

public class MessageHandler {

    @SteamEventBus.SteamEvent(event = Language.Message.CLIENT_FRIEND_MSG_INCOMING)
    public static void onClientFriendMessageIncoming(SteamService service, final byte[] data) throws IOException {
        final ProtoPacket<CMsgClientFriendMsgIncoming.Builder> packet =
                new ProtoPacket<CMsgClientFriendMsgIncoming.Builder>(CMsgClientFriendMsgIncoming.class);

        packet.setData(data);
        packet.deserialize();

        SteamProvider.Interaction.addChatIncoming(service, packet.getBody());
    }

    @SteamEventBus.SteamEvent(event = Language.Message.CLIENT_FRIEND_MESSAGE_ECHO_TO_SENDER)
    public static void onClientFriendMessageEchoToServer(SteamService service, final byte[] data) throws IOException {
        final ProtoPacket<CMsgClientFriendMsgIncoming.Builder> packet =
                new ProtoPacket<CMsgClientFriendMsgIncoming.Builder>(CMsgClientFriendMsgIncoming.class);

        packet.setData(data);
        packet.deserialize();

        SteamProvider.Interaction.addEchoChatOutgoing(service, packet.getBody());
    }
}
