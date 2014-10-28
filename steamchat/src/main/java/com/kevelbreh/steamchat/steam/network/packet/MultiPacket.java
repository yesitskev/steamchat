package com.kevelbreh.steamchat.steam.network.packet;

import com.kevelbreh.steamchat.steam.language.Message;
import com.kevelbreh.steamchat.steam.util.BinaryReader;
import com.kevelbreh.steamchat.steam.proto.SteamMessagesBaseProto;

import java.io.IOException;

/**
 * Created by kevin on 2014/08/13.
 */
public class MultiPacket extends Packet {

    private SteamMessagesBaseProto.CMsgProtoBufHeader.Builder header =
            SteamMessagesBaseProto.CMsgProtoBufHeader.newBuilder();

    private SteamMessagesBaseProto.CMsgMulti.Builder body =
            SteamMessagesBaseProto.CMsgMulti.newBuilder();

    public SteamMessagesBaseProto.CMsgProtoBufHeader.Builder getHeader() {
        return header;
    }

    public SteamMessagesBaseProto.CMsgMulti.Builder getBody() {
        return body;
    }

    @Override
    public MultiPacket withData(byte[] data) {
        setData(data);
        return this;
    }

    @Override
    public int getMessageType() {
        return Message.MULTI;
    }

    @Override
    public void deserialize(byte[] data) throws IOException {
        BinaryReader stream = new BinaryReader(data);

        // Read packet type and header length.
        int messagetype = stream.readInt();
        int headerlength = stream.readInt();

        // Read proto header and merge into our builder.
        header = SteamMessagesBaseProto.CMsgProtoBufHeader.newBuilder();
        final byte[] temp = stream.readBytes(headerlength);
        header.mergeFrom(temp);

        // Read the body of the packet.
        body.mergeFrom(stream.getStream());
    }

    @Override
    public String toString() {
        return new StringBuilder(super.toString())
                .append("[type=").append(getMessageType())
                .append("] ").toString();
    }
}
