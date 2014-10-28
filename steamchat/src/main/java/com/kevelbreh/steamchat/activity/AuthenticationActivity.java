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
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kevelbreh.steamchat.R;
import com.kevelbreh.steamchat.SteamChat;
import com.kevelbreh.steamchat.steam.language.Language;
import com.kevelbreh.steamchat.steam2.SteamService;

import org.apache.commons.lang.StringUtils;

/**
 *
 */
public class AuthenticationActivity extends Activity {

    private boolean guarded = false;

    /**
     * Service connection which connects to {@link com.kevelbreh.steamchat.steam.SteamService} for
     * us to send messages.  This can also be used for receiving the "typing..." signal.
     */
    private Messenger mService = null;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            mService = null;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);

        bindService(new Intent(this, SteamService.class), mConnection, Context.BIND_AUTO_CREATE);
        findViewById(R.id.authenticate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                authenticate();
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    /**
     *
     * @param intent
     */
    private void handleIntent(Intent intent) {
        switch (intent.getIntExtra("result", -500)) {

            case Language.Result.OK:
                finish();
                break;

            case Language.Result.ACCOUNT_LOGON_DENIED:
                setupGuardState();
                break;

            default:
                Toast.makeText(this, "Something went wrong.", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Authenticate the user.  Perform actions depending on the required form state and result code
     * of the Steam API.
     */
    private void authenticate() {
        if (isValid()) try {
            Bundle data = new Bundle();
            data.putString("username", getUsername());
            data.putString("password", getPassword());
            data.putString("guard", getGuard());
            data.putString("machine", getMachine());

            Message message = Message.obtain(null, SteamService.EVENT_STEAM_USER_LOGIN);
            message.setData(data);
            mService.send(message);
        }
        catch(RemoteException e) {
            SteamChat.debug(this, e.getMessage(), e);
        }
    }

    /**
     * Setup the form to show only the required fields for a steam guard protected login.
     */
    private void setupGuardState() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                guarded = true;
                findViewById(R.id.guard_layout).setVisibility(View.VISIBLE);
                findViewById(R.id.credentials_layout).setVisibility(View.GONE);

                TextView mTextView = (TextView) findViewById(R.id.machine);
                final String machine = StringUtils.isEmpty(android.os.Build.MODEL)
                        ? "SteamChat"
                        : "SteamChat:" + android.os.Build.MODEL;
                mTextView.setText(machine);
            }
        });
    }

    /**
     * Setup the form to show only the required fields for a basic login.
     */
    private void setupNormalState() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                guarded = false;
                findViewById(R.id.guard_layout).setVisibility(View.GONE);
                ((EditText) findViewById(R.id.password)).setText("");
            }
        });
    }

    /**
     * @return whether the form is valid or not.
     */
    private boolean isValid() {
        EditText mUsername = ((EditText) findViewById(R.id.username));
        EditText mPassword = ((EditText) findViewById(R.id.password));
        EditText mGuard = ((EditText) findViewById(R.id.guard));
        EditText mMachine = ((EditText) findViewById(R.id.machine));

        if (mUsername.getText().length() == 0) {
            mUsername.setError("Enter your username.");
            mUsername.requestFocus();
            return false;
        }

        if (mPassword.getText().length() == 0) {
            mPassword.setError("Enter your password.");
            mPassword.requestFocus();
            return false;
        }

        if (guarded && mGuard.getText().length() == 0) {
            mGuard.setError("Enter the Steam Guard code that has been sent to your email.");
            mGuard.requestFocus();
            return false;
        }

        if (guarded && mMachine.getText().length() == 0) {
            mGuard.setError("Enter a name to identify this connecting machine.");
            mGuard.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * @return the entered username
     */
    private String getUsername() {
        return ((EditText) findViewById(R.id.username)).getText().toString();
    }

    /**
     * @return the entered password
     */
    private String getPassword() {
        return ((EditText) findViewById(R.id.password)).getText().toString();
    }

    /**
     * @return the entered steam guard code
     */
    private String getGuard() {
        final String temp = ((EditText) findViewById(R.id.guard)).getText().toString();
        return StringUtils.isEmpty(temp) ? null : temp;
    }

    /**
     * @return the entered users machine name.
     */
    private String getMachine() {
        final String temp = ((EditText) findViewById(R.id.machine)).getText().toString();
        return StringUtils.isEmpty(temp) ? null : temp;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
    }
}
