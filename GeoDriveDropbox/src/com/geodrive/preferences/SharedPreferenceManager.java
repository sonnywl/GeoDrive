
package com.geodrive.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.geodrive.StaticInfo;

public class SharedPreferenceManager {
    static SharedPreferenceManager sManager;
    private SharedPreferences pref;
    private Context mContext;

    public static SharedPreferenceManager getInstance(Context context) {
        if (sManager != null) {
            return sManager;
        }
        return new SharedPreferenceManager(context);
    }

    public SharedPreferenceManager(Context context) {
        mContext = context;
        pref = mContext.getSharedPreferences(StaticInfo.APP_NAME, 0);
    }

    public String getStringValue(String key, String defValue) {
        return pref.getString(key, defValue);
    }

    public boolean getBooleanValue(String key, boolean defValue) {
        return pref.getBoolean(key, defValue);
    }

    public int getIntValue(String key, int defValue) {
        return pref.getInt(key, defValue);
    }

    public void updateInt(String key, int value) {
        pref.edit().putInt(key, value).commit();
    }

    public void updateString(String key, String value) {
        pref.edit().putString(key, value).commit();
    }

    public void updateBoolean(String key, boolean value) {
        pref.edit().putBoolean(key, value).commit();
    }

    public void clearAll() {
        pref.edit().clear().commit();
    }
}
