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

package com.kevelbreh.steamchat.provider;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.kevelbreh.steamchat.provider.content.InteractionContentItem;
import com.kevelbreh.steamchat.provider.content.PersonaContentItem;
import com.kevelbreh.steamchat.provider.content.UserContentItem;

import edu.mit.mobile.android.content.ForeignKeyDBHelper;
import edu.mit.mobile.android.content.GenericDBHelper;
import edu.mit.mobile.android.content.SimpleContentProvider;


public class SteamProvider extends SimpleContentProvider {

    /**
     * Content provider authority.
     */
    public static final String AUTHORITY = "com.kevelbreh.steamchat.provider";

    /**
     * Steam Content Provider.
     *
     * Change Log:
     * 1. Initial
     * 2. Tweaks
     * 3. Split up the UserDetail and User into their own content items.
     * 4. Table name updates.
     * 5. -
     * 6. Added conversation table.
     * 7. Added interaction table.
     * 8. Altered interaction table, removed conversation table.
     * 9. Remove data for russ.
     * 10. Remove data
     * 11. Removed everything.
     * 14. user_id
     * 16: added interactions
     * 17: completely reworked provider.
     */
    public SteamProvider() {
        super(AUTHORITY, "steamchat.db", 17);

        // Create a relationship between users and persona' from Persona.USER -> User._ID
        final ForeignKeyDBHelper persona =
                new ForeignKeyDBHelper(UserContentItem.class, PersonaContentItem.class, PersonaContentItem.USER);

        // Create a relationship between users and persona' from Interaction.USER -> User._ID
        final ForeignKeyDBHelper interaction =
                new ForeignKeyDBHelper(UserContentItem.class, InteractionContentItem.class, InteractionContentItem.USER);

        addDirAndItemUri(new GenericDBHelper(UserContentItem.class), UserContentItem.PATH_OR_TABLE);
        addDirAndItemUri(interaction, InteractionContentItem.PATH_ALL_INTERACTIONS);
        addChildDirAndItemUri(persona, UserContentItem.PATH_OR_TABLE, PersonaContentItem.PATH_OR_TABLE);
        addChildDirAndItemUri(interaction, UserContentItem.PATH_OR_TABLE, InteractionContentItem.PATH_OR_TABLE);
    }

    /**
     * Had to override this to make it thread safe by applying the synchronized key word. For
     * parameters and description see {@link edu.mit.mobile.android.content.SimpleContentProvider}
     */
    @Override
    public synchronized Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return super.query(uri, projection, selection, selectionArgs, sortOrder);
    }

    /**
     * Had to override this to make it thread safe by applying the synchronized key word. For
     * parameters and description see {@link edu.mit.mobile.android.content.SimpleContentProvider}
     */
    @Override
    public synchronized int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return super.update(uri, values, selection, selectionArgs);
    }

    /**
     * Had to override this to make it thread safe by applying the synchronized key word. For
     * parameters and description see {@link edu.mit.mobile.android.content.SimpleContentProvider}
     */
    @Override
    public synchronized Uri insert(Uri uri, ContentValues values) {
        return super.insert(uri, values);
    }
}
