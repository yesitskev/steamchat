package com.kevelbreh.steamchat.widget.adapter;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
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
import com.kevelbreh.steamchat.provider.SteamProviderUtils;
import com.kevelbreh.steamchat.steam.language.Language;

import org.apache.commons.lang.StringUtils;

/**
 *
 */
public class ChatAdapter extends CursorAdapter {

    /**
     * Used for inflating our layouts.
     */
    private LayoutInflater mLayoutInflater;

    /**
     * Public constructor for our adapter to load data for all the steam interactions.
     *
     * @param context of the application.
     */
    public ChatAdapter(Context context) {
        this(context, context.getContentResolver().query(
                SteamProvider.Interaction.CONTENT_URI,
                null,
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
    private ChatAdapter(Context context, Cursor cursor) {
        super(context, cursor, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
    }

    /**
     * Makes a new view to hold the data pointed to by the cursor.
     *
     * @param context of the application.
     * @param cursor containing our the latest interaction data per user.
     * @param parent view being a {@link android.widget.ListView}
     * @return view to be used for the chat list item.
     */
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return mLayoutInflater.inflate(R.layout.conversation_list_item, parent, false);
    }

    /**
     * Bind an existing view to the data pointed to by the cursor.
     *
     * @param view to mutate.
     * @param context of the application.
     * @param cursor containing our steam users data.
     */
    public void bindView(View view, Context context, Cursor cursor) {
        final Uri user = getUserUri(cursor);
        //final String nickname = SteamProviderUtils.getNickname(context, User.NICKNAME.getUri(user));
        final SteamProviderUtils.UserPersona persona =
                SteamProviderUtils.getUserPersona(context, SteamProvider.User.PERSONA.getUri(user));

        final TextView mName = (TextView) view.findViewById(android.R.id.text1);
        final TextView mMessage = (TextView) view.findViewById(android.R.id.text2);
        final TextView mTimestamp = (TextView) view.findViewById(R.id.timestamp);
        final TextView mUnreadCounter = (TextView) view.findViewById(R.id.unread);
        final View mAvatar = view.findViewById(R.id.icon_wrapper);

        // Set the friends name.
        // Todo: Set the friends nickname depending on users preferences.
        mName.setText(persona.player_name);

        // Get the message and set it.  This could be a normal text message or an emote.
        // Todo: handle emotes.
        final String message = cursor.getString(cursor.getColumnIndex(SteamProvider.Interaction.MESSAGE));
        mMessage.setText(message);

        // If read then display a simple message. If it hasn't been read then show the message as bold
        // and also get an unread count to display.
        final boolean is_read = (cursor.getInt(cursor.getColumnIndex(SteamProvider.Interaction.IS_READ)) == 1);
        if (!is_read) {
            final int unread = SteamProviderUtils.unreadMessageCount(context, SteamProvider.User.INTERACTION.getUri(user));
            mUnreadCounter.setText(String.valueOf(unread));
            mUnreadCounter.setVisibility(View.VISIBLE);
            mMessage.setTypeface(null, Typeface.BOLD);
        }

        // If the user is in a game then set the users name to be the in game color and set the
        // avatar background to match.
        if (!StringUtils.isEmpty(persona.game_name)) {
            mName.setTextColor(view.getContext().getResources()
                    .getColor(R.color.steam_list_friend_gaming));
            mUnreadCounter.setBackground(view.getContext().getResources()
                    .getDrawable(R.drawable.chat_unread_count_gaming));
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
                    mUnreadCounter.setBackground(view.getContext().getResources()
                            .getDrawable(R.drawable.chat_unread_count_normal));
                    break;
                case Language.PersonaState.OFFLINE:
                    mName.setTextColor(view.getContext().getResources()
                            .getColor(R.color.steam_list_friend_offline));
                    mAvatar.setBackground(view.getContext().getResources()
                            .getDrawable(R.drawable.friendslist_avatar_background_offline));
                    mUnreadCounter.setBackground(view.getContext().getResources()
                            .getDrawable(R.drawable.chat_unread_count_offline));
                    break;
            }
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
                cursor.getLong(cursor.getColumnIndex(SteamProvider.Interaction.USER)));
    }
}
