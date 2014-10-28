package com.kevelbreh.steamchat.account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.Bundle;

import com.kevelbreh.steamchat.SteamChat;

public class SteamAccount {

    public static final String ACCOUNT_TYPE = "com.kevelbreh.steamchat";

    private Context mContext;

    private Account mAccount;

    private Bundle mData;

    public SteamAccount(Context context) {
        mContext = context;
        mAccount = getAccount(context);
    }

    public SteamAccount(Context context, Bundle data) {
        mContext = context;
        mAccount = getAccount(context);
        mData = data;
    }

    // To be removed.
    public void setData(Bundle data) {
        mData = data;
    }

    /**
     * Set the user's username of their steam account from the temp meta we've saved in the bundled
     * data object.
     */
    public void setCredentials() {
        AccountManager manager = AccountManager.get(mContext);
        mAccount = getAccount(mContext);

        if (mAccount == null && mData != null) {
            final String username = mData.getString("username", null);
            mAccount = new Account(username, ACCOUNT_TYPE);
            manager.addAccountExplicitly(mAccount, null, Bundle.EMPTY);
            manager.setUserData(mAccount, "username", username);
			SteamChat.debug(this, "Created new steam account.");
        }

        mData = null;
    }

    public Bundle getData() {
        return mData;
    }

    /**
     * @return whether the user has an account or not.
     */
    public boolean hasAccount() {
        return !(mAccount == null);
    }

    /**
     * @return whether the user has an account or not.
     */
    public boolean hasAccountIntent() {
        return !(mData == null);
    }

    /**
     * @return the user's steam username.
     */
    public String getUsername() {
        return (mAccount == null) ? null : AccountManager.get(mContext)
                .getUserData(mAccount, "username");
    }

    /**
     * @return the user's steam password.
     */
    public String getPassword() {
        return (mAccount == null) ? null : AccountManager.get(mContext)
                .getPassword(mAccount);
    }

    /**
     * Get some meta from the account
     * @param key meta key
     * @return meta value
     */
    public String getExtra(String key) {
        if (mAccount == null) return null;
        AccountManager manager = AccountManager.get(mContext);
        return manager.getUserData(mAccount, key);
    }

    /**
     * Set some extra data on the user's account.
     * @param key of the meta.
     * @param value of the meta
     */
    public void setExtra(String key, String value) {
        if (mAccount == null) return;
        AccountManager manager = AccountManager.get(mContext);
        manager.setUserData(mAccount, key, value);
    }

    /**
     * Get the user's Steam account. If one is not found then return null.
     * @param context of the application.
     * @return the steam account on this device.
     */
    public static Account getAccount(Context context) {
        AccountManager manager = AccountManager.get(context);
        Account[] accounts = manager.getAccountsByType(ACCOUNT_TYPE);
        return (accounts.length == 0) ? null : accounts[0];
    }

	/**
	 * Delete the user's {@link android.accounts.Account} for the steam application.
	 * @param context of the application.
	 */
	public static void delete(Context context) {
		AccountManager manager = AccountManager.get(context);
		Account account = getAccount(context);
		if (account != null) {
			manager.removeAccount(getAccount(context), null, null);
		}
	}
}
