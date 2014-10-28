package com.kevelbreh.steamchat.fragment;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import com.kevelbreh.steamchat.R;
import com.kevelbreh.steamchat.SteamChat;
import com.kevelbreh.steamchat.provider.SteamProvider;
import com.kevelbreh.steamchat.widget.adapter.ConversationAdapter;

/**
 *
 */
public class ConversationFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private ConversationAdapter mAdapter;
    private Uri mInteractionUri;
    private Uri mUserUri;

    public static ConversationFragment forUser(Uri uri) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("data", uri);
        ConversationFragment fragment = new ConversationFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserUri = getArguments().getParcelable("data");
        mInteractionUri = SteamProvider.User.INTERACTION.getUri(mUserUri);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle instance) {
        return inflater.inflate(R.layout.fragment_chat, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setListAdapter(mAdapter = new ConversationAdapter(getActivity(), mInteractionUri));
        getListView().setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        return new CursorLoader(getActivity(),
                mInteractionUri,
                null,
                "type = 4 OR type = 1",
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
