package com.kevelbreh.steamchat.steam.handler;

import com.kevelbreh.steamchat.SteamChat;
import com.kevelbreh.steamchat.provider.SteamProvider;
import com.kevelbreh.steamchat.steam.SteamClient;
import com.kevelbreh.steamchat.steam.SteamServiceHandler;
import com.kevelbreh.steamchat.steam.language.Language;
import com.kevelbreh.steamchat.steam.network.packet.Packet;
import com.kevelbreh.steamchat.steam.network.packet.ProtoPacket;
import com.kevelbreh.steamchat.steam.proto.SteamMessagesClientServerProto.CMsgClientFriendsList;
import com.kevelbreh.steamchat.steam.proto.SteamMessagesClientServerProto.CMsgClientPersonaState;
import com.kevelbreh.steamchat.steam.proto.SteamMessagesClientServerProto.CMsgClientPlayerNicknameList;
import com.kevelbreh.steamchat.steam.proto.SteamMessagesClientServerProto.CMsgClientChangeStatus;
import com.kevelbreh.steamchat.steam.proto.SteamMessagesClientServerProto.CMsgClientFriendMsgIncoming;
import com.kevelbreh.steamchat.steam.util.BinaryReader;

import java.io.IOException;

/**
 * This class is responsible for interfacing with client friends specific events.
 */
public class FriendsHandler extends AEventHandler implements IEventHandler {

    public FriendsHandler(SteamClient client) {
        super(client);
    }

    @Override
    public SteamServiceHandler getHandler() {
        return new SteamServiceHandler();
    }

    @Override
    public void onEventReceived(SteamClient client, int type, Packet packet) {
        try {
            switch (type) {
                case 768:
                    onClientAccountInfo(packet);
                    break;

                case Language.Message.CLIENT_LOGGED_OFF:
                    break;

                case 767:
                    onFriendsList(packet);
                    break;

                case 5553:
                    onGroupList(packet);
                    break;

                case 5587:
                    onNicknameList(packet);
                    break;

                case Language.Message.CLIENT_PERSONA_STATE:
                    onPersonaState(packet);
                    break;

                case Language.Message.CLIENT_FRIEND_MSG_INCOMING:
                    onFriendMessage(packet);
                    break;

                case Language.Message.CLIENT_FRIEND_MESSAGE_ECHO_TO_SENDER:
                    onEchoMessage(packet);
                    break;
            }
        }
        catch(final IOException e) {
            SteamChat.debug(this, e.toString());
       }
    }

    @Override
    public void onConnected(SteamClient client) {

    }

    @Override
    public void onDisconnected(SteamClient client) {

    }

    @Override
    public void onAuthenticated(SteamClient client) {

    }

    private void onEchoMessage(Packet packet) throws IOException {
        ProtoPacket<CMsgClientFriendMsgIncoming.Builder> incoming =
                new ProtoPacket<CMsgClientFriendMsgIncoming.Builder>(CMsgClientFriendMsgIncoming.class);
        incoming.deserialize(packet.getData());

        SteamProvider.Interaction.addEchoChatOutgoing(getSteamClient().getContext(), incoming.getBody());
    }

    private void onFriendMessage(Packet packet) throws IOException {
        ProtoPacket<CMsgClientFriendMsgIncoming.Builder> incoming =
                new ProtoPacket<CMsgClientFriendMsgIncoming.Builder>(CMsgClientFriendMsgIncoming.class);
        incoming.deserialize(packet.getData());

        SteamProvider.Interaction.addChatIncoming(getSteamClient().getContext(), incoming.getBody());
    }

    // todo: save persona name.
    private void onClientAccountInfo(Packet packet) throws IOException {
        ProtoPacket<CMsgClientChangeStatus.Builder> request =
                new ProtoPacket<CMsgClientChangeStatus.Builder>(CMsgClientChangeStatus.class);

        request.setMessageType(Language.Message.CLIENT_CHANGE_STATUS);
        request.getHeader().setClientSessionid(getSteamClient().getConnection().sessionid);
        request.getHeader().setSteamid(getSteamClient().getConnection().steamid);
        request.getHeader().setJobidTarget(BinaryReader.LongMaxValue);
        request.getHeader().setJobidSource(BinaryReader.LongMaxValue);

        request.getBody().setPersonaState(Language.PersonaState.ONLINE);
        request.getBody().setPlayerName("Kev");

        getSteamClient().send(request);
    }

    private void onFriendsList(Packet packet) throws IOException {
        ProtoPacket<CMsgClientFriendsList.Builder> p =
                new ProtoPacket<CMsgClientFriendsList.Builder>(CMsgClientFriendsList.class);
        p.deserialize(packet.getData());

        SteamProvider.User.addFriendsList(getSteamClient().getContext(), p.getBody().getFriendsList());

        /*ProtoPacket<CMsgClientRequestFriendData.Builder> request =
                new ProtoPacket<CMsgClientRequestFriendData.Builder>(CMsgClientRequestFriendData.class);

        for (CMsgClientFriendsList.Friend friend : p.getBody().getFriendsList()) {
            request.getBody().addFriends(friend.getUlfriendid());
        }

        request.setMessageType(Language.Message.CLIENT_REQUEST_FRIEND_DATA);
        request.getHeader().setClientSessionid(getSteamClient().getConnection().sessionid);
        request.getHeader().setSteamid(getSteamClient().getConnection().steamid);
        request.getHeader().setJobidTarget(BinaryReader.LongMaxValue);
        request.getHeader().setJobidSource(BinaryReader.LongMaxValue);

        request.getBody().setPersonaStateRequested(
                Language.ClientPersonaStateFlag.PLAYER_NAME |
                Language.ClientPersonaStateFlag.PRESENCE |
                Language.ClientPersonaStateFlag.SOURCE_ID |
                Language.ClientPersonaStateFlag.GAME_EXTRA_INFO |
                Language.ClientPersonaStateFlag.METADATA |
                Language.ClientPersonaStateFlag.STATUS |
                Language.ClientPersonaStateFlag.LAST_SEEN);

        getSteamClient().send(request);*/
    }

    private void onGroupList(Packet packet)  throws IOException {
        /*ProtoPacket<CMsgClientFriendsGroupsList.Builder> p =
                new ProtoPacket<CMsgClientFriendsGroupsList.Builder>(CMsgClientFriendsGroupsList.class);
        p.deserialize(packet.getData());

        List<CMsgClientFriendsGroupsList.FriendGroup> groups = p.getBody().getFriendGroupsList();
        for (CMsgClientFriendsGroupsList.FriendGroup group  : groups) {
            SteamChat.debug(this, "GROUP: " + group.getNGroupID() + " AS " + group.getStrGroupName());
        }*/
    }

    private void onNicknameList(Packet packet)  throws IOException {
        ProtoPacket<CMsgClientPlayerNicknameList.Builder> p =
                new ProtoPacket<CMsgClientPlayerNicknameList.Builder>(CMsgClientPlayerNicknameList.class);
        p.deserialize(packet.getData());

        SteamProvider.Nickname.addNicknameList(getSteamClient().getContext(), p.getBody().getNicknamesList());
    }

    private void onPersonaState(Packet packet) throws IOException {
        ProtoPacket<CMsgClientPersonaState.Builder> p =
                new ProtoPacket<CMsgClientPersonaState.Builder>(CMsgClientPersonaState.class);
        p.deserialize(packet.getData());

        SteamProvider.Persona.addPersonaList(getSteamClient().getContext(), p.getBody().getFriendsList());
    }
}
