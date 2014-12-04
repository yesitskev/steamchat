package com.kevelbreh.steamchat.provider.content;

import android.net.Uri;

import com.kevelbreh.steamchat.provider.SteamProvider;

import edu.mit.mobile.android.content.ContentItem;
import edu.mit.mobile.android.content.ProviderUtils;
import edu.mit.mobile.android.content.UriPath;
import edu.mit.mobile.android.content.column.BlobColumn;
import edu.mit.mobile.android.content.column.BooleanColumn;
import edu.mit.mobile.android.content.column.DBColumn;
import edu.mit.mobile.android.content.column.DBForeignKeyColumn;
import edu.mit.mobile.android.content.column.DatetimeColumn;
import edu.mit.mobile.android.content.column.IntegerColumn;
import edu.mit.mobile.android.content.column.TextColumn;

@UriPath(PersonaContentItem.PATH_OR_TABLE)
public class PersonaContentItem implements ContentItem {

    /**
     * The path of the content item on our provider as well as the table name to be used in the
     * SQLite layer.
     */
    public static final String PATH_OR_TABLE = "persona";

    /**
     * A {@link android.net.Uri} pointing towards the content of this user item.
     */
    public static final Uri CONTENT_URI = ProviderUtils.toContentUri(SteamProvider.AUTHORITY, PATH_OR_TABLE);

    @DBForeignKeyColumn(parent = UserContentItem.class, notnull = true)
    public static final String USER = "user_id";

    @DBColumn(type = BlobColumn.class)
    public static final String AVATAR_HASH = "avatar_hash";

    @DBColumn(type = IntegerColumn.class)
    public static final String CLAN_RANK = "clan_rank";

    @DBColumn(type = TextColumn.class)
    public static final String CLAN_TAG = "clan_tag";

    @DBColumn(type = DatetimeColumn.class)
    public static final String FACEBOOK_ID = "facebook_id";

    @DBColumn(type = DatetimeColumn.class)
    public static final String GAME_ID = "game_id";

    @DBColumn(type = TextColumn.class)
    public static final String GAME_NAME = "game_name";

    @DBColumn(type = IntegerColumn.class)
    public static final String GAME_PLAYED_APP_ID = "game_played_app_id";

    @DBColumn(type = IntegerColumn.class)
    public static final String GAME_SERVER_IP = "game_server_ip";

    @DBColumn(type = IntegerColumn.class)
    public static final String GAME_SERVER_PORT = "game_server_port";

    @DBColumn(type = IntegerColumn.class)
    public static final String LAST_LOG_OFF = "last_log_off";

    @DBColumn(type = IntegerColumn.class)
    public static final String LAST_LOG_ON = "last_log_on";

    @DBColumn(type = BooleanColumn.class)
    public static final String PERSONA_SET_BY_USER = "persona_set_by_user";

    @DBColumn(type = IntegerColumn.class)
    public static final String PERSONA_STATE = "persona_state";

    @DBColumn(type = IntegerColumn.class)
    public static final String PERSONA_STATE_FLAGS = "persona_state_flags";

    @DBColumn(type = TextColumn.class)
    public static final String PLAYER_NAME = "player_name";


}
