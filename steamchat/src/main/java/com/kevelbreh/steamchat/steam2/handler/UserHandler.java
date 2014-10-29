package com.kevelbreh.steamchat.steam2.handler;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

import com.google.protobuf.ByteString;
import com.kevelbreh.steamchat.SteamChat;
import com.kevelbreh.steamchat.account.SteamAccount;
import com.kevelbreh.steamchat.activity.AuthenticationActivity;
import com.kevelbreh.steamchat.steam.SteamID;
import com.kevelbreh.steamchat.steam.language.Language;
import com.kevelbreh.steamchat.steam.proto.SteamMessagesClientServerProto;
import com.kevelbreh.steamchat.steam.proto.SteamMessagesClientServerProto.CMsgClientAccountInfo;
import com.kevelbreh.steamchat.steam.proto.SteamMessagesClientServerProto.CMsgClientLogon;
import com.kevelbreh.steamchat.steam.proto.SteamMessagesClientServerProto.CMsgClientLogonResponse;
import com.kevelbreh.steamchat.steam.proto.SteamMessagesClientServerProto.CMsgClientSessionToken;
import com.kevelbreh.steamchat.steam.proto.SteamMessagesClientServerProto.CMsgClientChangeStatus;
import com.kevelbreh.steamchat.steam.proto.SteamMessagesClientServerProto.CMsgClientUpdateMachineAuth;
import com.kevelbreh.steamchat.steam.proto.SteamMessagesClientServerProto.CMsgClientUpdateMachineAuthResponse;
import com.kevelbreh.steamchat.steam.proto.SteamMessagesClientServerProto.CMsgClientNewLoginKey;
import com.kevelbreh.steamchat.steam.proto.SteamMessagesClientServerProto.CMsgClientNewLoginKeyAccepted;
import com.kevelbreh.steamchat.steam.security.Cryptography;
import com.kevelbreh.steamchat.steam.util.BinaryReader;
import com.kevelbreh.steamchat.steam2.SteamConnection;
import com.kevelbreh.steamchat.steam2.SteamEventBus;
import com.kevelbreh.steamchat.steam2.SteamService;
import com.kevelbreh.steamchat.steam2.packet.ProtoPacket;
import com.nostra13.universalimageloader.utils.IoUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;


public class UserHandler {

    @SuppressWarnings("unused")
    @SteamEventBus.UserEvent(event = SteamService.EVENT_STEAM_USER_LOGIN)
    public static void doSteamLogin(SteamService service, final Bundle data) {
        service.getSteamAccount().setData(data);
        service.resetSteamConnection();
    }

