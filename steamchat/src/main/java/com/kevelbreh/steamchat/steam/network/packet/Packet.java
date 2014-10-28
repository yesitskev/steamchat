package com.kevelbreh.steamchat.steam.network.packet;

import com.kevelbreh.steamchat.SteamChat;
import com.kevelbreh.steamchat.steam.language.Message;
import com.kevelbreh.steamchat.steam.security.NetEncryption;
import com.kevelbreh.steamchat.steam.util.BinaryWriter;

import java.io.IOException;
import java.nio.ByteBuffer;

public class Packet {

    protected int messageType = Message.INVALID;
    private byte[] data;
    private boolean buffed = false;

    public Packet() {

    }

    public Packet withData(byte[] data) {
        this.data = data;
        if (data == null) {
            messageType = Message.INVALID;
            return this;
        }

        final byte[] type = new byte[4];
        for (int i = 0; i < 4; i++) {
            type[3 - i] = data[i];
        }

        final ByteBuffer buffer = ByteBuffer.wrap(type);
        final int rawType = buffer.getInt();

        messageType = Message.forType(rawType);
        buffed = Message.isProtoBuffed(rawType);

        return this;
    }

    public byte[] getData() {
        return data;
    } //c7n5g

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int v) {
        messageType = v;
    }

    public byte[] serialize() throws IOException {
        throw new UnsupportedOperationException();
    }

    public void deserialize(byte[] data) throws IOException {
        throw new UnsupportedOperationException();
    }

    public void deserialize() throws IOException {
        deserialize(data);
    }

    @Override
    public String toString() {
        return new StringBuilder("Packet: ").toString();
    }

    /**
     * Write this packet to a stream.
     * @param stream to be written to.
     * @throws IOException
     */
    public void send(NetEncryption filter, BinaryWriter stream) throws IOException {
        byte[] data = serialize();
        if (filter != null) {
            data = filter.processOutgoing(data);
        }

        stream.write(data.length);
        stream.write(0x31305456);
        stream.write(data);
        stream.flush();

        SteamChat.debug("Sent data " + data.length + "bytes ");
    }

    public boolean isProto() {
        return buffed;
    }
}
