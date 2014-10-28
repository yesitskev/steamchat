package com.kevelbreh.steamchat.steam;

import android.os.Handler;
import android.os.Message;

import com.kevelbreh.steamchat.SteamChat;

/**
 * Steam service handler is responsible for relaying data between the Steam android service
 * {@link com.kevelbreh.steamchat.steam.SteamService} and any other activity or fragment wanting to
 * send or receive information.
 *
 * This handler is used on both ends of communication.
 */
public class SteamServiceHandler extends Handler {

    public static final int ARG_NORMAL                                  = 0;
    public static final int ARG_BROADCAST                               = -1;

    public static final int HANDLER_ADD                                 = 101;
    public static final int HANDLER_REMOVE                              = 102;

    public static final int CONNECTION_REFUSED                          = 201;
    public static final int CONNECTION_TERMINATED                       = 202;
    public static final int CONNECTION_TERMINATE                        = 203;
    public static final int CONNECTION_ESTABLISHED                      = 204;
    public static final int CONNECTION_RETRY                            = 205;
    public static final int CONNECTION_SHUFFLE_TARGET                   = 206;

    public static final int AUTHENTICATION_REQUIRED                     = 301;
    public static final int AUTHENTICATE_NOW                            = 302;
    public static final int AUTHENTICATION_GUARDED                      = 303;
    public static final int AUTHENTICATION_SUCCESS                      = 304;
    public static final int AUTHENTICATION_FAILED                       = 305;
    public static final int AUTHENTICATION_INVALID_PASSWORD             = 306;
    public static final int IS_AUTHENTICATED                            = 307;
    public static final int GET_AUTHENTICATOR                           = 308;
    public static final int GOT_AUTHENTICATOR                           = 309;

    @Override
    public void handleMessage(Message message) {
        if (message.arg1 == ARG_BROADCAST) {
            onBroadcast(message);
        }

        switch (message.what) {
            case HANDLER_ADD: onAddHandler(message); break;
            case HANDLER_REMOVE: onRemoveHandler(message); break;
            case CONNECTION_REFUSED: onConnectionRefused(message); break;
            case CONNECTION_TERMINATED: onConnectionTerminated(message); break;
            case CONNECTION_ESTABLISHED: onConnectionEstablished(message); break;
            case AUTHENTICATION_REQUIRED: onAuthenticationRequired(message); break;
            case AUTHENTICATE_NOW: onAuthenticateNow(message); break;
            case AUTHENTICATION_GUARDED: onAuthenticationGuarded(message); break;
            case AUTHENTICATION_SUCCESS: onAuthenticationSuccess(message); break;
            case AUTHENTICATION_FAILED: onAuthenticationFailed(message); break;
            case AUTHENTICATION_INVALID_PASSWORD: onAuthenticationInvalidPassword(message); break;
            case IS_AUTHENTICATED: onIsAuthenticated(message); break;
            case GET_AUTHENTICATOR: onGetAuthenticator(message); break;
            case GOT_AUTHENTICATOR: onGetAuthenticator(message); break;
            default:
                super.handleMessage(message);
        }
    }

    /**
     *
     * @param message being received via the handler.
     */
    public void onBroadcast(Message message) {
        // This needs to be overridden to to be handled.
    }

    /**
     *
     * @param message being received via the handler.
     */
    public void onAddHandler(Message message) {
        // This needs to be overridden to to be handled.
    }

    /**
     *
     * @param message being received via the handler.
     */
    public void onRemoveHandler(Message message) {
        // This needs to be overridden to to be handled.
    }

    /**
     *
     * @param message being received via the handler.
     */
    public void onConnectionRefused(Message message) {
        // This needs to be overridden to to be handled.
    }

    /**
     *
     * @param message being received via the handler.
     */
    public void onConnectionTerminated(Message message) {
        // This needs to be overridden to to be handled.
    }

    /**
     *
     * @param message being received via the handler.
     */
    public void onConnectionEstablished(Message message) {
        // This needs to be overridden to to be handled.
    }

    /**
     *
     * @param message being received via the handler.
     */
    public void onAuthenticationRequired(Message message) {
        // This needs to be overridden to to be handled.
    }

    /**
     *
     * @param message being received via the handler.
     */
    public void onAuthenticateNow(Message message) {
        // This needs to be overridden to to be handled.
    }

    /**
     *
     * @param message being received via the handler.
     */
    public void onAuthenticationGuarded(Message message) {
        // This needs to be overridden to to be handled.
    }

    /**
     *
     * @param message being received via the handler.
     */
    public void onAuthenticationSuccess(Message message) {
        // This needs to be overridden to to be handled.
    }

    /**
     *
     * @param message being received via the handler.
     */
    public void onAuthenticationFailed(Message message) {
        // This needs to be overridden to to be handled.
    }

    /**
     *
     * @param message being received via the handler.
     */
    public void onAuthenticationInvalidPassword(Message message) {
        // This needs to be overridden to to be handled.
    }

    /**
     *
     * @param message being received via the handler.
     */
    public void onIsAuthenticated(Message message) {
        // This needs to be overridden to to be handled.
    }

    /**
     *
     * @param message being received via the handler.
     */
    public void onGetAuthenticator(Message message) {
        // This needs to be overridden to to be handled.
    }

    /**
     *
     * @param message being received via the handler.
     */
    public void onGotAuthenticator(Message message) {
        // This needs to be overridden to to be handled.
    }

}
