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

package com.kevelbreh.steamchat;

import android.app.Application;
import android.util.Log;

public class SteamChat extends Application {

    private static final String TAG = "SteamChat";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     * Wrapper for logging with a default tag.
     *
     * @param message displayed.
     */
    public static void debug(String message) {
        Log.d(TAG, message);
    }

    /**
     * Wrapper for logging with object simple class name as tag.  This makes it a whole lot easier
     * to see where we are logging things from.
     *
     * @param object used as log tag.
     * @param message displayed.
     */
    public static void debug(Object object, String message) {
        Log.d(object.getClass().getSimpleName(), message);
    }

    /**
     * Wrapper for logging with object simple class name as tag.  This makes it a whole lot easier
     * to see where we are logging things from.
     *
     * @param object used as log tag.
     * @param message displayed.
     */
    public static void debug(Object object, String message, Throwable exception) {
        Log.d(object.getClass().getSimpleName(), message, exception);
    }

}
