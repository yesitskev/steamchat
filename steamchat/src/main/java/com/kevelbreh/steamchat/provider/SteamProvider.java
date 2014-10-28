/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kevelbreh.steamchat.provider;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.kevelbreh.steamchat.SteamChat;
import com.kevelbreh.steamchat.steam.language.Language;
import com.kevelbreh.steamchat.steam.proto.SteamMessagesClientServerProto.CMsgClientFriendsList;
import com.kevelbreh.steamchat.steam.proto.SteamMessagesClientServerProto.CMsgClientPersonaState;
import com.kevelbreh.steamchat.steam.proto.SteamMessagesClientServerProto.CMsgClientPlayerNicknameList;
import com.kevelbreh.steamchat.steam.proto.SteamMessagesClientServerProto.CMsgClientFriendMsgIncoming;

import java.io.UnsupportedEncodingException;
import java.util.List;

import edu.mit.mobile.android.content.ContentItem;
import edu.mit.mobile.android.content.ForeignKeyDBHelper;
import edu.mit.mobile.android.content.ForeignKeyManager;
import edu.mit.mobile.android.content.GenericDBHelper;
import edu.mit.mobile.android.content.ProviderUtils;
import edu.mit.mobile.android.content.SimpleContentProvider;
import edu.mit.mobile.android.content.UriPath;
import edu.mit.mobile.android.content.column.BlobColumn;
import edu.mit.mobile.android.content.column.BooleanColumn;
import edu.mit.mobile.android.content.column.DBColumn;
import edu.mit.mobile.android.content.column.DBForeignKeyColumn;
import edu.mit.mobile.android.content.column.DatetimeColumn;
import edu.mit.mobile.android.content.column.IntegerColumn;
import edu.mit.mobile.android.content.column.TextColumn;
import edu.mit.mobile.android.content.column.TimestampColumn;


/**
 * user/^/persona
 * user/^/interactions
 * user/^/nickname
 */
public class SteamProvider extends SimpleContentProvider {

    /**
     * Content provider authority.
     */
    private static final String AUTHORITY = "com.kevelbreh.steamchat.provider";

    /**
     * Steam Content Provider.
     *
     * Change Log:
     * 1. Initial
     * 2. Tweaks
     * 3. Split up the UserDetail and User into their own content items.
     * 4. Table name updates.
     * 5. -
     * 6. Added conversation table.
     * 7. Added interaction table.
     * 8. Altered interaction table, removed conversation table.
     * 9. Remove data for russ.
     * 10. Remove data
     * 11. Removed everything.
     * 14. user_id
     * 16: added interactions
     */
    public SteamProvider() {
        super(AUTHORITY, "steamchat.db", 16);

        // Create a relationship between users and nicknames from Nickname.USER -> User._ID
        final ForeignKeyDBHelper nickname = new ForeignKeyDBHelper(User.class, Nickname.class,
                Nickname.USER);

        // Create a relationship between users and persona' from Persona.USER -> User._ID
        final ForeignKeyDBHelper persona = new ForeignKeyDBHelper(User.class, Persona.class,
                Persona.USER);

        // Create a relationship between users and persona' from Interaction.USER -> User._ID
        final ForeignKeyDBHelper interaction = new ForeignKeyDBHelper(User.class, Interaction.class,
                Interaction.USER);

        addDirAndItemUri(new GenericDBHelper(User.class), User.PATH_OR_TABLE);
        addDirAndItemUri(interaction, Interaction.PATH_ALL_INTERACTIONS);
        addChildDirAndItemUri(nickname, User.PATH_OR_TABLE, Nickname.PATH_OR_TABLE);
        addChildDirAndItemUri(persona, User.PATH_OR_TABLE, Persona.PATH_OR_TABLE);
        addChildDirAndItemUri(interaction, User.PATH_OR_TABLE, Interaction.PATH_OR_TABLE);
    }

