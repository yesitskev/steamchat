package com.kevelbreh.steamchat.steam2.packet;

import com.kevelbreh.steamchat.steam.language.Message;
import com.kevelbreh.steamchat.steam.util.BinaryReader;

import java.io.IOException;

/**
 *
 */
public abstract class Packet {

    /**
     * Steam {@link com.kevelbreh.steamchat.steam.language.Message} which tells us what type of
     * packet it is.
     */
    private int message_type = Message.INVALID;

    /**
     * Complete packet data (including the header and body).
     */
    private byte[] data;

    /**
     * Whether this packet is or should be protobuf backed or not.  By default the packet will be
     * set as not being protobuf backed.
     */
    private boolean is_proto = false;

    /**
     * The steam id of the user. If not 0 then this will get added to the packet header.
     */
    private long steam_id;

    /**
     * The session id once a successful login has been performed.  Steam requires this to be added
     * to the header for all packets after logging in.
     */
    private int session_id;

    /**
     * The job identity number found on the Steam server.  Certain packets have this job and require
     * it to be sent back in the responding packet so that the server knows what job it is linked to
     * and how to react to it.
     */
    private long target_job_id = BinaryReader.LongMaxValue;

    /**
     * Used if we want to assign a job id to a packet. The response from the Steam server will have this
     * id attached in it's header.
     */
    private long source_job_id = BinaryReader.LongMaxValue;


    public Packet() {

    }

    public Packet(byte[] data) {
        this.data = data;
    }

    public long getSourceJobId() {
        return source_job_id;
    }

    public void setSourceJobId(long id) {
        source_job_id = id;
    }

    public long getTargetJobId() {
        return target_job_id;
    }

    public void setTargetJobId(long id) {
        target_job_id = id;
    }

    public long getSteamId() {
        return steam_id;
    }

    public void setSteamId(long id) {
        steam_id = id;
    }

    public int getSessionId() {
        return session_id;
    }

    public void setSessionId(int id) {
        session_id = id;
    }

    public int getMessageType() {
        return message_type;
    }

    public void setMessageType(int type) {
        message_type = type;
    }

    public boolean isProto() {
        return is_proto;
    }

    public void setProto(boolean proto) {
        is_proto = proto;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public abstract byte[] serialize() throws IOException;

    public abstract void deserialize() throws IOException;

}
