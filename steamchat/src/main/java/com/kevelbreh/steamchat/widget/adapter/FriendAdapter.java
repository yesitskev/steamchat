package com.kevelbreh.steamchat.widget.adapter;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.kevelbreh.steamchat.R;
import com.kevelbreh.steamchat.SteamChat;
import com.kevelbreh.steamchat.provider.SteamProvider;
import com.kevelbreh.steamchat.provider.SteamProvider.User;
import com.kevelbreh.steamchat.provider.SteamProviderUtils;
import com.kevelbreh.steamchat.provider.SteamProviderUtils.UserPersona;
import com.kevelbreh.steamchat.steam.language.Language;
import com.kevelbreh.steamchat.util.Dump;

import org.apache.commons.lang.StringUtils;


/**
 *
 */
public class FriendAdapter extends CursorAdapter {

    /**
     * Used for inflating our layouts.
     */
    private LayoutInflater mLayoutInflater;

    /**
     * Projection for this cursor adapter
     */
    public static final String[] PROJECTION = new String[] {
            SteamProvider.User._ID,
            SteamProvider.User.STEAM_ID,
            SteamProvider.User.RELATIONSHIP,
            SteamProvider.User.SUB_NICKNAME_QUERY,
            SteamProvider.User.SUB_PERSONA_STATE_QUERY,
            SteamProvider.User.SUB_PLAYER_NAME_QUERY
    };