    /**
     * Had to override this to make it thread safe by applying the synchronized key word. For
     * parameters and description see {@link edu.mit.mobile.android.content.SimpleContentProvider}
     */
    @Override
    public synchronized Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return super.query(uri, projection, selection, selectionArgs, sortOrder);
    }

    /**
     * Had to override this to make it thread safe by applying the synchronized key word. For
     * parameters and description see {@link edu.mit.mobile.android.content.SimpleContentProvider}
     */
    @Override
    public synchronized int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return super.update(uri, values, selection, selectionArgs);
    }

    /**
     * Had to override this to make it thread safe by applying the synchronized key word. For
     * parameters and description see {@link edu.mit.mobile.android.content.SimpleContentProvider}
     */
    @Override
    public synchronized Uri insert(Uri uri, ContentValues values) {
        return super.insert(uri, values);
    }

    /**
     * The base user entity for Steam users.  This content item stores the users unique Steam ID
     * and their relationship status (whether it to be a friend or pending invites).
     */
    public static class User implements ContentItem {

        /**
         * The path of the content item on our provider as well as the table name to be used in the
         * SQLite layer.
         */
        public static final String PATH_OR_TABLE = "user";

        /**
         * A {@link android.net.Uri} pointing towards the content of this user item.
         */
        public static final Uri CONTENT_URI = ProviderUtils.toContentUri(AUTHORITY, PATH_OR_TABLE);

        /**
         * Connect back to the {@link com.kevelbreh.steamchat.provider.SteamProvider.Persona} of
         * this user.
         */
        public static final ForeignKeyManager PERSONA = new ForeignKeyManager(Persona.class);

        /**
         * Connect back to the {@link com.kevelbreh.steamchat.provider.SteamProvider.Nickname} of
         * this user.
         */
        public static final ForeignKeyManager NICKNAME = new ForeignKeyManager(Nickname.class);


        /**
         * Connect back to the {@link com.kevelbreh.steamchat.provider.SteamProvider.Interaction} of
         * this user.
         */
        public static final ForeignKeyManager INTERACTION = new ForeignKeyManager(Interaction.class);


        @DBColumn(type = DatetimeColumn.class, unique = true, onConflict = DBColumn.OnConflict.REPLACE, notnull = true)
        public static final String STEAM_ID = "steam_id";


        @DBColumn(type = IntegerColumn.class, notnull = true)
        public static final String RELATIONSHIP = "relationship";

        /**
         * This is not a database column!
         *
         * This is used to do a sub select on {@link com.kevelbreh.steamchat.provider.SteamProvider.Persona}
         * to return the persona state of the user.
         */
        public static final String SUB_PERSONA_STATE = "sub_persona_state";
        public static final String SUB_PERSONA_STATE_QUERY =
                "(SELECT p.persona_state FROM persona AS p WHERE p.user_id = _id) AS sub_persona_state";

        /**
         * This is not a database column!
         *
         * This is used to do a sub select on {@link com.kevelbreh.steamchat.provider.SteamProvider.Persona}
         * to return the player name of the user.
         */
        public static final String SUB_PLAYER_NAME = "sub_player_name";
        public static final String SUB_PLAYER_NAME_QUERY =
                "(SELECT p.player_name FROM persona AS p WHERE p.user_id = _id) AS sub_player_name";

        /**
         * This is not a database column!
         *
         * This is used to do a sub select on {@link com.kevelbreh.steamchat.provider.SteamProvider.Persona}
         * to return the assigned nickname of the user.
         */
        public static final String SUB_NICKNAME = "sub_nickname";
        public static final String SUB_NICKNAME_QUERY =
                "(SELECT n.nickname FROM nickname AS n WHERE n.user_id = _id) AS sub_nickname";

        /**
         * Add a list of {@link com.kevelbreh.steamchat.steam.proto.SteamMessagesClientServerProto.CMsgClientFriendsList}
         * to the {@link com.kevelbreh.steamchat.provider.SteamProvider.User} content provider. If the user
         * does not exist then add a new entry, else update the existing one.
         * @param context of the application.
         * @param friends list that needs to be added.
         */
        public static void addFriendsList(Context context, List<CMsgClientFriendsList.Friend> friends) {
            /*
             * Synchronized resolver should theoretically block any other use of the content resolver
             * while we the insert or update steam users.  This should hopefully stop any cursor loaders
             * from trying to update their adapters until all operations have been completed.
             */
            synchronized (context.getContentResolver()) {
                String selection = STEAM_ID + "=?";

                for (CMsgClientFriendsList.Friend friend : friends) {
                    ContentValues values = new ContentValues(2);
                    values.put(STEAM_ID, friend.getUlfriendid());
                    values.put(RELATIONSHIP, friend.getEfriendrelationship());

                    String[] args = new String[] { String.valueOf(friend.getUlfriendid()) };
                    if (!SteamProviderUtils.isExisting(context, CONTENT_URI, selection, args)) {
                        // This steam user does not yet exist so add a new entry.
                        context.getContentResolver().insert(CONTENT_URI, values);
                    } else {
                        // This team user already exists so update the entry.  We can overwrite the
                        // steam_id because it won't be changing.
                        context.getContentResolver().update(CONTENT_URI, values, selection, args);
                    }
                }
            }
        }
    }

    /**
     * A nickname for a {@link com.kevelbreh.steamchat.provider.SteamProvider.User} will be stored
     * here against a user.  There can only be one {@link com.kevelbreh.steamchat.provider.SteamProvider.Nickname}
     * per user. Not all users will have a nickname.
     */
    @UriPath(Nickname.PATH_OR_TABLE)
    public static class Nickname implements ContentItem {

        /**
         * The path of the content item on our provider as well as the table name to be used in the
         * SQLite layer.
         */
        public static final String PATH_OR_TABLE = "nickname";

        /**
         * A {@link android.net.Uri} pointing towards the content of this user item.
         */
        public static final Uri CONTENT_URI = ProviderUtils.toContentUri(AUTHORITY, PATH_OR_TABLE);

        /**
         * Foreign key relationship to {@link com.kevelbreh.steamchat.provider.SteamProvider.User}.
         * There is no functionality to ensure that this field is unique so we need to police this
         * ourselves.
         */
        @DBForeignKeyColumn(parent = User.class, notnull = true)
        public static final String USER = "user_id";

        /**
         * The given nickname for the {@link com.kevelbreh.steamchat.provider.SteamProvider.User}.
         */
        @DBColumn(type = TextColumn.class)
        public static final String NICKNAME = "nickname";

        /**
         * Add a list of {@link com.kevelbreh.steamchat.steam.proto.SteamMessagesClientServerProto.CMsgClientPlayerNicknameList}
         * to the {@link com.kevelbreh.steamchat.provider.SteamProvider.User} content provider. If there is no
         * nickname then we add one else we update it.
         * @param context of the application.
         * @param nicknames assigned to the steam user.
         *
         * Todo: Refactor this somehow so that we don't have to query for a user uri each time.
         * Todo: Update when if there is an already given nickname to the user.
         */
        public static void addNicknameList(Context context, List<CMsgClientPlayerNicknameList.PlayerNickname> nicknames) {
           /*
            * Synchronized resolver should theoretically block any other use of the content resolver
            * while we the insert or update steam users.  This should hopefully stop any cursor loaders
            * from trying to update their adapters until all operations have been completed.
            */
            synchronized (context.getContentResolver()) {
                for (CMsgClientPlayerNicknameList.PlayerNickname nickname : nicknames) {
                    ContentValues values = new ContentValues(1);
                    values.put(NICKNAME, nickname.getNickname());

                    final Uri userUri = SteamProviderUtils.getUserFromSteamId(context, nickname.getSteamid());

                    // Continue if we didn't manage to find the user id.
                    if (userUri == null) {
                        SteamChat.debug("Couldn't find user for " + nickname.getSteamid() + "(" + nickname.getNickname() +")");
                        continue;
                    }

                    final Uri nicknameUri = User.NICKNAME.getUri(userUri);
                    if (!SteamProviderUtils.isExisting(context, nicknameUri, null, null)) {
                        // There isn't a nickname for this user so add one.
                        context.getContentResolver().insert(nicknameUri, values);
                    } else {
                        // There is already an existing nickname so update the entry.  We can overwrite the
                        // steam_id because it won't be changing.
                        //context.getContentResolver().update(nicknameUri, values, null, null);
                    }
                }
            }
        }
    }

    /**
     * Persona about a {@link com.kevelbreh.steamchat.provider.SteamProvider.User}.  There can only
     * be one persona record per steam user.
     */
    @UriPath(Persona.PATH_OR_TABLE)
    public static class Persona implements ContentItem {

        /**
         * The path of the content item on our provider as well as the table name to be used in the
         * SQLite layer.
         */
        public static final String PATH_OR_TABLE = "persona";

        /**
         * A {@link android.net.Uri} pointing towards the content of this user item.
         */
        public static final Uri CONTENT_URI = ProviderUtils.toContentUri(AUTHORITY, PATH_OR_TABLE);

        /**
         * Foreign key relationship to {@link com.kevelbreh.steamchat.provider.SteamProvider.User}.
         * There is no functionality to ensure that this field is unique so we need to police this
         * ourselves.
         */
        @DBForeignKeyColumn(parent = User.class, notnull = true)
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

        /**
         * Add a list of {@link com.kevelbreh.steamchat.steam.proto.SteamMessagesClientServerProto.CMsgClientPersonaState.Friend}
         * to the {@link com.kevelbreh.steamchat.provider.SteamProvider} content provider. If there is no
         * persona found then we add one else we update it.
         * @param context of the application.
         * @param friends list containing the persona.
         *
         * Todo: Refactor this somehow so that we don't have to query for a user uri each time.
         * Todo: Update when if there is an already given persona to the user.
         */
        public static void addPersonaList(Context context, List<CMsgClientPersonaState.Friend> friends) {
            /*
             * Synchronized resolver should theoretically block any other use of the content resolver
             * while we the insert or update steam user persona'.  This should hopefully stop any cursor
             * loaders from trying to update their adapters until all operations have been completed.
             */
            synchronized (context.getContentResolver()) {
                for (CMsgClientPersonaState.Friend friend : friends) {
                    ContentValues values = new ContentValues(15);
                    values.put(AVATAR_HASH, friend.getAvatarHash().toByteArray()); // blob
                    values.put(CLAN_RANK, friend.getClanRank()); // int
                    values.put(CLAN_TAG, friend.getClanTag()); // str
                    values.put(FACEBOOK_ID, friend.getFacebookId()); // long
                    values.put(GAME_ID, friend.getGameid()); // long
                    values.put(GAME_NAME, friend.getGameName()); // str
                    values.put(GAME_PLAYED_APP_ID, friend.getGamePlayedAppId()); // int
                    values.put(GAME_SERVER_IP, friend.getGameServerIp());// int
                    values.put(GAME_SERVER_PORT, friend.getGameServerPort()); // int
                    values.put(LAST_LOG_OFF, friend.getLastLogoff()); // int
                    values.put(LAST_LOG_ON, friend.getLastLogon()); //int
                    values.put(PERSONA_SET_BY_USER, friend.getPersonaSetByUser()); // bool
                    values.put(PERSONA_STATE, friend.getPersonaState()); // int
                    values.put(PERSONA_STATE_FLAGS, friend.getPersonaStateFlags()); // int
                    values.put(PLAYER_NAME, friend.getPlayerName()); //str

                    final Uri userUri = SteamProviderUtils.getUserFromSteamId(context, friend.getFriendid());

                    // Continue if we didn't manage to find the user id.
                    if (userUri == null) {
                        SteamChat.debug("Couldn't find user for " + friend.getFriendid()
                                + "(" + friend.getPlayerName() +")");
                        continue;
                    }

                    final Uri personaUri = User.PERSONA.getUri(userUri);
                    if (!SteamProviderUtils.isExisting(context, personaUri, null, null)) {
                        // There isn't a persona for this user so add one.
                        context.getContentResolver().insert(personaUri, values);
                    } else {
                        // There is already an existing persona so update the entry.  We can overwrite the
                        // all the data because we have no idea what changed (and adding checks is tedious).
                        //context.getContentResolver().update(nicknameUri, values, null, null);
                    }
                }
            }
        }
    }

    /**
     * Represents user interactions between each other.
     */
    @UriPath(Interaction.PATH_OR_TABLE)
    public static class Interaction implements ContentItem {

        /**
         * The path of the content item on our provider as well as the table name to be used in the
         * SQLite layer.
         */
        public static final String PATH_OR_TABLE = "interaction";

        /**
         * A {@link android.net.Uri} pointing towards the content of this user item.
         */
        // public static final Uri CONTENT_URI = ProviderUtils.toContentUri(AUTHORITY, PATH_OR_TABLE);

        /**
         *
         */
        public static final String PATH_ALL_INTERACTIONS = User.PATH_OR_TABLE + "/"
                + ForeignKeyDBHelper.WILDCARD_PATH_SEGMENT + "/" + PATH_OR_TABLE;

        /**
         *
         */
        public static final Uri CONTENT_URI = ProviderUtils.toContentUri(AUTHORITY,
                PATH_ALL_INTERACTIONS);

        /**
         * Foreign key relationship to {@link com.kevelbreh.steamchat.provider.SteamProvider.User}.
         * There is no functionality to ensure that this field is unique so we need to police this
         * ourselves.
         */
        @DBForeignKeyColumn(parent = User.class, notnull = true)
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

        @DBColumn(type = IntegerColumn.class, defaultValueInt = 0)
        public static final String LOCAL_FLAG = "local_flag";

        public static int FLAG_INCOMING = 0;
        public static int FLAG_OUTGOING_CLIENT = 1;
        public static int FLAG_OUTGOING_ECHO = 2;

        /**
         *
         * @param context
         * @param entry
         */
        public static void addChatIncoming(Context context, CMsgClientFriendMsgIncoming.Builder entry) {
            final Uri userUri = SteamProviderUtils.getUserFromSteamId(context, entry.getSteamidFrom());
            SteamChat.debug(entry.getSteamidFrom() + ": " + entry.getMessage().toStringUtf8());

            // Continue if we didn't manage to find the user id.
            if (userUri == null) {
                SteamChat.debug("Couldn't find user for " + entry.getSteamidFrom());
                return;
            }

            // Try convert the message to a UTF-8 string.  If this fails then just convert it
            // to whatever string converts to. For more info check out the SteamKit2 port:
            // https://github.com/SteamRE/SteamKit/blob/master/SteamKit2/SteamKit2/Steam/Handlers/SteamFriends/Callbacks.cs
            String message = null;
            try {
                if (entry.getMessage() != null) {
                    message = new String(entry.getMessage().toByteArray(), "UTF-8");
                }
            }
            catch(UnsupportedEncodingException e) {
                message = new String(entry.getMessage().toByteArray());
                SteamChat.debug("addChatIncoming", "Failed to convert message", e);
            }

            ContentValues values = new ContentValues();
            values.put(TYPE, entry.getChatEntryType());
            values.put(MESSAGE, message);
            values.put(SERVER_TIMESTAMP, entry.getRtime32ServerTimestamp());
            values.put(LOCAL_FLAG, FLAG_INCOMING);

            /*
             * Synchronized resolver should theoretically block any other use of the content resolver
             * while we the insert or update steam user persona'.  This should hopefully stop any cursor
             * loaders from trying to update their adapters until all operations have been completed.
             */
            synchronized (context.getContentResolver()) {
                context.getContentResolver().insert(User.INTERACTION.getUri(userUri), values);
                context.getContentResolver().notifyChange(Interaction.CONTENT_URI, null);
            }
        }

        public static void addChatOutgoing(Context context, Uri user, String message) {
            ContentValues values = new ContentValues();
            values.put(TYPE, Language.Chat.Entry.CHAT_MESSAGE);
            values.put(MESSAGE, message);
            values.put(LOCAL_FLAG, FLAG_OUTGOING_CLIENT);

            /*
             * Synchronized resolver should theoretically block any other use of the content resolver
             * while we the insert or update steam user persona'.  This should hopefully stop any cursor
             * loaders from trying to update their adapters until all operations have been completed.
             */
            synchronized (context.getContentResolver()) {
                context.getContentResolver().insert(User.INTERACTION.getUri(user), values);
                context.getContentResolver().notifyChange(Interaction.CONTENT_URI, null);
            }
        }

        public static void addEchoChatOutgoing(Context context, CMsgClientFriendMsgIncoming.Builder entry) {
            final Uri userUri = SteamProviderUtils.getUserFromSteamId(context, entry.getSteamidFrom());

            // Continue if we didn't manage to find the user id.
            if (userUri == null) {
                SteamChat.debug("Couldn't find user for " + entry.getSteamidFrom());
                return;
            }

            // Try convert the message to a UTF-8 string.  If this fails then just convert it
            // to whatever string converts to. For more info check out the SteamKit2 port:
            // https://github.com/SteamRE/SteamKit/blob/master/SteamKit2/SteamKit2/Steam/Handlers/SteamFriends/Callbacks.cs
            String message = null;
            try {
                if (entry.getMessage() != null) {
                    message = new String(entry.getMessage().toByteArray(), "UTF-8");
                }
            }
            catch(UnsupportedEncodingException e) {
                message = new String(entry.getMessage().toByteArray());
                SteamChat.debug("addChatIncoming", "Failed to convert message", e);
            }

            ContentValues values = new ContentValues();
            values.put(TYPE, entry.getChatEntryType());
            values.put(MESSAGE, message);
            values.put(SERVER_TIMESTAMP, entry.getRtime32ServerTimestamp());
            values.put(LOCAL_FLAG, FLAG_OUTGOING_ECHO);

            /*
             * Synchronized resolver should theoretically block any other use of the content resolver
             * while we the insert or update steam user persona'.  This should hopefully stop any cursor
             * loaders from trying to update their adapters until all operations have been completed.
             */
            synchronized (context.getContentResolver()) {
                context.getContentResolver().insert(User.INTERACTION.getUri(userUri), values);
                context.getContentResolver().notifyChange(Interaction.CONTENT_URI, null);
            }
        }
    }
}
