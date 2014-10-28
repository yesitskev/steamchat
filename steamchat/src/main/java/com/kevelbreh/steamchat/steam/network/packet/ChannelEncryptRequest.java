package com.kevelbreh.steamchat.steam.network.packet;

import com.kevelbreh.steamchat.steam.language.Message;
import com.kevelbreh.steamchat.steam.util.BinaryReader;

import java.io.IOException;

/**
 * ChannelEncryptRequest implementation.
 */
public class ChannelEncryptRequest extends Packet {

    private int messagetype;
    private long targetjobid;
    private long sourcejobid;
    private int protocol;
    private int universe;

    /**
     * @return the server protocol.
     */
    public int getProtocol() {
        return protocol;
    }

    /**
     * @return the server universe.
     */
    public int getUniverse() {
        return universe;
    }

    @Override
    public ChannelEncryptRequest withData(byte[] data) {
        setData(data);
        return this;
    }

    @Override
    public int getMessageType() {
        return Message.CHANNEL_ENCRYPT_REQUEST;
    }

    @Override
    public void deserialize(byte[] data) throws IOException {
        BinaryReader stream = new BinaryReader(data);
        messagetype = stream.readInt();
        targetjobid = stream.readLong();
        sourcejobid = stream.readLong();
        protocol = stream.readInt();
        universe = stream.readInt();
    }

    @Override
    public String toString() {
        return new StringBuilder(super.toString())
                .append("[type=").append(messagetype)
                .append("] [targetjobid=").append(targetjobid)
                .append("] [sourcejobid=").append(sourcejobid)
                .append("] [protocol=").append(protocol)
                .append("] [universe=").append(universe)
                .append("] ").toString();
    }
}
