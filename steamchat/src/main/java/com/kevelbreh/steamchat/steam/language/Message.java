package com.kevelbreh.steamchat.steam.language;

/**
 * Steam message object.  This houses the steam language with message types.
 */
public class Message {

    private static final int PROTO_MASK                 = 0x80000000;
    private static final int MESSAGE_MASK               = ~PROTO_MASK;

    /**
     * Message types.
     */
    public static final int INVALID                             = 0;
    public static final int MULTI                               = 1;
    public static final int JOB_HEARTBEAT                       = 123;
    public static final int CLIENT_HEARTBEAT                    = 703;
    public static final int CLIENT_LOG_ON_RESPONSE              = 751;
    public static final int CLIENT_LOGGED_OFF                   = 757;
    public static final int CLIENT_CM_LIST                      = 783;
    public static final int CLIENT_SERVER_LIST                  = 880;
    public static final int CHANNEL_ENCRYPT_REQUEST             = 1303;
    public static final int CHANNEL_ENCRYPT_RESPONSE            = 1304;
    public static final int CHANNEL_ENCRYPT_RESULT              = 1305;
    public static final int CLIENT_SERVERS_AVAILABLE            = 5501;
    public static final int CLIENT_REQUESTED_CLIENT_STATS       = 5480;
    public static final int CLIENT_LOG_ON                       = 5514;

    /**
     * Get a new Message object for a specific value masked by the inverse of the proto mask.
     * @param value of the raw message type.
     * @return a new {@link com.kevelbreh.steamchat.steam.language.Message} object.
     */
    public static int forType(int value) {
        return (value & MESSAGE_MASK);
    }

    /**
     * @return whether the message is proto buffed or not.
     */
    public static boolean isProtoBuffed(int value) {
        return (value & 0xffffffffL & PROTO_MASK) > 0;
    }

    /**
     * Protobuff the event type.
     * @param value the event type.
     * @return newely buffed type.
     */
    public static int buff(int value) {
        return value | PROTO_MASK;
    }
}
