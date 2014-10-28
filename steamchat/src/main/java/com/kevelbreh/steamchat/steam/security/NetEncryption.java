package com.kevelbreh.steamchat.steam.security;

/**
 * Created by kevin on 2014/08/07.
 */
final public class NetEncryption {

    byte[] sessionKey;

    public NetEncryption(byte[] sessionKey) {
        this.sessionKey = sessionKey;
    }

    public byte[] processIncoming(byte[] data) {
        return Cryptography.SymmetricDecrypt(data, sessionKey);
    }

    public byte[] processOutgoing(byte[] ms) {
        return Cryptography.SymmetricEncrypt(ms, sessionKey);
    }
}
