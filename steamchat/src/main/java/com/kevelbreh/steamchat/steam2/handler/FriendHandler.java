package com.kevelbreh.steamchat.steam2.handler;

import android.content.ContentValues;
import android.net.Uri;

import com.kevelbreh.steamchat.SteamChat;
import com.kevelbreh.steamchat.provider.SteamProvider;
import com.kevelbreh.steamchat.provider.SteamProvider.Nickname;
import com.kevelbreh.steamchat.provider.SteamProvider.Persona;
import com.kevelbreh.steamchat.provider.SteamProvider.User;
import com.kevelbreh.steamchat.provider.SteamProviderUtils;
import com.kevelbreh.steamchat.steam.language.Language;
import com.kevelbreh.steamchat.steam.proto.SteamMessagesClientServerProto.CMsgClientFriendsList;
import com.kevelbreh.steamchat.steam.proto.SteamMessagesClientServerProto.CMsgClientPlayerNicknameList;
import com.kevelbreh.steamchat.steam.proto.SteamMessagesClientServerProto.CMsgClientPersonaState;
import com.kevelbreh.steamchat.steam2.SteamEventBus;
import com.kevelbreh.steamchat.steam2.SteamService;
import com.kevelbreh.steamchat.steam2.packet.ProtoPacket;

import java.io.IOException;


public class FriendHandler {

    @SteamEventBus.SteamEvent(event = Language.Message.CLIENT_PERSONA_STATE)
    public static void onClientPersonaState(SteamService service, final byte[] data) throws IOException {
        ProtoPacket<CMsgClientPersonaState.Builder> packet = new ProtoPacket<CMsgClientPersonaState.Builder>(CMsgClientPersonaState.class);
        packet.setData(data);
        packet.deserialize();

        for (CMsgClientPersonaState.Friend friend : packet.getBody().getFriendsList()) {
            ContentValues values = new ContentValues(15);
            values.put(Persona.AVATAR_HASH, friend.getAvatarHash().toByteArray()); // blob
            values.put(Persona.CLAN_RANK, friend.getClanRank()); // int
            values.put(Persona.CLAN_TAG, friend.getClanTag()); // str
            values.put(Persona.FACEBOOK_ID, friend.getFacebookId()); // long
            values.put(Persona.GAME_ID, friend.getGameid()); // long
            values.put(Persona.GAME_NAME, friend.getGameName()); // str
            values.put(Persona.GAME_PLAYED_APP_ID, friend.getGamePlayedAppId()); // int
            values.put(Persona.GAME_SERVER_IP, friend.getGameServerIp());// int
            values.put(Persona.GAME_SERVER_PORT, friend.getGameServerPort()); // int
            values.put(Persona.LAST_LOG_OFF, friend.getLastLogoff()); // int
            values.put(Persona.LAST_LOG_ON, friend.getLastLogon()); //int
            values.put(Persona.PERSONA_SET_BY_USER, friend.getPersonaSetByUser()); // bool
            values.put(Persona.PERSONA_STATE, friend.getPersonaState()); // int
            values.put(Persona.PERSONA_STATE_FLAGS, friend.getPersonaStateFlags()); // int
            values.put(Persona.PLAYER_NAME, friend.getPlayerName()); //str

            final Uri userUri = SteamProviderUtils.getUserFromSteamId(service, friend.getFriendid());

            // Continue if we didn't manage to find the user id.
            if (userUri == null) {
                SteamChat.debug("Couldn't find user for " + friend.getFriendid()
                        + "(" + friend.getPlayerName() +")");
                continue;
            }

            final Uri personaUri = SteamProvider.User.PERSONA.getUri(userUri);
            if (!SteamProviderUtils.isExisting(service, personaUri, null, null)) {
                // There isn't a persona for this user so add one.
                service.getContentResolver().insert(personaUri, values);
            } else {
                // There is already an existing persona so update the entry.  We can overwrite the
                // all the data because we have no idea what changed (and adding checks is tedious).
                service.getContentResolver().update(personaUri, values, null, null);
            }
        }
    }

    @SteamEventBus.SteamEvent(event = Language.Message.CLIENT_FRIENDS_LIST)
    public static void onClientFriendsList(SteamService service, final byte[] data)  throws IOException {
        ProtoPacket<CMsgClientFriendsList.Builder> packet = new ProtoPacket<CMsgClientFriendsList.Builder>(CMsgClientFriendsList.class);
        packet.setData(data);
        packet.deserialize();

        String selection = SteamProvider.User.STEAM_ID + "=?";

        for (CMsgClientFriendsList.Friend friend : packet.getBody().getFriendsList()) {
            ContentValues values = new ContentValues(2);
            values.put(User.STEAM_ID, friend.getUlfriendid());
            values.put(User.RELATIONSHIP, friend.getEfriendrelationship());

            String[] args = new String[] { String.valueOf(friend.getUlfriendid()) };
            if (!SteamProviderUtils.isExisting(service, User.CONTENT_URI, selection, args)) {
                // This steam user does not yet exist so add a new entry.
                service.getContentResolver().insert(User.CONTENT_URI, values);
            } else {
                // This team user already exists so update the entry.  We can overwrite the
                // steam_id because it won't be changing.
                service.getContentResolver().update(User.CONTENT_URI, values, selection, args);
            }
        }
    }

    @SteamEventBus.SteamEvent(event = Language.Message.CLIENT_FRIENDS_GROUPS_LIST)
    public static void onClientFriendsGroupsList(SteamService service, final byte[] data)  throws IOException {

    }

    @SteamEventBus.SteamEvent(event = Language.Message.CLIENT_PLAYER_NICKNAME_LIST)
    public static void onClientPlayerNicknameList(SteamService service, final byte[] data) throws IOException {
        final ProtoPacket<CMsgClientPlayerNicknameList.Builder> packet = new ProtoPacket<CMsgClientPlayerNicknameList.Builder>(CMsgClientPlayerNicknameList.class);
        packet.setData(data);
        packet.deserialize();

        for (CMsgClientPlayerNicknameList.PlayerNickname nickname : packet.getBody().getNicknamesList()) {
            ContentValues values = new ContentValues(1);
            values.put(Nickname.NICKNAME, nickname.getNickname());

            // Continue if we didn't manage to find the user id.
            final Uri userUri = SteamProviderUtils.getUserFromSteamId(service, nickname.getSteamid());
            if (userUri == null) {
                SteamChat.debug("Couldn't find user for " + nickname.getSteamid() + "(" + nickname.getNickname() + ")");
                continue;
            }

            final Uri nicknameUri = User.NICKNAME.getUri(userUri);
            if (!SteamProviderUtils.isExisting(service, nicknameUri, null, null)) {
                // There isn't a nickname for this user so add one.
                service.getContentResolver().insert(nicknameUri, values);
            } else {
                // There is already an existing nickname so update the entry.
                service.getContentResolver().update(nicknameUri, values, null, null);
            }
        }
    }
}
