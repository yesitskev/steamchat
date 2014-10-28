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

package com.kevelbreh.steamchat.util;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.util.Log;

/**
 * Dump will log all the columns and data of a given URI.  This becomes handy because its an absolute
 * pain in the ass to get the SQLite database from the device to check whats going on.
 */
public class Dump {

    /*
     * Dump all data from the URI.
     */
    public static void dump(Context context, Uri uri) {

        Cursor cursor = null;
        boolean gotHeadings = false;
        String[] row_headers;
        String[][] rows;
        int[] row_sizes;

        try {
            cursor = context.getContentResolver().query(uri, null, null, null, null);
            row_headers = new String[cursor.getColumnCount()];
            row_sizes = new int[cursor.getColumnCount()];
            rows = new String[cursor.getCount()][cursor.getColumnCount()];

            int current = 0;
            while (cursor.moveToNext()) {

                // Set the column headers and the column sizes.
                if (!gotHeadings) {
                    gotHeadings = true;
                    for (int i = 0; i < cursor.getColumnCount(); i++) {
                        row_headers[i] = cursor.getColumnName(i);
                        row_sizes[i] = cursor.getColumnName(i).length() + 10;
                    }
                }

                // Add data and set lengths.
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    try {
                        String val = cursor.getString(i);
                        if (val == null) val = "NULL";
                        rows[current][i] = val;
                        if (row_sizes[i] < val.length()) {
                            row_sizes[i] = val.length() + 10;
                        }
                    } catch(SQLiteException e) {
                        String val = "NULL";
                        rows[current][i] = val;
                        if (row_sizes[i] < val.length()) {
                            row_sizes[i] = val.length() + 10;
                        }
                    }
                }

                current++;
            }

            // Print headers.
            String temp = "";
            for (int column = 0; column < row_headers.length; column++) {
                temp += setPadding(
                        row_headers[column],
                        row_sizes[column]
                );
            }
            Log.d(uri.getLastPathSegment(), temp);

            // Print rows.
            for (int row = 0; row < rows.length; row++) {
                temp = "";
                for (int column = 0; column < rows[row].length; column++) {
                    temp += setPadding(
                            rows[row][column],
                            row_sizes[column]
                    );
                }
                Log.d(uri.getLastPathSegment(), temp);
            }
        }
        finally {
            if (cursor != null) { cursor.close(); }
        }
    }

    private static String setPadding(String word, int length) {
        if (word.length() == length) return word;

        int len = (length - word.length());
        while(len > 0) {
            word += " ";
            --len;
        }

        return word;
    }
}
