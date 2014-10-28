package com.kevelbreh.steamchat.fragment;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.kevelbreh.steamchat.R;
import com.kevelbreh.steamchat.activity.ConversationActivity;
import com.kevelbreh.steamchat.activity.FriendsActivity;
import com.kevelbreh.steamchat.activity.SettingsActivity;
import com.kevelbreh.steamchat.provider.SteamProvider;
import com.kevelbreh.steamchat.widget.adapter.ChatAdapter;
import com.kevelbreh.steamchat.widget.adapter.FriendAdapter;

/**
 * Chats fragment displays a list of all the chats in progress with steam friends.
 */
public class ChatsFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private ChatAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setListAdapter(mAdapter = new ChatAdapter(getActivity()));
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.chats_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_friends:
            case R.id.action_chat:
                startActivity(new Intent(getActivity(), FriendsActivity.class));
                return true;
            case R.id.action_settings:
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            case R.id.action_status:
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        return new CursorLoader(getActivity(),
                SteamProvider.Interaction.CONTENT_URI,
                null,
                "type = 1 OR type = 4 GROUP BY user_id",
                null,
                "client_timestamp DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        // todo: v-- Remove this and it without this hac --v
        Cursor cursor = getActivity().getContentResolver().query(
                ContentUris.withAppendedId(SteamProvider.Interaction.CONTENT_URI, id),
                null,
                null,
                null,
                null
        );
        cursor.moveToFirst();
        final int user_id = cursor.getInt(cursor.getColumnIndex(SteamProvider.Interaction.USER));
        final Uri user_uri = ContentUris.withAppendedId(SteamProvider.User.CONTENT_URI, user_id);
        // todo: ^-- Remove this and it without this hack --^

        final Intent intent = new Intent(getActivity(), ConversationActivity.class);
        intent.setData(user_uri);
        intent.setAction(Intent.ACTION_VIEW);
        startActivity(intent);
    }
}
