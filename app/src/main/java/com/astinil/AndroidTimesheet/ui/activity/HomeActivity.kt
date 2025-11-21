package com.astinil.AndroidTimesheet.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.astinil.AndroidTimesheet.R
import com.astinil.AndroidTimesheet.util.Prefs

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val cardTimesheet = findViewById<LinearLayout>(R.id.cardTimesheet)
        val cardTimeLog = findViewById<LinearLayout>(R.id.cardTimeLog)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        // ✔ Timesheet screen
        cardTimesheet.setOnClickListener {
            startActivity(Intent(this, TimesheetActivity::class.java))
        }

        // ✔ Time Log screen
        cardTimeLog.setOnClickListener {
            startActivity(Intent(this, TimeLogActivity::class.java))
        }

        // ✔ Logout button
        btnLogout.setOnClickListener {
            Prefs(this).clear()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
