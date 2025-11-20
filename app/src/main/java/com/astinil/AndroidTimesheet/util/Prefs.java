package com.astinil.AndroidTimesheet.util;

import android.content.Context;
import android.content.SharedPreferences;

public class Prefs {
    private static final String PREF = "timesheet_prefs";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USERNAME = "username";
    private final SharedPreferences sp;

    public Prefs(Context ctx) {
        sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public void saveToken(String token) {
        sp.edit().putString(KEY_TOKEN, token).apply();
    }

    public String getToken() {
        return sp.getString(KEY_TOKEN, null);
    }

    public void saveUsername(String username){
        sp.edit().putString(KEY_USERNAME, username).apply();
    }

    public String getUsername(){
        return sp.getString(KEY_USERNAME, null);
    }
    public void clear() {
        sp.edit().clear().apply();
    }

}
