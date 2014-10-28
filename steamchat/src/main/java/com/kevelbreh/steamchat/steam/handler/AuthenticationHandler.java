package com.kevelbreh.steamchat.steam.handler;

import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;

import com.google.protobuf.ByteString;
import com.kevelbreh.steamchat.SteamChat;
import com.kevelbreh.steamchat.account.SteamAccount;
import com.kevelbreh.steamchat.steam.proto.SteamMessagesClientServerProto;
import com.kevelbreh.steamchat.steam.proto.SteamMessagesClientServerProto.CMsgClientNewLoginKey;
import com.kevelbreh.steamchat.steam.proto.SteamMessagesClientServerProto.CMsgClientNewLoginKeyAccepted;
import com.kevelbreh.steamchat.steam.proto.SteamMessagesClientServerProto.CMsgClientSessionToken;
import com.kevelbreh.steamchat.steam.proto.SteamMessagesClientServerProto.CMsgClientUpdateMachineAuth;
import com.kevelbreh.steamchat.steam.proto.SteamMessagesClientServerProto.CMsgClientUpdateMachineAuthResponse;
import com.kevelbreh.steamchat.steam.SteamClient;
import com.kevelbreh.steamchat.steam.SteamID;
import com.kevelbreh.steamchat.steam.SteamServiceHandler;
import com.kevelbreh.steamchat.steam.language.Language;
import com.kevelbreh.steamchat.steam.network.packet.ClientLogOn;
import com.kevelbreh.steamchat.steam.network.packet.ClientLogOnResponse;
import com.kevelbreh.steamchat.steam.network.packet.Packet;
import com.kevelbreh.steamchat.steam.network.packet.ProtoPacket;
import com.kevelbreh.steamchat.steam.security.Cryptography;
import com.kevelbreh.steamchat.steam.util.BinaryReader;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * This class is responsible for interfacing with client user specific events.
 */
public class AuthenticationHandler extends AEventHandler implements IEventHandler {

    public AuthenticationHandler(SteamClient client) {
        super(client);
    }

    @Override
    public SteamServiceHandler getHandler() {
        return new SteamServiceHandler();
    }

    @Override
    public void onEventReceived(SteamClient client, int type, Packet packet) {
        switch(type) {
            case Language.Message.CLIENT_LOG_ON_RESPONSE:
                doUserLoginResponse(client, packet);
                break;

            case Language.Message.CLIENT_NEW_LOGIN_KEY:
                doClientNewLoginKey(client, packet);
                break;

            case Language.Message.CLIENT_SESSION_TOKEN:
                doClientSessionToken(client, packet);
                break;

            case Language.Message.CLIENT_UPDATE_MACHINE_AUTH:
                doClientUpdateMachineAuth(client, packet);
                break;

            case 757:

                ProtoPacket<SteamMessagesClientServerProto.CMsgClientLoggedOff.Builder> p =
                        new ProtoPacket<SteamMessagesClientServerProto.CMsgClientLoggedOff.Builder>(SteamMessagesClientServerProto.CMsgClientLoggedOff.class);
                p.withData(packet.getData());

                try {
                    p.deserialize();
                    SteamChat.debug(this, "CMsgClientLoggedOff: " + p.getBody().getEresult());
                }
                catch(final IOException e) {
                    SteamChat.debug(this, e.toString());
                }

                break;
        }
    }

    @Override
    public void onConnected(SteamClient client) {
        SteamAccount account = client.getAccount();
        if (account.hasAccount()) {
            SteamChat.debug(this,"Has Account");
            doUserAccountLogin(client, account);
            return;
        }

        if (account.hasAccountIntent()) {
            final Bundle data = account.getData();

            final String username = data.getString("username");
            final String password = data.getString("password");
            final String guard = data.getString("guard", null);
            final String machine = data.getString("machine", null);
            if (guard != null && machine != null) {
                doUserLogin(client, username, password, guard, machine);
            } else {
                doUserLogin(client, username, password);
            }
        }
    }

    @Override
    public void onDisconnected(SteamClient client) {
        // Do nothing.
    }

    @Override
    public void onAuthenticated(SteamClient client) {
        // Do nothing.
    }


    public void doUserAccountLogin(SteamClient client, SteamAccount account) {
        final ClientLogOn packet = new ClientLogOn();
        final SteamID user = new SteamID(0,
                0, client.getConnection().getUniverse(), Language.Account.INDIVIDUAL);

        packet.getHeader().setClientSessionid(0);
        packet.getHeader().setSteamid(user.getLong());

        packet.getBody().setAccountName(account.getExtra("username"));
        // Todo: Set the password in the account. ideally you want the accepted steam login key rather.
        //packet.getBody().setPassword("");
        //packet.getBody().setLoginKey(account.getExtra("login_key"));
        packet.getBody().setObfustucatedPrivateIp(client.getConnection().getLocalIpAddress() ^ 0xBAADF00D);
        packet.getBody().setProtocolVersion(65579); // current protocol
        packet.getBody().setClientOsType(-203); // linux unknown
        packet.getBody().setClientPackageVersion(1771);
        packet.getBody().setClientLanguage("english");
        packet.getBody().setSteam2TicketRequest(false);

        File file = client.getContext().getFileStreamPath(account.getExtra("sentry"));
        byte[] data = new byte[(int) file.length()];
        try {
            BufferedInputStream stream = new BufferedInputStream(new FileInputStream(file));
            stream.read(data, 0, data.length);
            stream.close();
        }
        catch(final IOException e) {
            SteamChat.debug(this, e.toString(), e);
        }

        byte[] SHA = Cryptography.SHAHash(data);

        packet.getBody().setShaSentryfile(ByteString.copyFrom(SHA));
        packet.getBody().setEresultSentryfile(Language.Result.OK);
        packet.getBody().setMachineId(ByteString.copyFrom(getDeviceId(client.getContext())));

        client.send(packet);
    }

