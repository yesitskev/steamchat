package com.kevelbreh.steamchat.steam;

import com.kevelbreh.steamchat.steam.util.BitVector64;

/**
 * This represents a steam user.
 */
public class SteamID {

    /**
     * Steam user id regex pattern.
     */
    //public static final Pattern SteamIDRegex =
    //        Pattern.compile("STEAM_(?[0-5]):(?[0-1]):(?\\d+)",
    //                Pattern.CASE_INSENSITIVE);

    /**
     * Steam account instances.
     */
    public static final int INSTANCE_ALL        = 0;
    public static final int INSTANCE_DESKTOP    = 1;
    public static final int INSTANCE_CONSOLE    = 2;
    public static final int INSTANCE_WEB        = 4;

    /**
     * Steam account and instance masks.
     */
    public static final int MASK_ID             = 0xFFFFFFFF;
    public static final int MASK_INSTANCE       = 0x000FFFFF;

    /**
     * Steam chat instance flags.
     */
    public static final int CHAT_FLAG_CLAN      = MASK_INSTANCE + 1 >> 1;
    public static final int CHAT_FLAG_LOBBY     = MASK_INSTANCE + 1 >> 2;
    public static final int CHAT_FLAG_MMS_LOBBY = MASK_INSTANCE + 1 >> 3;

    /**
     * This steam users steam id.
     */
    private BitVector64 steamid;

    public SteamID() {
        this(0);
    }

    public SteamID(long id) {
        steamid = new BitVector64(id);
    }

    /*public SteamID(int id, int universe, int type) {
        this()
        setAccountID(id);
        setAccountUniverse(universe);
        setAccountType(type);
    }*/

    public SteamID(int id, int instance, int universe, int type) {
        this();
        setAccountID(id);
        setAccountInstance(instance);
        setAccountUniverse(universe);
        setAccountType(type);
    }

    /*public SteamID(String id) {
        this(id, Universe.PUBLIC);
    }

    public SteamID(String id, int universe) {

    }*/

    public long getLong() {
        return steamid.getValue();
    }

    /**
     * Gets the account id.
     * @return The account id.
     */
    public long getAccountID() {
        return steamid.getMask((short) 0, 0xFFFFFFFF);
    }

    /**
     * Sets the account id.
     * @param value	The account id.
     */
    public void setAccountID(long value) {
        steamid.setMask((short) 0, 0xFFFFFFFF, value);
    }

    /**
     * Gets the account instance.
     * @return The account instance.
     */
    public long getAccountInstance() {
        return steamid.getMask((short) 32, 0xFFFFF);
    }

    /**
     * Sets the account instance.
     * @param value	The account instance.
     */
    public void setAccountInstance(long value) {
        steamid.setMask((short) 32, 0xFFFFF, value);
    }

    /**
     * Gets the account type.
     * @return The account type.
     */
    public int getAccountType() {
        return (int) steamid.getMask((short) 52, 0xF);
    }

    /**
     * Sets the account type.
     * @param value	The account type.
     */
    public void setAccountType(int value) {
        steamid.setMask((short) 52, 0xF, value);
    }

    /**
     * Gets the account universe.
     * @return The account universe.
     */
    public int getAccountUniverse() {
        return (int) steamid.getMask((short) 56, 0xFF);
    }

    /**
     * Sets the account universe.
     * @param value	The account universe.
     */
    public void setAccountUniverse(int value) {
        steamid.setMask((short) 56, 0xFF, value);
    }
}
