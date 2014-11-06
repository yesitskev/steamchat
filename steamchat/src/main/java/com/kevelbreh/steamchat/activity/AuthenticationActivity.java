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
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.kevelbreh.steamchat.R;
import com.kevelbreh.steamchat.SteamChat;
import com.kevelbreh.steamchat.steam.language.Language;
import com.kevelbreh.steamchat.steam2.SteamService;

import org.apache.commons.lang.StringUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Authenticate a user to the steam network.  This activity is responsible for logging in the user
 * using a username and password.  If this is a first time login using this device, steam guard will
 * probably want the user to authenticate the device also.
 *
 * TODO: Add spinner when performing a network operation (logging in attempts).
 */
public class AuthenticationActivity extends Activity {

    private boolean guarded = false;

    @InjectView(R.id.credentials_container) ViewGroup mCredentialsContainer;
    @InjectView(R.id.guard_container) ViewGroup mGuardContainer;
    @InjectView(R.id.authenticate) Button mAuthenticate;
    @InjectView(R.id.username) EditText mUsername;
    @InjectView(R.id.password) EditText mPassword;
    @InjectView(R.id.guard) EditText mGuard;
    @InjectView(R.id.machine) EditText mMachine;

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
        setContentView(R.layout.activity_login_2);
        ButterKnife.inject(this);
        bindService(new Intent(this, SteamService.class), mConnection, Context.BIND_AUTO_CREATE);
        mAuthenticate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                authenticate();
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);

        switch (intent.getIntExtra("result", -500)) {
            // Login was successful!
            case Language.Result.OK:
                startActivity(new Intent(this, InteractionsActivity.class));
                finish();
                break;
            // Steam requires the user to provide the Steam Guard code emailed to their email address
            // registered on Steam.
            case Language.Result.ACCOUNT_LOGON_DENIED:
                setupGuardState();
                break;
            // Some other error occurred.  Display this to the user for their viewing. Possibly reset
            // the login screen.
            default:
                setupNormalState();
                Toast.makeText(this, "Something went wrong.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
    }

    /**
     * Authenticate the user.  Perform actions depending on the required form state and result code
     * of the Steam API.
     */
    @OnClick(R.id.authenticate)
    public void authenticate() {
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
                mGuardContainer.setVisibility(View.VISIBLE);
                mCredentialsContainer.setVisibility(View.GONE);

                if (StringUtils.isEmpty(android.os.Build.MODEL) && android.os.Build.MODEL.length() >= 6) {
                    mMachine.setText(android.os.Build.MODEL);
                }
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
                mCredentialsContainer.setVisibility(View.VISIBLE);
                mGuardContainer.setVisibility(View.GONE);
                mPassword.setText(null);
            }
        });
    }

    /**
     * @return whether the form is valid or not.
     */
    private boolean isValid() {
        boolean hasError = false;

        if (mUsername.getText().length() == 0) {
            mUsername.setError("Enter your username.");
            hasError = true;
        }

        if (mPassword.getText().length() == 0) {
            mPassword.setError("Enter your password.");
            hasError = true;
        }

        if (guarded && mGuard.getText().length() == 0) {
            mGuard.setError("Enter the Steam Guard code that has been sent to your email.");
            hasError = true;
        }

        if (guarded && mGuard.getText().length() != 5) {
            mGuard.setError("The Steam Guard code sent to you can only be 5 characters in length.");
            hasError = true;
        }

        if (guarded && mMachine.getText().length() == 0) {
            mMachine.setError("Enter a name to identify this connecting machine.");
            hasError = true;
        }

        if (guarded && mMachine.getText().length() < 6) {
            mMachine.setError("The machine name needs to be longer than 6 characters.");
            hasError = true;
        }

        return !hasError;
    }

    /**
     * @return the entered username
     */
    private String getUsername() {
        return mUsername.getText().toString();
    }

    /**
     * @return the entered password
     */
    private String getPassword() {
        return mPassword.getText().toString();
    }

    /**
     * @return the entered steam guard code or null.
     */
    private String getGuard() {
        final String temp = mGuard.getText().toString();
        return StringUtils.isEmpty(temp) ? null : temp;
    }

    /**
     * @return the entered users machine name or null.
     */
    private String getMachine() {
        final String temp = mMachine.getText().toString();
        return StringUtils.isEmpty(temp) ? null : temp;
    }
}
