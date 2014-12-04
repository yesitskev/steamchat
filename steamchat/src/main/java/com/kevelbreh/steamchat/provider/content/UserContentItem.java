package com.kevelbreh.steamchat.provider.content;

import android.net.Uri;

import com.kevelbreh.steamchat.provider.SteamProvider;

import edu.mit.mobile.android.content.ContentItem;
import edu.mit.mobile.android.content.ForeignKeyManager;
import edu.mit.mobile.android.content.ProviderUtils;
import edu.mit.mobile.android.content.column.DBColumn;
import edu.mit.mobile.android.content.column.DatetimeColumn;
import edu.mit.mobile.android.content.column.IntegerColumn;

/**
 * The base user entity for Steam users.  This content item stores the users unique Steam ID
 * and their relationship status (whether it to be a friend or pending invites).
 */
public class UserContentItem implements ContentItem {

    /**
     * The path of the content item on our provider as well as the table name to be used in the
     * SQLite layer.
     */
    public static final String PATH_OR_TABLE = "user";

    /**
     * A {@link android.net.Uri} pointing towards the content of this user item.
     */
    public static final Uri CONTENT_URI = ProviderUtils.toContentUri(SteamProvider.AUTHORITY, PATH_OR_TABLE);

    /**
     * Connect back to the {@link com.kevelbreh.steamchat.provider.content.PersonaContentItem} of
     * this user.
     */
    public static final ForeignKeyManager PERSONA = new ForeignKeyManager(PersonaContentItem.class);

    /**
     * Connect back to the {@link com.kevelbreh.steamchat.provider.content.InteractionContentItem} of
     * this user.
     */
    public static final ForeignKeyManager INTERACTION = new ForeignKeyManager(InteractionContentItem.class);

    @DBColumn(type = DatetimeColumn.class, unique = true, onConflict = DBColumn.OnConflict.REPLACE, notnull = true)
    public static final String STEAM_ID = "steam_id";

    @DBColumn(type = IntegerColumn.class, notnull = true)
    public static final String RELATIONSHIP = "relationship";

}