    /**
     * Public constructor for our adapter to load data for all the steam users.
     *
     * @param context of the application.
     */
    public FriendAdapter(Context context) {
        this(context, context.getContentResolver().query(
                User.CONTENT_URI,
                PROJECTION,
                null,
                null,
                null
        ));

        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * Private constructor to for super class inheritance. Tell the super class to register a
     * content observer to monitor our data changes.
     *
     * @param context of the application.
     * @param cursor for accessing our data from
     *                  {@link com.kevelbreh.steamchat.provider.SteamProvider}
     */
    private FriendAdapter(Context context, Cursor cursor) {
        super(context, cursor, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
    }

    /**
     * Makes a new view to hold the data pointed to by the cursor.
     *
     * @param context of the application.
     * @param cursor containing our steam users data.
     * @param parent view being a {@link android.widget.ListView}
     * @return view to be used for the steam user
     */
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return mLayoutInflater.inflate(R.layout.friends_list_item, parent, false);
    }

    /**
     * Bind an existing view to the data pointed to by the cursor.  All views will have an avatar
     * area and steam name. Additional interaction and information views will be added depending
     * on the {@link com.kevelbreh.steamchat.provider.SteamProvider.User#RELATIONSHIP} of the user.
     *
     * @param view to mutate.
     * @param context of the application.
     * @param cursor containing our steam users data.
     */
    public void bindView(View view, Context context, Cursor cursor) {
        final int relationship = cursor.getInt(cursor.getColumnIndex(User.RELATIONSHIP));
        final Uri user = getUserUri(cursor);
        //final String nickname = SteamProviderUtils.getNickname(context, User.NICKNAME.getUri(user));
        final UserPersona persona =
                SteamProviderUtils.getUserPersona(context, User.PERSONA.getUri(user));

        // Clear the contents of the content area on the view.  This will be filled up with contextual
        // information and actions by the specific relationship type handlers.
        ((FrameLayout) view.findViewById(android.R.id.content)).removeAllViews();
        final TextView mName = (TextView) view.findViewById(android.R.id.text1);
        final View mAvatar = view.findViewById(R.id.icon_wrapper);

        // Set the friends name.
        // Todo: Set the friends nickname depending on users preferences.
        mName.setText(persona.player_name);

        // If the user is in a game then set the users name to be the in game color and set the
        // avatar background to match.
        if (!StringUtils.isEmpty(persona.game_name)) {
            mName.setTextColor(view.getContext().getResources()
                    .getColor(R.color.steam_list_friend_gaming));
            mAvatar.setBackground(view.getContext().getResources()
                    .getDrawable(R.drawable.friendslist_avatar_background_playing));
        }
        else {
        // Set colors depending on the persona state.
            switch (persona.persona_state) {
                case Language.PersonaState.ONLINE:
                case Language.PersonaState.LOOKING_TO_PLAY:
                case Language.PersonaState.BUSY:
                case Language.PersonaState.AWAY:
                case Language.PersonaState.SNOOZE:
                    mName.setTextColor(view.getContext().getResources()
                            .getColor(R.color.steam_list_friend_online));
                    mAvatar.setBackground(view.getContext().getResources()
                            .getDrawable(R.drawable.friendslist_avatar_background_online));
                    break;
                case Language.PersonaState.OFFLINE:
                    mName.setTextColor(view.getContext().getResources()
                            .getColor(R.color.steam_list_friend_offline));
                    mAvatar.setBackground(view.getContext().getResources()
                            .getDrawable(R.drawable.friendslist_avatar_background_offline));
                    break;
            }
        }

        // Call the relationship type handlers.
        switch (relationship) {
            case Language.Friend.Relationship.FRIEND:
                bindViewAsFriend(view, context, cursor, persona);
                break;
            case Language.Friend.Relationship.BLOCKED:
                break;
            case Language.Friend.Relationship.IGNORED:
                break;
            case Language.Friend.Relationship.IGNORED_FRIEND:
                break;
            case Language.Friend.Relationship.REQUEST_RECIPIENT:
                break;
            case Language.Friend.Relationship.REQUEST_INITIATOR:
                break;
            case Language.Friend.Relationship.NONE:
                default:
        }
    }

    /**
     * Bind specific friend user data and actions.
     *
     * @param view to mutate.
     * @param context of the application.
     * @param cursor containing our steam users data.
     * @param persona of the user.
     */
    private void bindViewAsFriend(View view, Context context, Cursor cursor, UserPersona persona) {
        FrameLayout content = (FrameLayout) view.findViewById(android.R.id.content);

        // Inflate our friend content layout into the content area.
        View layout = mLayoutInflater.inflate(R.layout.friends_list_item_content_friend, content, true);
        TextView mStatus = (TextView) layout.findViewById(R.id.friend_status);

        // If the user is in a game then set the status to the game title and change the text color
        // to the in game color.
        if (!StringUtils.isEmpty(persona.game_name)) {
            mStatus.setText(persona.game_name);
            mStatus.setTextColor(view.getContext().getResources()
                    .getColor(R.color.steam_list_friend_gaming));
            return;
        }

        // Set colors depending on the persona state.
        switch(persona.persona_state) {
            case Language.PersonaState.ONLINE:
            case Language.PersonaState.LOOKING_TO_PLAY:
            case Language.PersonaState.BUSY:
            case Language.PersonaState.AWAY:
            case Language.PersonaState.SNOOZE:
                mStatus.setTextColor(view.getContext().getResources()
                        .getColor(R.color.steam_list_friend_online));
                break;
            case Language.PersonaState.OFFLINE:
                mStatus.setTextColor(view.getContext().getResources()
                        .getColor(R.color.steam_list_friend_offline));
                break;
        }

        // Set the status text depending on the persona state.
        switch(persona.persona_state) {
            case Language.PersonaState.ONLINE:
                mStatus.setText(R.string.friends_list_persona_online);
                break;
            case Language.PersonaState.LOOKING_TO_PLAY:
                mStatus.setText(R.string.friends_list_persona_looking_to_play);
                break;
            case Language.PersonaState.LOOKING_TO_TRADE:
                mStatus.setText(R.string.friends_list_persona_looking_to_trade);
                break;
            case Language.PersonaState.BUSY:
                mStatus.setText(R.string.friends_list_persona_busy);
                break;
            case Language.PersonaState.AWAY:
                mStatus.setText(R.string.friends_list_persona_away);
                break;
            case Language.PersonaState.SNOOZE:
                mStatus.setText(R.string.friends_list_persona_snooze);
                break;
            case Language.PersonaState.OFFLINE:
                mStatus.setText(R.string.friends_list_persona_offline);
                break;
        }
    }

    /**
     * Get a user uri from a cursor containing data from {@link com.kevelbreh.steamchat.provider.SteamProvider.User}
     *
     * @param cursor containing user data.
     * @return user pointing to the user.
     */
    public static Uri getUserUri(Cursor cursor) {
        return ContentUris.withAppendedId(SteamProvider.User.CONTENT_URI,
                cursor.getLong(cursor.getColumnIndex(SteamProvider.User._ID)));
    }
}
