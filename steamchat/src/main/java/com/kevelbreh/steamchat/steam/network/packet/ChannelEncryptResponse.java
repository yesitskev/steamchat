package com.kevelbreh.steamchat.steam.network.packet;

import com.kevelbreh.steamchat.steam.language.Message;
import com.kevelbreh.steamchat.steam.util.BinaryWriter;

import java.io.IOException;

/**
 * ChannelEncryptResponse packet implementation.
 */
public class ChannelEncryptResponse extends Packet {

    private int messagetype;
    private long targetjobid;
    private long sourcejobid;
    private int protocolversion = 1;
    private int keysize = 128;
    private byte[] cryptedsessionkey;
    private byte[] keycrc;

    /**
     * @param key to be used for the encrypted session.
     */
    public void setEncryptedSessionKey(byte[] key) {
        cryptedsessionkey = key;
    }

    /**
     * @param key to be used for CRC.
     */
    public void setKeyCRC(byte[] key) {
        keycrc = key;
    }

    @Override
    public int getMessageType() {
        return Message.CHANNEL_ENCRYPT_RESPONSE;
    }

    @Override
    public byte[] serialize() throws IOException {
        BinaryWriter stream = new BinaryWriter();
        stream.write(getMessageType());
        stream.write(targetjobid);
        stream.write(sourcejobid);
        stream.write(protocolversion);
        stream.write(keysize);
        stream.write(cryptedsessionkey);
        stream.write(keycrc);
        stream.write(0);
        return stream.toByteArray();
    }

    @Override
    public String toString() {
        return new StringBuilder(super.toString())
                .append("[type=").append(messagetype)
                .append("] [targetjobid=").append(targetjobid)
                .append("] [sourcejobid=").append(sourcejobid)
                .append("] [protocolversion=").append(protocolversion)
                .append("] [keysize=").append(keysize)
                .append("] [cryptedsessionkey=").append(cryptedsessionkey)
                .append("] [keycrc=").append(keycrc)
                .append("] ").toString();
    }

}
