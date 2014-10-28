package com.kevelbreh.steamchat.steam.network.packet;

import com.kevelbreh.steamchat.steam.language.Message;
import com.kevelbreh.steamchat.steam.util.BinaryWriter;
import com.kevelbreh.steamchat.steam.proto.SteamMessagesBaseProto;
import com.kevelbreh.steamchat.steam.proto.SteamMessagesClientServerProto;

import java.io.IOException;

/**
 * Created by kevin on 2014/08/13.
 */
public class ClientLogOn extends Packet {

    private SteamMessagesBaseProto.CMsgProtoBufHeader.Builder header =
            SteamMessagesBaseProto.CMsgProtoBufHeader.newBuilder();
    private SteamMessagesClientServerProto.CMsgClientLogon.Builder body =
            SteamMessagesClientServerProto.CMsgClientLogon.newBuilder();

    public SteamMessagesBaseProto.CMsgProtoBufHeader.Builder getHeader() {
        return header;
    }

    public SteamMessagesClientServerProto.CMsgClientLogon.Builder getBody() {
        return body;
    }

    @Override
    public int getMessageType() {
        return Message.CLIENT_LOG_ON;
    }

    @Override
    public byte[] serialize() throws IOException {
        final BinaryWriter stream = new BinaryWriter();

        // v correct.
        final byte[] header_data = header.build().toByteArray();
        stream.write(Message.buff(getMessageType()));
        stream.write(header_data.length);
        stream.write(header_data);
        // ^ correct.

        stream.write(body.build().toByteArray());
        return stream.toByteArray();
    }

    @Override
    public String toString() {
        return new StringBuilder(super.toString())
                .append("[type=").append(getMessageType())
                .append("] ").toString();
    }
}
