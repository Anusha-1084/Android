package com.astinil.AndroidTimesheet;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.astinil.AndroidTimesheet.ui.activity.LoginActivity;
import com.astinil.AndroidTimesheet.ui.activity.TimesheetActivity;
import com.astinil.AndroidTimesheet.util.Prefs;


/**
 * Entry point. If token exists -> go to TimesheetActivity else LoginActivity
 */
public class MainActivity extends AppCompatActivity {
    Prefs prefs;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = new Prefs(this);
        String token = prefs.getToken();
        if (token == null || token.isEmpty()) {
            startActivity(new Intent(this, LoginActivity.class));
        } else {
            startActivity(new Intent(this, TimesheetActivity.class));
        }
        finish();
    }
}
