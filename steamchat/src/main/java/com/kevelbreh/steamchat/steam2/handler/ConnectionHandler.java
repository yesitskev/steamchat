package com.kevelbreh.steamchat.steam2.handler;

import com.kevelbreh.steamchat.SteamChat;
import com.kevelbreh.steamchat.steam.language.Language;
import com.kevelbreh.steamchat.steam.language.Message;
import com.kevelbreh.steamchat.steam.proto.SteamMessagesBaseProto.CMsgMulti;
import com.kevelbreh.steamchat.steam.security.Cryptography;
import com.kevelbreh.steamchat.steam.security.PublicKey;
import com.kevelbreh.steamchat.steam.security.RSA;
import com.kevelbreh.steamchat.steam.util.BinaryReader;
import com.kevelbreh.steamchat.steam.util.BinaryWriter;
import com.kevelbreh.steamchat.steam2.SteamEventBus;
import com.kevelbreh.steamchat.steam2.SteamService;
import com.kevelbreh.steamchat.steam2.packet.Packet;
import com.kevelbreh.steamchat.steam2.packet.ProtoPacket;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.GZIPInputStream;

public class ConnectionHandler {

    // This is a dirty hack to set the session_key only after the channel result has been returned.
    // I should really use a much safer and better approach to doing this.
    public static byte[] sesh;

    @SuppressWarnings(value = "unused")
    @SteamEventBus.SteamEvent(event = Language.Message.CHANNEL_ENCRYPT_REQUEST, isProto = false)
    public static void onChannelEncryptRequest(final SteamService service, final byte[] data) {
        ChannelEncryptRequestPacket request = new ChannelEncryptRequestPacket(data);
        ChannelEncryptResponsePacket response = new ChannelEncryptResponsePacket();

        try {
            request.deserialize();

            // set connection universe.
            // set connection session key for encryption.

            // Create encrypted session keys.
            final byte[] public_key = PublicKey.UNIVERSE_PUBLIC;
            final byte[] session_key = Cryptography.GenerateRandomBlock(32);
            final RSA rsa = new RSA(public_key);
            final byte[] encrypted_session_key = rsa.encrypt(session_key);
            final byte[] key_crc = Cryptography.CRCHash(encrypted_session_key);

            // Create a response packet with the encrypted session credentials and then send it to
            // the steam server.
            response.setMessageType(Language.Message.CHANNEL_ENCRYPT_RESPONSE);
            response.encrypted_session_key = encrypted_session_key;
            response.key_crc = key_crc;
            service.getSteamConnection().send(response);

            sesh = session_key;
        }
        catch(final IOException e) {
            SteamChat.debug("onChannelEncryptRequest", e.getMessage(), e);
        }
    }

    @SuppressWarnings(value = "unused")
    @SteamEventBus.SteamEvent(event = Language.Message.CHANNEL_ENCRYPT_RESULT, isProto = false)
    public static void onChannelEncryptResult(final SteamService service, final byte[] data) {
        ChannelEncryptResultPacket result = new ChannelEncryptResultPacket(data);
        try {
            result.deserialize();

            // The channel encryption session was a success so set the channel session key for encrypting
            // and decrypting all future packets.  Also fire off an client event signaling that the channel
            // is ready for us to use.
            if (result.result == Language.Result.OK) {
                service.getSteamConnection().setSessionKey(sesh);
                service.getSteamEventBus().handleUserEvent(SteamService.EVENT_STEAM_CHANNEL_READY, null);
            }
        }
        catch(final IOException e) {
            SteamChat.debug("onChannelEncryptResult", e.getMessage(), e);
        }
    }

