package com.kevelbreh.steamchat.provider;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.kevelbreh.steamchat.SteamChat;
import com.kevelbreh.steamchat.provider.SteamProvider.Persona;

/**
 * {@link com.kevelbreh.steamchat.provider.SteamProvider} utilities class.
 */
public class SteamProviderUtils {

    /**
     * Determine whether or not a give URI has an item matching the specified selected and
     * arguments.
     *
     * @param context of the application.
     * @param uri of the content location.
     * @param selection of the data.
     * @param args of the selection.
     * @return whether the defined selection with args are found on the URI.
     */
    public static boolean isExisting(Context context, Uri uri, String selection, String[] args) {
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, null, selection, args, null);
            return cursor.getCount() > 0;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    /**
     * Get a {@link com.kevelbreh.steamchat.provider.SteamProvider.User} uri a given steam id. If
     * there is no user found then a null will be returned.
     *
     * @param context of the application.
     * @param steam_id of the user
     * @return the uri pointing to the user, or null.
     */
    public static Uri getUserFromSteamId(Context context, long steam_id) {
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(SteamProvider.User.CONTENT_URI,
                    null,
                    SteamProvider.User.STEAM_ID + "=?",
                    new String[] { String.valueOf(steam_id) },
                    null);
            if (cursor.getCount() != 0 && cursor.moveToFirst()) {
                final long id = cursor.getLong(cursor.getColumnIndex(SteamProvider.User._ID));
                return ContentUris.withAppendedId(SteamProvider.User.CONTENT_URI, id);
            }
            return null;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    /**
     * Get a {@link com.kevelbreh.steamchat.provider.SteamProviderUtils.UserPersona} object once off
     * per steam user containing a summary of data.
     *
     * @param context of the application.
     * @param uri of the user persona
     * @return the user persona
     */
    public static UserPersona getUserPersona(Context context, Uri uri) {
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor.getCount() == 1 && cursor.moveToFirst()) {
                UserPersona persona = new UserPersona();
                persona.avatar_hash = cursor.getBlob(cursor.getColumnIndex(Persona.AVATAR_HASH));
                persona.game_name = cursor.getString(cursor.getColumnIndex(Persona.GAME_NAME));
                persona.last_log_off = cursor.getLong(cursor.getColumnIndex(Persona.LAST_LOG_OFF));
                persona.persona_state = cursor.getInt(cursor.getColumnIndex(Persona.PERSONA_STATE));
                persona.persona_state_flags = cursor.getInt(cursor.getColumnIndex(Persona.PERSONA_STATE_FLAGS));
                persona.player_name = cursor.getString(cursor.getColumnIndex(Persona.PLAYER_NAME));
                return persona;
            }
            return new UserPersona();
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    /**
     * Get a {@link com.kevelbreh.steamchat.provider.SteamProvider.Nickname#NICKNAME} for a steam
     * user.
     *
     * @param context of the application.
     * @param uri to the nickname
     * @return the nickname.
     */
    public static String getNickname(Context context, Uri uri) {
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor.getCount() == 1 && cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndex(SteamProvider.Nickname.NICKNAME));
            }
            return null;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    /**
     *
     * @param context
     * @param uri
     * @return
     */
    public static int unreadMessageCount(Context context, Uri uri) {
        Cursor cursor = null;
        try {
            SteamChat.debug("KEVIN CHECK: " + uri.toString());
            cursor = context.getContentResolver().query(uri, null, "is_read = 0 AND (type = 4 OR type = 1)", null, null);
            return cursor.getCount();
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    /**
     * Simple {@link com.kevelbreh.steamchat.provider.SteamProvider.Persona} structure for a single
     * user.  This is the data we show to the user when they navigate to the
     *                                      {@link com.kevelbreh.steamchat.fragment.FriendsFragment}
     */
    public static class UserPersona {
        public byte[] avatar_hash;
        public String game_name;
        public long last_log_off;
        public int persona_state;
        public int persona_state_flags;
        public String player_name;
    }
}
