package com.kevelbreh.steamchat.steam.network.packet;

import com.kevelbreh.steamchat.steam.language.Message;
import com.kevelbreh.steamchat.steam.util.BinaryReader;
import com.kevelbreh.steamchat.steam.proto.SteamMessagesBaseProto.CMsgProtoBufHeader;
import com.kevelbreh.steamchat.steam.proto.SteamMessagesClientServerProto.CMsgClientLogonResponse;

import java.io.IOException;


public class ClientLogOnResponse extends Packet {

    private CMsgProtoBufHeader.Builder header = CMsgProtoBufHeader.newBuilder();
    private CMsgClientLogonResponse.Builder body = CMsgClientLogonResponse.newBuilder();

    public CMsgProtoBufHeader.Builder getHeader() {
        return header;
    }

    public CMsgClientLogonResponse.Builder getBody() {
        return body;
    }

    @Override
    public ClientLogOnResponse withData(byte[] data) {
        setData(data);
        return this;
    }

    @Override
    public int getMessageType() {
        return Message.CLIENT_LOG_ON_RESPONSE;
    }

    @Override
    public void deserialize(byte[] data) throws IOException {
        BinaryReader stream = new BinaryReader(data);

        // Read packet type and header length.
        int messagetype = stream.readInt();
        int headerlength = stream.readInt();

        // Read proto header and merge into our builder.
        header = CMsgProtoBufHeader.newBuilder();
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