    @SuppressWarnings(value = "unused")
    @SteamEventBus.SteamEvent(event = Language.Message.MULTI)
    public static void onMulti(final SteamService service, byte[] data) {
        ProtoPacket<CMsgMulti.Builder> received = new ProtoPacket<CMsgMulti.Builder>
                (CMsgMulti.class, Language.Message.MULTI);

        try {
            received.setData(data);
            received.deserialize();
            byte[] payload = received.getBody().getMessageBody().toByteArray();

            if (received.getBody().getSizeUnzipped() > 0) {
                // Unzip the data received from Steam.
                try {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    GZIPInputStream stream = new GZIPInputStream(new ByteArrayInputStream(payload));
                    byte[] buffer = new byte[1024];
                    int read;

                    while ((read = stream.read(buffer)) > 0) {
                        out.write(buffer, 0, read);
                    }

                    payload = out.toByteArray();
                    out.close();
                    stream.close();
                }
                catch(final IOException e) {
                    SteamChat.debug("onMulti", e.getMessage(), e);
                }
            }

            // Read there the byte stream until it is finished. Read an integer value for the length
            // of the zipped packet, then read that amount as our packet data. Continue until the entire
            // contents of the stream has been read.
            final BinaryReader reader = new BinaryReader(payload);
            while (!reader.isAtEnd()) {
                final int subsize = reader.readInt();
                final byte[] subdata = reader.readBytes(subsize);

                // Get the message type (EMsg) of the sub data.
                final byte[] type = new byte[4];
                for (int i = 0; i < 4; i++) {
                    type[3 - i] = subdata[i];
                }

                final ByteBuffer temp = ByteBuffer.wrap(type);
                final int rawType = temp.getInt();

                // Handle new steam event.
                service.getSteamEventBus().handleSteamEvent(Message.forType(rawType),
                        Message.isProtoBuffed(rawType), subdata);
            }
        }
        catch(final IOException e) {
            // An error occurred while trying to unzip the data.  Not sure if feedback should be given
            // or not.  This is general stuff. Can take a look at it later. Haven't experienced any
            // issues with it in the past.
            SteamChat.debug("onMulti", e.getMessage(), e);
        }
    }

    /**
     * ChannelEncryptRequest packet received from Steam once a socket connection has first been
     * established.  This message is not proto backed. There is also no need to implement anything
     * for serializing as this packet does not get sent back to the server.
     */
    private static class ChannelEncryptRequestPacket extends Packet {

        private int protocol;
        private int universe;

        public ChannelEncryptRequestPacket(byte[] data) {
            super(data);
        }

        @Override
        public void deserialize() throws IOException {
            BinaryReader stream = new BinaryReader(getData());
            setMessageType(stream.readInt());
            setTargetJobId(stream.readLong());
            setSourceJobId(stream.readLong());
            protocol = stream.readInt();
            universe = stream.readInt();
        }

        @Override
        public byte[] serialize() throws IOException {
            return null;
        }
    }

    /**
     * ChannelEncryptResult packet is used to respond to the
     * {@link com.kevelbreh.steamchat.steam2.handler.ConnectionHandler.ChannelEncryptRequestPacket}.
     * There is no need to deserialize anything.
     */
    private static class ChannelEncryptResponsePacket extends Packet {

        private byte[] encrypted_session_key;
        private byte[] key_crc;

        @Override
        public void deserialize() throws IOException {}

        @Override
        public byte[] serialize() throws IOException {
            BinaryWriter stream = new BinaryWriter();
            stream.write(getMessageType());
            stream.write(getTargetJobId());
            stream.write(getSourceJobId());
            stream.write(1); // Protocol version.
            stream.write(128); // Key size.
            stream.write(encrypted_session_key);
            stream.write(key_crc);
            stream.write(0);
            return stream.toByteArray();
        }
    }

    /**
     * ChannelEncryptResult is received with the result of the
     * {@link com.kevelbreh.steamchat.steam2.handler.ConnectionHandler.ChannelEncryptResponsePacket}.
     */
    private static class ChannelEncryptResultPacket extends Packet {

        private int result;

        public ChannelEncryptResultPacket(byte[] data) {
            super(data);
        }

        @Override
        public void deserialize() throws IOException {
            BinaryReader stream = new BinaryReader(getData());
            setMessageType(stream.readInt());
            setTargetJobId(stream.readLong());
            setSourceJobId(stream.readLong());
            result = stream.readInt();
        }

        @Override
        public byte[] serialize() throws IOException {
            return null;
        }
    }
}
