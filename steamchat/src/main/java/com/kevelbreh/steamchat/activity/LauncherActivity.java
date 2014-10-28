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

package com.kevelbreh.steamchat.activity;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;

import com.kevelbreh.steamchat.account.SteamAccount;
import com.kevelbreh.steamchat.steam.SteamServiceHandler;
import com.kevelbreh.steamchat.steam2.SteamService;
import com.kevelbreh.steamchat.util.AServiceActivity;

/**
 * Launcher activity is responsible for making sure that the user is logged in and authenticated
 * to the Steam network.  If the user is not authenticated, the user will be directed to
 * {@link com.kevelbreh.steamchat.activity.AuthenticationActivity} else the user will be able to
 * go to the conversations activity.
 *
 * Arrive here.
 *      -> No account -> AuthenticationActivity -> Login -> Create account -> finish();
 *      -> has account -> ConversationsActivity.
 *
 *      if  account login failed, delete local account, force user to log in again.
 */
public class LauncherActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		startService(new Intent(this, SteamService.class));
	}

    @Override
    public void onResume() {
		super.onResume();

        /*
         * See if there is an existing account for the user for this application.  If there is no
         * account then we have to force them to the login / register screen to create an account
         * to use the application.
         */
		SteamAccount account = new SteamAccount(this);
		if (account.hasAccount()) {
			startActivity(new Intent(this, ChatsActivity.class));
			finish();
		} else {
			startActivity(new Intent(this, AuthenticationActivity.class));
			finish();
		}
	}
}
