package com.kevelbreh.steamchat.widget.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Steam interactions list view item displaying a friends profile picture and other content depending
 * on the state of the last interaction.
 */
public class FriendInteractionsView extends RelativeLayout {

    public FriendInteractionsView(Context context) {
        super(context);
    }

    public FriendInteractionsView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
}
