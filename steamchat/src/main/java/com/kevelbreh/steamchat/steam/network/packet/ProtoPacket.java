package com.kevelbreh.steamchat.steam.network.packet;

import com.google.protobuf.AbstractMessage;
import com.google.protobuf.GeneratedMessage;
import com.kevelbreh.steamchat.SteamChat;
import com.kevelbreh.steamchat.steam.language.Message;
import com.kevelbreh.steamchat.steam.proto.SteamMessagesBaseProto;
import com.kevelbreh.steamchat.steam.util.BinaryReader;
import com.kevelbreh.steamchat.steam.util.BinaryWriter;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ProtoPacket<T extends GeneratedMessage.Builder<T>> extends Packet {

    private SteamMessagesBaseProto.CMsgProtoBufHeader.Builder header = SteamMessagesBaseProto.CMsgProtoBufHeader.newBuilder();

    private T body;

    public SteamMessagesBaseProto.CMsgProtoBufHeader.Builder getHeader() {
        return header;
    }

    public T getBody() {
        return body;
    }

    @SuppressWarnings("unchecked")
    public ProtoPacket(Class<? extends AbstractMessage> klass) {

        try {
            final Method method = klass.getMethod("newBuilder");
            body = (T) method.invoke(null);
        }
        catch(final IllegalAccessException e) {
            SteamChat.debug(this, "Failed to create packet: " + e.toString());
        }
        catch(final NoSuchMethodException e) {
            SteamChat.debug(this, "Failed to create packet: " + e.toString());
        }
        catch(final SecurityException e) {
            SteamChat.debug(this, "Failed to create packet: " + e.toString());
        }
        catch(final IllegalArgumentException e) {
            SteamChat.debug(this, "Failed to create packet: " + e.toString());
        }
        catch(final InvocationTargetException e) {
            SteamChat.debug(this, "Failed to create packet: " + e.toString());
        }
    }

    @Override
    public ProtoPacket withData(byte[] data) {
        setData(data);
        return this;
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
    public byte[] serialize() throws IOException {
        final BinaryWriter stream = new BinaryWriter();

        // v correct.
        final byte[] header_data = header.build().toByteArray();
        SteamChat.debug(this, "Creating header for " + getMessageType() + " with length " + header_data.length);

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