    /**
     * Attempt to log in the user.
     */
    public void doUserLogin(SteamClient client, String username, String password) {
        final ClientLogOn packet = getLoginPacket(client);
        packet.getBody().setAccountName(username);
        packet.getBody().setPassword(password);
        client.send(packet);
    }

    /**
     * Attempt to log in the user.
     */
    public void doUserLogin(SteamClient client, String username, String password, String guard, String machine) {
        final ClientLogOn packet = getLoginPacket(client);
        packet.getBody().setAccountName(username);
        packet.getBody().setPassword(password);
        packet.getBody().setAuthCode(guard);
        packet.getBody().setMachineName(machine);
        client.send(packet);
    }

    /**
     * Setup the login packet without steam credentials of any kind.
     * @param client of the connection.
     * @return a new login packet.
     */
    private ClientLogOn getLoginPacket(SteamClient client) {
        final ClientLogOn packet = new ClientLogOn();
        final SteamID user = new SteamID(0, 1, client.getConnection().getUniverse(), Language.Account.INDIVIDUAL);

        packet.getHeader().setClientSessionid(0);
        packet.getHeader().setSteamid(user.getLong());
        packet.getBody().setObfustucatedPrivateIp(client.getConnection().getLocalIpAddress() ^ 0xBAADF00D);
        packet.getBody().setProtocolVersion(65579); // current protocol
        packet.getBody().setClientOsType(-203); // linux unknown
        packet.getBody().setClientLanguage("english");
        packet.getBody().setSteam2TicketRequest(false);
        packet.getBody().setClientPackageVersion(1771);
        packet.getBody().clearShaSentryfile();
        packet.getBody().setEresultSentryfile(Language.Result.FILE_NOT_FOUND);
        packet.getBody().setMachineId(ByteString.copyFrom(getDeviceId(client.getContext())));


        return packet;
    }

    /**
     * Respond to the user's log in attempt.  Report back if the user has successfully logged in, if
     * the user requires a Steam Guard token to login in, or invalid username or password.
     * @param client of this Steam connection.
     * @param packet of the {@link com.kevelbreh.steamchat.steam.language.Language} response.
     */
    private void doUserLoginResponse(SteamClient client, Packet packet) {
        boolean resetConnection = true;

        if (packet.isProto()) {
            try {
                ClientLogOnResponse response = new ClientLogOnResponse().withData(packet.getData());
                response.deserialize();

                SteamChat.debug(this, "LoginResult=" + response.getBody().getEresult());

                switch(response.getBody().getEresult()) {

                    /*
                    * Logging in was successful.
                    */
                    case Language.Result.OK:
                        resetConnection = false;
                        android.os.Message message1 =
                                android.os.Message.obtain(null, SteamServiceHandler.AUTHENTICATION_SUCCESS);
                        message1.replyTo = getService();

                        SteamAccount account = client.getAccount();
                        account.setCredentials();
                        account.setExtra("account_flags", String.valueOf(response.getBody().getAccountFlags()));
                        account.setExtra("steam_id", String.valueOf(response.getBody().getClientSuppliedSteamid()));

                        client.authenticated();
                        client.getService().sendBroadcast(message1);
                        break;

                    /*
                    * If the account has been denied access, then it's safe to assume that Steam Guard is
                    * in effect.  We need to log in again with the access code emailed to the user's
                    * email account.
                    */
                    case Language.Result.ACCOUNT_LOGON_DENIED:
                    case Language.Result.ACCESS_DENIED:
                        android.os.Message message2 =
                                android.os.Message.obtain(null, SteamServiceHandler.AUTHENTICATION_GUARDED);
                        message2.replyTo = getService();
                        client.getService().sendBroadcast(message2);
                        break;

                    /*
                    * The user had sent an incorrect password.  The user has to log in again with the
                    * correct credentials.
                    */
                    case Language.Result.INVALID_PASSWORD:
                        android.os.Message message3 =
                                android.os.Message.obtain(null, SteamServiceHandler.AUTHENTICATION_INVALID_PASSWORD);
                        message3.replyTo = getService();
                        client.getService().sendBroadcast(message3);
                        break;

                    /*
                     * Default failure of logging in the user.
                     */
                    default:
                        android.os.Message message4 =
                                android.os.Message.obtain(null, SteamServiceHandler.AUTHENTICATION_REQUIRED);
                        message4.replyTo = getService();
                        client.getService().sendBroadcast(message4);
                        break;
                }
            }
            catch(final IOException e) {
                SteamChat.debug(this, "Login Exception: " + e.toString());
            }
        }

        if (resetConnection) {
            client.getConnection().disconnect();
        }
    }


