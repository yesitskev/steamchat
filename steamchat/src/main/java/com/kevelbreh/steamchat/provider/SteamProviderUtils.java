package com.kevelbreh.steamchat.provider;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;


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
}
