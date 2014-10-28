package com.kevelbreh.steamchat.steam.network.packet;

import com.kevelbreh.steamchat.steam.proto.SteamMessagesBaseProto.CMsgProtoBufHeader;
import com.kevelbreh.steamchat.steam.language.Message;
import com.kevelbreh.steamchat.steam.proto.SteamMessagesClientServerProto.CMsgClientHeartBeat;
import com.kevelbreh.steamchat.steam.util.BinaryWriter;

import java.io.IOException;

/**
 * Created by kevin on 2014/08/16.
 */
public class HeartBeat extends Packet {

    private CMsgProtoBufHeader.Builder header =
            CMsgProtoBufHeader.newBuilder();

    private CMsgClientHeartBeat.Builder body =
            CMsgClientHeartBeat.newBuilder();

    public CMsgProtoBufHeader.Builder getHeader() {
        return header;
    }

    public CMsgClientHeartBeat.Builder getBody() {
        return body;
    }

    @Override
    public int getMessageType() {
        return Message.CLIENT_HEARTBEAT;
    }

    @Override
    public byte[] serialize() throws IOException {
        final BinaryWriter stream = new BinaryWriter();

        // v correct.
        final byte[] header_data = header.build().toByteArray();
        stream.write(Message.buff(Message.CLIENT_HEARTBEAT));
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