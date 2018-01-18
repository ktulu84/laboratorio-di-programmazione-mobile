package it.univaq.ing.myshiprace.Util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * MyService
 * Created by leonardo on 17/11/17.
 * <p>
 * BiTE s.r.l.
 * contact info@bitesrl.it
 */

public class Preferences
{

    public static void save(Context context, String key, boolean value)
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static boolean load(Context context, String key, boolean fallback)
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getBoolean(key, fallback);
    }

    public static void save(Context context, String key, String value)
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String load(Context context, String key, String fallback)
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString(key, fallback);
    }

    public static float load(Context context, String key, float fallback)
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        Float f = Float.valueOf(fallback);
        float r = Float.valueOf(pref.getString(key, f.toString()));
        return r;
    }
}