    /**
     * Todo: Investigate what this does exactly.
     * @param client of this Steam connection.
     * @param packet of the {@link com.kevelbreh.steamchat.steam.language.Language} response.
     */
    private void doClientNewLoginKey(SteamClient client, Packet packet) {
        ProtoPacket<CMsgClientNewLoginKey.Builder> p =
                new ProtoPacket<CMsgClientNewLoginKey.Builder>(CMsgClientNewLoginKey.class);
        p.withData(packet.getData());

        try {
            p.deserialize();
            client.getAccount().setExtra("login_key", p.getBody().getLoginKey());

            ProtoPacket<CMsgClientNewLoginKeyAccepted.Builder> p2 =
                    new ProtoPacket<CMsgClientNewLoginKeyAccepted.Builder>(CMsgClientNewLoginKeyAccepted.class);

            p2.getHeader().setSteamid(client.getConnection().steamid);
            p2.getHeader().setClientSessionid(client.getConnection().sessionid);
            p2.getHeader().setJobidTarget(p.getHeader().getJobidSource());
            p2.getHeader().setJobidSource(BinaryReader.LongMaxValue);

            p2.setMessageType(5464);
            p2.getBody().setUniqueId(p.getBody().getUniqueId());
            client.send(p2);

            SteamChat.debug(this, "Sent CMsgClientNewLoginKeyAccepted");
        }
        catch(final IOException e) {
            SteamChat.debug(this, e.toString());
        }
    }

    /**
     * Receive and set a Steam3 session token used for authenticating to various other steam services.
     * Apparently this is used for Steam2 content downloading.
     * @param client of this Steam connection.
     * @param packet of the {@link com.kevelbreh.steamchat.steam.language.Language} response.
     */
    private void doClientSessionToken(SteamClient client, Packet packet) {
        ProtoPacket<CMsgClientSessionToken.Builder> p =
                new ProtoPacket<CMsgClientSessionToken.Builder>(CMsgClientSessionToken.class);
        p.withData(packet.getData());

        try {
            p.deserialize();
            client.getAccount().setExtra("session_token", String.valueOf(p.getBody().getToken()));
        }
        catch(final IOException e) {
            SteamChat.debug(this, e.toString());
        }
    }

    /**
     * Steam wants the client to update the local machines authentication data.
     * @param client of this Steam connection.
     * @param packet of the {@link com.kevelbreh.steamchat.steam.language.Language} response.
     */
    private void doClientUpdateMachineAuth(SteamClient client, Packet packet) {
        ProtoPacket<CMsgClientUpdateMachineAuth.Builder> p =
                new ProtoPacket<CMsgClientUpdateMachineAuth.Builder>(CMsgClientUpdateMachineAuth.class);
        p.withData(packet.getData());

        try {
            p.deserialize();

            final String filename = p.getBody().getFilename();
            final byte[] data = p.getBody().getBytes().toByteArray();
            final byte[] hash = Cryptography.SHAHash(data);

            client.getAccount().setExtra("sentry", filename);
            FileOutputStream stream = client.getContext().openFileOutput(filename, Context.MODE_PRIVATE);
            stream.write(data);
            stream.close();

            ProtoPacket<CMsgClientUpdateMachineAuthResponse.Builder> respond =
                    new ProtoPacket<CMsgClientUpdateMachineAuthResponse.Builder>(CMsgClientUpdateMachineAuthResponse.class);

            respond.setMessageType(5538);
            respond.getHeader().setSteamid(client.getConnection().steamid);
            respond.getHeader().setClientSessionid(client.getConnection().sessionid);
            respond.getHeader().setJobidTarget(p.getHeader().getJobidSource());
            respond.getHeader().setJobidSource(BinaryReader.LongMaxValue);

            respond.getBody().setCubwrote(p.getBody().getCubtowrite());
            respond.getBody().setEresult(Language.Result.OK);
            respond.getBody().setFilename(filename);
            respond.getBody().setFilesize(data.length);
            respond.getBody().setGetlasterror(0);
            respond.getBody().setOffset(p.getBody().getOffset());
            respond.getBody().setShaFile(ByteString.copyFrom(hash));
            respond.getBody().setOtpIdentifier(p.getBody().getOtpIdentifier());
            respond.getBody().setOtpType(p.getBody().getOtpType());
            respond.getBody().setOtpValue(0);

            SteamChat.debug(this, "Sent CMsgClientUpdateMachineAuthResponse");
            client.send(respond);
        }
        catch(final IOException e) {
            SteamChat.debug(this, e.toString());
        }
    }

    /**
     * @return a steam suitable device id for this device.  The device id will only change once the
     * user does a factory restore on their phone.
     */
    private static byte[] getDeviceId(Context context) {
        final String android_id = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        return android_id.getBytes();
    }
}