    @SuppressWarnings("unused")
    @SteamEventBus.UserEvent(event = SteamService.EVENT_STEAM_CHANNEL_READY)
    public static void onSteamChannelReady(SteamService service, final Bundle data) {
        final ProtoPacket<CMsgClientLogon.Builder> request = new ProtoPacket<CMsgClientLogon.Builder>
                (CMsgClientLogon.class, Language.Message.CLIENT_LOG_ON);
        final SteamID user = new SteamID(0, 1, 1, Language.Account.INDIVIDUAL); // get proper universe.

        // If there is null data and there is no account, open up the authentication activity so that the
        // user can try log in.
        SteamAccount account = service.getSteamAccount();
        if (account.getData() == null && !account.hasAccount()) {
            SteamChat.debug("directing logging in to authentication activity.");
            Intent intent = new Intent(service, AuthenticationActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            service.startActivity(intent);
            service.getSteamConnection().close();
            return;
        }

        // If the user has an account then start logging in the user with the credentials saved within
        // the found account.
        // todo: Set the username and password found in the account or something
        if (account.hasAccount()) {
            SteamChat.debug("Logging in using account");
            request.getBody().setAccountName(account.getExtra("username"));
            request.getBody().setPassword(account.getExtra("password"));
            //packet.getBody().setLoginKey(account.getExtra("login_key"));
            request.getBody().setEresultSentryfile(Language.Result.OK);
            request.getBody().setShaSentryfile(getSentryHash(service, service.getSteamAccount().getExtra("sentry")));
        }

        // Else if there is no account found but there is at least temp data set; attempt to then log
        // in the user for the first time using the temp data.
        else {
            SteamChat.debug("Logging in using temp data from authentication");
            final Bundle temp = service.getSteamAccount().getData();
            request.getBody().setAccountName(temp.getString("username"));
            request.getBody().setPassword(temp.getString("password"));
            final String guard = temp.getString("guard", null);
            final String machine = temp.getString("machine", null);
            if (guard != null && machine != null) {
                request.getBody().setAuthCode(guard);
                request.getBody().setMachineName(machine);
            }
            request.getBody().clearShaSentryfile();
            request.getBody().setEresultSentryfile(Language.Result.FILE_NOT_FOUND);
        }

        request.setSessionId(0);
        request.setSteamId(user.getLong());
        request.getBody().setObfustucatedPrivateIp(getIPAddress() ^ 0xBAADF00D);
        request.getBody().setProtocolVersion(65579); // current protocol
        request.getBody().setClientOsType(-203); // linux unknown
        request.getBody().setClientLanguage("english");
        request.getBody().setSteam2TicketRequest(false);
        request.getBody().setClientPackageVersion(1771);
        request.getBody().setMachineId(getDeviceId(service));
        service.getSteamConnection().send(request);
    }

    @SuppressWarnings("unused")
    @SteamEventBus.SteamEvent(event = Language.Message.CLIENT_LOG_ON_RESPONSE)
    public static void onClientLogOnResponse(SteamService service, final byte[] data) {
        final ProtoPacket<CMsgClientLogonResponse.Builder> response = new ProtoPacket<CMsgClientLogonResponse.Builder>
                (CMsgClientLogonResponse.class, Language.Message.CLIENT_LOG_ON_RESPONSE);

        try {
            response.setData(data);
            response.deserialize();
            if (response.getBody().getEresult() == Language.Result.OK) {
                final long steam_id = response.getHeader().getSteamid();
                final int session_id = response.getHeader().getClientSessionid();
                final int heartbeat_interval = response.getBody().getOutOfGameHeartbeatSeconds();
                service.getSteamConnection().setSteamId(steam_id);
                service.getSteamConnection().setSessionId(session_id);
                service.getSteamConnection().startHeartbeat(heartbeat_interval);

                if (!service.getSteamAccount().hasAccount()) {
                    service.getSteamAccount().setCredentials();
                    service.getSteamAccount().setExtra("account_flags", String.valueOf(response.getBody().getAccountFlags()));
                    service.getSteamAccount().setExtra("steam_id", String.valueOf(steam_id));
                } else {
                    return;
                }
            } else {
                service.getSteamConnection().close();
                SteamAccount.delete(service);
                SteamChat.debug("E-RESULT LOGIN: " + response.getBody().getEresult());
            }

            // Send a new intent to the authenticator activity with a result from the login. This will get
            // skipped if the user previously had a steam account.
            Intent intent = new Intent(service, AuthenticationActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("result", response.getBody().getEresult());
            service.startActivity(intent);
        }
        catch(final IOException e) {
            SteamChat.debug("onClientLogOnResponse", e.getMessage(), e);
        }
    }

    @SuppressWarnings("unused")
    @SteamEventBus.SteamEvent(event = Language.Message.CLIENT_LOGGED_OFF)
    public static void onClientLoggedOff(SteamService service, final byte[] data) {
        SteamChat.debug("onClientLoggedOff");
        service.getSteamConnection().close();
        SteamAccount.delete(service);
    }

    @SuppressWarnings("unused")
    @SteamEventBus.SteamEvent(event = Language.Message.CLIENT_UPDATE_MACHINE_AUTH)
    public static void onClientUpdateMachineAuth(SteamService service, final byte[] data) {
        ProtoPacket<CMsgClientUpdateMachineAuth.Builder> request =
                new ProtoPacket<CMsgClientUpdateMachineAuth.Builder>(CMsgClientUpdateMachineAuth.class);

        ProtoPacket<CMsgClientUpdateMachineAuthResponse.Builder> response =
                new ProtoPacket<CMsgClientUpdateMachineAuthResponse.Builder>
                        (CMsgClientUpdateMachineAuthResponse.class, Language.Message.CLIENT_UPDATE_MACHINE_AUTH_RESPONSE);

        try {
            request.setData(data);
            request.deserialize();

            final String filename = request.getBody().getFilename();
            final byte[] sentry = request.getBody().getBytes().toByteArray();
            final byte[] hash = Cryptography.SHAHash(sentry);

            service.getSteamAccount().setExtra("sentry", filename);
            setSentryFile(service, filename, sentry);

            response.setTargetJobId(request.getHeader().getJobidSource());

            response.getBody().setCubwrote(request.getBody().getCubtowrite());
            response.getBody().setEresult(Language.Result.OK);
            response.getBody().setFilename(filename);
            response.getBody().setFilesize(sentry.length);
            response.getBody().setGetlasterror(0);
            response.getBody().setOffset(request.getBody().getOffset());
            response.getBody().setShaFile(ByteString.copyFrom(hash));
            response.getBody().setOtpIdentifier(request.getBody().getOtpIdentifier());
            response.getBody().setOtpType(request.getBody().getOtpType());
            response.getBody().setOtpValue(0);

            service.getSteamConnection().send(response);
        }
        catch(final IOException e) {
            // Failed to update the auth machine.  Perhaps we should dispatch a notification to the user
            // that authenticating this machine failed.  Then they know why they have to log in and out the
            // whole time.
            SteamChat.debug("onClientUpdateMachineAuth", e.getMessage(), e);
        }
    }

    @SteamEventBus.SteamEvent(event = Language.Message.CLIENT_NEW_LOGIN_KEY)
    public static void onClientNewLoginKey(SteamService service, final byte[] data) {
        ProtoPacket<CMsgClientNewLoginKey.Builder> request =
                new ProtoPacket<CMsgClientNewLoginKey.Builder>(CMsgClientNewLoginKey.class);

        ProtoPacket<CMsgClientNewLoginKeyAccepted.Builder> response =
                new ProtoPacket<CMsgClientNewLoginKeyAccepted.Builder>(CMsgClientNewLoginKeyAccepted.class,
                        Language.Message.CLIENT_NEW_LOGIN_KEY_ACCEPTED);
        try {
            request.setData(data);
            request.deserialize();

            final String token = request.getBody().getLoginKey();
            response.setTargetJobId(request.getSourceJobId());
            response.getBody().setUniqueId(request.getBody().getUniqueId());
            service.getSteamConnection().send(response);
        }
        catch(final IOException e) {
            // Failed to accept a new login key for the user.  Next time the user the TCP connections tries to
            // connect it would most likely fail and request the user to use their username and password.
            SteamChat.debug("onClientNewLoginKey", e.getMessage(), e);
        }
    }

    @SteamEventBus.SteamEvent(event = Language.Message.CLIENT_ACCOUNT_INFO)
    public static void onClientAccountInfo(SteamService service, final byte[] data) throws IOException {
        ProtoPacket<CMsgClientAccountInfo.Builder> packet = new ProtoPacket<CMsgClientAccountInfo.Builder>(CMsgClientAccountInfo.class);
        ProtoPacket<CMsgClientChangeStatus.Builder> request = new ProtoPacket<CMsgClientChangeStatus.Builder>(CMsgClientChangeStatus.class, Language.Message.CLIENT_CHANGE_STATUS);

        packet.setData(data);
        packet.deserialize();

        request.getBody().setPlayerName(packet.getBody().getPersonaName());
        request.getBody().setPersonaState(Language.PersonaState.ONLINE);
        service.getSteamConnection().send(request);
    }

    /**
     * @return a steam suitable device id for this device.  The device id will only change once the
     * user does a factory restore on their phone.
     */
    private static ByteString getDeviceId(Context context) {
        final String android_id = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        return ByteString.copyFrom(android_id.getBytes());
    }

    /**
     * @return an integer representing the IP address for this device. This performs a network activity
     * so can not be called on the main thread.
     */
    private static int getIPAddress() {
        try {
            final ByteBuffer buff = ByteBuffer.wrap(InetAddress.getLocalHost().getAddress());
            return (int) (buff.getInt() & 0xFFFFFFFFL);
        }
        catch(UnknownHostException e) {
            SteamChat.debug(e.toString());
            return 0;
        }
    }

    /**
     * Once a user has given access for this device to steam guard, a sentry file is created containing
     * some random data to identify this machine. Thereon after this hash needs to be sent to steam
     * on every login to avoid steam guard from being nasty.
     *
     * @param context of the application.
     * @param filename of the sentry file.
     * @return the an SHA1 hash of the steam sentry file contents.
     */
    private static ByteString getSentryHash(Context context, String filename) {
        File file = context.getFileStreamPath(filename);
        byte[] data = new byte[(int) file.length()];
        try {
            BufferedInputStream stream = new BufferedInputStream(new FileInputStream(file));
            stream.read(data, 0, data.length);
            stream.close();
            return ByteString.copyFrom(Cryptography.SHAHash(data));
        }
        catch(final IOException e) {
            SteamChat.debug(e.toString());
            return null;
        }
    }

    /**
     * Save the sentry data received from a machine auth update request.  This allows the user to log in
     * and bypassing steam guard as the device will be white listed with the steam service.
     *
     * @param context of the application.
     * @param filename used for the sentry file.
     * @param data to be written to file.
     * @throws IOException is thrown if there are any errors while trying to create / write to file.
     */
    private static void setSentryFile(Context context, String filename, final byte[] data) throws IOException {
        FileOutputStream stream = context.openFileOutput(filename, Context.MODE_PRIVATE);
        stream.write(data);
        stream.close();
    }

    /**
     * Helpful little method to dump the contents of a byte array.  This was created mainly to see if the sentry
     * file contents and hash matched.
     *
     * @param name to be used for logging.
     * @param data to be dumped.
     */
    @SuppressWarnings("unused")
    private static void dumpBytes(String name, byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            sb.append(String.format("%02X ", b));
        }
        SteamChat.debug(name, sb.toString());
    }
}
