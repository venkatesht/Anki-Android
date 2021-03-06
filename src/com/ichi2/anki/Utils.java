/****************************************************************************************
 * Copyright (c) 2009 Daniel Svärd <daniel.svard@gmail.com>                             *
 * Copyright (c) 2009 Edu Zamora <edu.zasu@gmail.com>                                   *
 *                                                                                      *
 * This program is free software; you can redistribute it and/or modify it under        *
 * the terms of the GNU General Public License as published by the Free Software        *
 * Foundation; either version 3 of the License, or (at your option) any later           *
 * version.                                                                             *
 *                                                                                      *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY      *
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A      *
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.             *
 *                                                                                      *
 * You should have received a copy of the GNU General Public License along with         *
 * this program.  If not, see <http://www.gnu.org/licenses/>.                           *
 ****************************************************************************************/

package com.ichi2.anki;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;

import com.mindprod.common11.BigDate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TreeSet;
import java.util.zip.Deflater;

/**
 * TODO comments
 */
public class Utils {

    // Used to format doubles with English's decimal separator system
    public static final Locale ENGLISH_LOCALE = new Locale("en_US");

    public static final int CHUNK_SIZE = 32768;

    private static final long MILLIS_IN_A_DAY = 86400000;
    private static final int DAYS_BEFORE_1970 = 719163;

    private static TreeSet<Integer> sIdTree;
    private static long sIdTime;


    /* Prevent class from being instantiated */
    private Utils() { }


    public static long genID() {
        long time = System.currentTimeMillis();
        long id;
        int rand;
        Random random = new Random();

        if (sIdTree == null) {
            sIdTree = new TreeSet<Integer>();
            sIdTime = time;
        } else if (sIdTime != time) {
            sIdTime = time;
            sIdTree.clear();
        }

        while (true) {
            rand = random.nextInt(2 ^ 23);
            if (!sIdTree.contains(new Integer(rand))) {
                sIdTree.add(new Integer(rand));
                break;
            }
        }
        id = rand << 41 | time;
        return id;
    }


    /**
     * Returns a SQL string from an array of integers.
     * @param ids The array of integers to include in the list.
     * @return An SQL compatible string in the format (ids[0],ids[1],..).
     */
    public static String ids2str(long[] ids) {
        String str = "(";
        int len = ids.length;
        for (int i = 0; i < len; i++) {
            if (i == (len - 1)) {
                str += ids[i];
            } else {
                str += ids[i] + ",";
            }
        }
        str += ")";
        return str;
    }


    /**
     * Returns a SQL string from an array of integers.
     * @param ids The array of integers to include in the list.
     * @return An SQL compatible string in the format (ids[0],ids[1],..).
     */
    public static String ids2str(JSONArray ids) {
        String str = "(";
        int len = ids.length();
        for (int i = 0; i < len; i++) {
            try {
                if (i == (len - 1)) {
                    str += ids.get(i);
                } else {
                    str += ids.get(i) + ",";
                }
            } catch (JSONException e) {
                Log.i(AnkiDroidApp.TAG, "JSONException = " + e.getMessage());
            }
        }
        str += ")";
        return str;
    }


    /**
     * Returns a SQL string from an array of integers.
     * @param ids The array of integers to include in the list.
     * @return An SQL compatible string in the format (ids[0],ids[1],..).
     */
    public static String ids2str(List<String> ids) {
        String str = "(";
        int len = ids.size();
        for (int i = 0; i < len; i++) {
            if (i == (len - 1)) {
                str += ids.get(i);
            } else {
                str += ids.get(i) + ",";
            }
        }
        str += ")";
        return str;
    }


    public static JSONArray listToJSONArray(List<Object> list) {
        JSONArray jsonArray = new JSONArray();

        for (Object o : list) {
            jsonArray.put(o);
        }

        return jsonArray;
    }


    public static List<String> jsonArrayToListString(JSONArray jsonArray) throws JSONException {
        ArrayList<String> list = new ArrayList<String>();

        int len = jsonArray.length();
        for (int i = 0; i < len; i++) {
            list.add(jsonArray.getString(i));
        }

        return list;
    }


