package com.kevelbreh.steamchat.steam.network.packet;

import com.kevelbreh.steamchat.steam.language.Message;
import com.kevelbreh.steamchat.steam.util.BinaryReader;

import java.io.IOException;

/**
 * ChannelEncryptResult implementation.
 */
public class ChannelEncryptResult extends Packet {

    private int messagetype;
    private long targetjobid;
    private long sourcejobid;
    private int result;

    /**
     * @return the result in the packet.
     */
    public int getResult() {
        return result;
    }

    @Override
    public ChannelEncryptResult withData(byte[] data) {
        setData(data);
        return this;
    }

    @Override
    public int getMessageType() {
        return Message.CHANNEL_ENCRYPT_RESULT;
    }

    @Override
    public void deserialize(byte[] data) throws IOException {
        BinaryReader stream = new BinaryReader(data);
        messagetype = stream.readInt();
        targetjobid = stream.readLong();
        sourcejobid = stream.readLong();
        result = stream.readInt();
    }

    @Override
    public String toString() {
        return new StringBuilder(super.toString())
                .append("[type=").append(messagetype)
                .append("] [targetjobid=").append(targetjobid)
                .append("] [sourcejobid=").append(sourcejobid)
                .append("] [result=").append(result)
                .append("] ").toString();
    }
}
