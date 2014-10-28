package com.kevelbreh.steamchat.widget.adapter;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.kevelbreh.steamchat.R;
import com.kevelbreh.steamchat.provider.SteamProvider;
import com.kevelbreh.steamchat.provider.SteamProvider.Interaction;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Responsible for binding the correct interaction view types to the relevant interaction data type
 * {@link com.kevelbreh.steamchat.provider.SteamProvider.Interaction}.
 */
public class ConversationAdapter extends CursorAdapter {

    /**
     * Used for inflating our layouts.
     */
    private LayoutInflater mLayoutInflater;

    /**
     * Public constructor for our adapter to load data for all the steam interactions found at the
     * the given URI.
     *
     * @param context of the application.
     * @param uri to the interactions (.../user/{id}/interactions)
     */
    public ConversationAdapter(Context context, Uri uri) {
        this(context, context.getContentResolver().query(
                uri,
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
    private ConversationAdapter(Context context, Cursor cursor) {
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
        return mLayoutInflater.inflate(R.layout.chat_simple_house, parent, false);
    }

    /**
     * Bind an existing view to the data pointed to by the cursor.
     *
     * @param view to mutate.
     * @param context of the application.
     * @param cursor containing our steam users data.
     */
    public void bindView(View view, Context context, Cursor cursor) {
        FrameLayout house = (FrameLayout) view;
        house.removeAllViews();

        View inner_view;
        final int type = cursor.getInt(cursor.getColumnIndex(Interaction.LOCAL_FLAG));
        if (type == Interaction.FLAG_INCOMING) {
            inner_view = mLayoutInflater.inflate(R.layout.chat_simple_incoming, null, false);
        }
        else {
            inner_view = mLayoutInflater.inflate(R.layout.chat_simple_outgoing, null, false);
        }

        final String message = cursor.getString(cursor.getColumnIndex(Interaction.MESSAGE));
        final TextView mMessage = (TextView) inner_view.findViewById(android.R.id.text1);
        mMessage.setText(message);

        final long timestamp = cursor.getLong(cursor.getColumnIndex(Interaction.CLIENT_TIMESTAMP));
        final TextView mTimestamp = (TextView) inner_view.findViewById(android.R.id.text2);
        mTimestamp.setText(new SimpleDateFormat("HH:mm").format(new Date(timestamp)));

        house.addView(inner_view);
    }
}
