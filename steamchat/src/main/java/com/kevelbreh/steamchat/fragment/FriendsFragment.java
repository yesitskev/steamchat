package com.kevelbreh.steamchat.fragment;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.kevelbreh.steamchat.R;
import com.kevelbreh.steamchat.provider.SteamProvider;
import com.kevelbreh.steamchat.widget.adapter.FriendAdapter;


public class FriendsFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private FriendAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle instance) {
        return inflater.inflate(R.layout.friends_list, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setListAdapter(mAdapter = new FriendAdapter(getActivity()));
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        return new CursorLoader(getActivity(),
                SteamProvider.User.CONTENT_URI,
                mAdapter.PROJECTION,
                null,
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