    /**
     * Converts an InputStream to a String.
     * @param is InputStream to convert
     * @return String version of the InputStream
     */
    public static String convertStreamToString(InputStream is) {
        String contentOfMyInputStream = "";
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is), 4096);
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            rd.close();
            contentOfMyInputStream = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return contentOfMyInputStream;
    }


    /**
     * Compress data.
     * @param bytesToCompress is the byte array to compress.
     * @return a compressed byte array.
     * @throws java.io.IOException
     */
    public static byte[] compress(byte[] bytesToCompress) throws IOException {
        // Compressor with highest level of compression.
        Deflater compressor = new Deflater(Deflater.BEST_COMPRESSION);
        // Give the compressor the data to compress.
        compressor.setInput(bytesToCompress);
        compressor.finish();

        // Create an expandable byte array to hold the compressed data.
        // It is not necessary that the compressed data will be smaller than
        // the uncompressed data.
        ByteArrayOutputStream bos = new ByteArrayOutputStream(bytesToCompress.length);

        // Compress the data
        byte[] buf = new byte[bytesToCompress.length + 100];
        while (!compressor.finished()) {
            bos.write(buf, 0, compressor.deflate(buf));
        }

        bos.close();

        // Get the compressed data
        return bos.toByteArray();
    }


    /**
     * Utility method to write to a file.
     */
    public static boolean writeToFile(InputStream source, String destination) {
        try {
            Log.i(AnkiDroidApp.TAG, "Creating new file... = " + destination);
            new File(destination).createNewFile();

            OutputStream output = new FileOutputStream(destination);

            // Transfer bytes, from source to destination.
            byte[] buf = new byte[CHUNK_SIZE];
            int len;
            if (source == null) {
                Log.i(AnkiDroidApp.TAG, "source is null!");
            }
            while ((len = source.read(buf)) > 0) {
                output.write(buf, 0, len);
                Log.i(AnkiDroidApp.TAG, "Write...");
            }

            Log.i(AnkiDroidApp.TAG, "Finished writing!");
            output.close();

        } catch (Exception e) {
            return false;
        }
        return true;
    }


    // Print methods
    public static void printJSONObject(JSONObject jsonObject) {
        printJSONObject(jsonObject, "-", false);
    }


    public static void printJSONObject(JSONObject jsonObject, boolean writeToFile) {
        if (writeToFile) {
            new File("/sdcard/payloadAndroid.txt").delete();
        }
        printJSONObject(jsonObject, "-", writeToFile);
    }


    public static void printJSONObject(JSONObject jsonObject, String indentation, boolean writeToFile) {
        try {

            Iterator<String> keys = jsonObject.keys();
            TreeSet<String> orderedKeysSet = new TreeSet<String>();
            while (keys.hasNext()) {
                orderedKeysSet.add(keys.next());
            }

            Iterator<String> orderedKeys = orderedKeysSet.iterator();
            while (orderedKeys.hasNext()) {
                String key = orderedKeys.next();

                try {
                    Object value = jsonObject.get(key);
                    if (value instanceof JSONObject) {
                        if (writeToFile) {
                            BufferedWriter buff = new BufferedWriter(new FileWriter("/sdcard/payloadAndroid.txt", true));
                            buff.write(indentation + " " + key + " : ");
                            buff.newLine();
                            buff.close();
                        }
                        Log.i(AnkiDroidApp.TAG, "	" + indentation + key + " : ");
                        printJSONObject((JSONObject) value, indentation + "-", writeToFile);
                    } else {
                        if (writeToFile) {
                            BufferedWriter buff = new BufferedWriter(new FileWriter("/sdcard/payloadAndroid.txt", true));
                            buff.write(indentation + " " + key + " = " + jsonObject.get(key).toString());
                            buff.newLine();
                            buff.close();
                        }
                        Log.i(AnkiDroidApp.TAG, "	" + indentation + key + " = " + jsonObject.get(key).toString());
                    }
                } catch (JSONException e) {
                    Log.i(AnkiDroidApp.TAG, "JSONException = " + e.getMessage());
                }
            }

        } catch (IOException e1) {
            Log.i(AnkiDroidApp.TAG, "IOException = " + e1.getMessage());
        }

    }


    public static void saveJSONObject(JSONObject jsonObject) throws IOException {
        Log.i(AnkiDroidApp.TAG, "saveJSONObject");
        BufferedWriter buff = new BufferedWriter(new FileWriter("/sdcard/jsonObjectAndroid.txt", true));
        buff.write(jsonObject.toString());
        buff.close();
    }


    public static int booleanToInt(boolean b) {
        return (b) ? 1 : 0;
    }


    /**
     * Get the current time in seconds since January 1, 1970 UTC.
     * @return the local system time in seconds
     */
    public static double now() {
        return (System.currentTimeMillis() / 1000.0);
    }


    /**
     * @param utcOffset The UTC offset in seconds.
     * @return a new Date object 
     */
    public static Date genToday(double utcOffset) {
        // Get timezone offset in milliseconds
        Calendar now = Calendar.getInstance();
        int timezoneOffset = (now.get(Calendar.ZONE_OFFSET) + now.get(Calendar.DST_OFFSET));

        return new Date((long) (System.currentTimeMillis() - utcOffset * 1000 - timezoneOffset));
    }


    /**
     * Returns the proleptic Gregorian ordinal of the date, where January 1 of year 1 has ordinal 1.
     * @param date Date to convert to ordinal, since 01/01/01
     * @return The ordinal representing the date
     */
    public static int dateToOrdinal(Date date) {
        // BigDate.toOrdinal returns the ordinal since 1970, so we add up the days from 01/01/01 to 1970
        return BigDate.toOrdinal(date.getYear() + 1900, date.getMonth() + 1, date.getDate()) + DAYS_BEFORE_1970;
    }


    /**
     * Return the date corresponding to the proleptic Gregorian ordinal, where January 1 of year 1 has ordinal 1.
     * @param ordinal representing the days since 01/01/01
     * @return Date converted from the ordinal
     */
    public static Date ordinalToDate(int ordinal) {
        return new Date((ordinal - DAYS_BEFORE_1970) * MILLIS_IN_A_DAY);
    }


    /**
     * Indicates whether the specified action can be used as an intent. This method queries the package manager for
     * installed packages that can respond to an intent with the specified action. If no suitable package is found, this
     * method returns false.
     * @param context The application's environment.
     * @param action The Intent action to check for availability.
     * @return True if an Intent with the specified action can be sent and responded to, false otherwise.
     */
    public static boolean isIntentAvailable(Context context, String action) {
        final PackageManager packageManager = context.getPackageManager();
        final Intent intent = new Intent(action);
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }
    
    public static String getBaseUrl(Model model, String deckFileName) {
    	String base = model.getFeatures().trim();
    	if( base.length() == 0 )
    		base = "file://" + deckFileName.replace(".anki", ".media/");
    	return base;
    }
}
