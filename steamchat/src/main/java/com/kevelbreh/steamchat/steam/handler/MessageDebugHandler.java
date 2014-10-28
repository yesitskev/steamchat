package com.kevelbreh.steamchat.steam.handler;

/**
 * Created by kevin on 2014/08/18.
 */

import com.kevelbreh.steamchat.steam.SteamClient;
import com.kevelbreh.steamchat.steam.SteamServiceHandler;
import com.kevelbreh.steamchat.steam.language.Language;
import com.kevelbreh.steamchat.steam.network.packet.Packet;


import com.kevelbreh.steamchat.SteamChat;


/**
 * This class is responsible for interfacing with client friends specific events.
 */
public class MessageDebugHandler extends AEventHandler implements IEventHandler {

    public MessageDebugHandler(SteamClient client) {
        super(client);
    }

    @Override
    public SteamServiceHandler getHandler() {
        return new IncomingHandler();
    }

    @Override
    public void onEventReceived(SteamClient client, int type, Packet packet) {
        switch (type) {
            case 1:
                SteamChat.debug(this, "Received Multi");
                break;
            case 5501:
                SteamChat.debug(this, "Received ClientServersAvailable");
                break;
            case 751:
                SteamChat.debug(this, "Received ClientLogOnResponse");
                break;
            case 768:
                SteamChat.debug(this, "Received ClientAccountInfo");
                break;
            case 5456:
                SteamChat.debug(this, "Received ClientEmailAddrInfo");
                break;
            case 782:
                SteamChat.debug(this, "Received ClientVACBanStatus");
                break;
            case 767:
                SteamChat.debug(this, "Received ClientFriendsList");
                break;
            case 5553:
                SteamChat.debug(this, "Received ClientFriendsGroupsList");
                break;
            case 5587:
                SteamChat.debug(this, "Received ClientPlayerNicknameList");
                break;
            case 780:
                SteamChat.debug(this, "Received ClientLicenseList");
                break;
            case 798:
                SteamChat.debug(this, "Received ClientUpdateGuestPassesList");
                break;
            case 5528:
                SteamChat.debug(this, "Received ClientWalletInfoUpdate");
                break;
            case 779:
                SteamChat.debug(this, "Received ClientGameConnectTokens");
                break;
            case 850:
                SteamChat.debug(this, "Received ClientSessionToken");
                break;
            case 5430:
                SteamChat.debug(this, "Received ClientIsLimitedAccount");
                break;
            case 783:
                SteamChat.debug(this, "Received ClientCMList");
                break;
            case 880:
                SteamChat.debug(this, "Received ClientServerList");
                break;
            case 5480:
                SteamChat.debug(this, "Received ClientRequestedClientStats");
                break;
            case 5537:
                SteamChat.debug(this, "Received ClientUpdateMachineAuth");
                break;
            case 757:
                SteamChat.debug(this, "Received ClientLoggedOff");
                break;
            case 5463:
                SteamChat.debug(this, "Received ClientNewLoginKey");
                break;
            case 5510:
                SteamChat.debug(this, "Received ClientMarketingMessageUpdate2");
                break;
            case Language.Message.CLIENT_PERSONA_STATE:
                SteamChat.debug(this, "Received ClientPersonaState");
                 break;
            case Language.Message.CLIENT_FRIEND_MSG_INCOMING:
                SteamChat.debug(this, "Received ClientFriendMsgIncoming");
                break;
            case Language.Message.CLIENT_FRIEND_MESSAGE_ECHO_TO_SENDER:
                SteamChat.debug(this, "Received ClientFriendMsgToSender");
                break;
            default:
                SteamChat.debug(this, "Received " + type);
                break;

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

    /**
     * Get messages from service.
     */
    private class IncomingHandler extends SteamServiceHandler {

    }
}

