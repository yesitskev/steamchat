package com.kevelbreh.steamchat.provider.content;

import android.net.Uri;

import com.kevelbreh.steamchat.provider.SteamProvider;

import edu.mit.mobile.android.content.ContentItem;
import edu.mit.mobile.android.content.ForeignKeyDBHelper;
import edu.mit.mobile.android.content.ProviderUtils;
import edu.mit.mobile.android.content.UriPath;
import edu.mit.mobile.android.content.column.BooleanColumn;
import edu.mit.mobile.android.content.column.DBColumn;
import edu.mit.mobile.android.content.column.DBForeignKeyColumn;
import edu.mit.mobile.android.content.column.IntegerColumn;
import edu.mit.mobile.android.content.column.TextColumn;
import edu.mit.mobile.android.content.column.TimestampColumn;

/**
 * Represents user interactions between the current user and his or her friend.
 */
@UriPath(InteractionContentItem.PATH_OR_TABLE)
public class InteractionContentItem implements ContentItem {

    public static final int DIRECTION_INCOMING = 0;
    public static final int DIRECTION_OUTGOING_CLIENT = 1;
    public static final int DIRECTION_OUTGOING_ECHO = 2;

    public static final String PATH_OR_TABLE = "interaction";

    public static final String PATH_ALL_INTERACTIONS = UserContentItem.PATH_OR_TABLE + "/"
            + ForeignKeyDBHelper.WILDCARD_PATH_SEGMENT + "/" + PATH_OR_TABLE;

    public static final Uri CONTENT_URI = ProviderUtils.toContentUri(SteamProvider.AUTHORITY,
            PATH_ALL_INTERACTIONS);

    @DBForeignKeyColumn(parent = UserContentItem.class, notnull = true)
    public static final String USER = "user_id";

    @DBColumn(type = IntegerColumn.class)
    public static final String TYPE = "type";

    @DBColumn(type = TextColumn.class)
    public static final String MESSAGE = "message";

    @DBColumn(type = TimestampColumn.class)
    public static final String SERVER_TIMESTAMP = "server_timestamp";

    @DBColumn(type = TimestampColumn.class, defaultValue = TimestampColumn.CURRENT_TIMESTAMP)
    public static final String CLIENT_TIMESTAMP = "client_timestamp";

    @DBColumn(type = BooleanColumn.class, defaultValueInt = 0)
    public static final String IS_READ = "is_read";

    @DBColumn(type = BooleanColumn.class, defaultValueInt = 0)
    public static final String IS_SENT = "is_sent";

    @DBColumn(type = IntegerColumn.class, defaultValueInt = DIRECTION_INCOMING)
    public static final String CHAT_DIRECTION = "chat_direction";
}
