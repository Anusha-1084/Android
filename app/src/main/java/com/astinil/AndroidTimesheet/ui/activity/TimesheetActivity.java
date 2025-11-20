package com.astinil.AndroidTimesheet.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.astinil.AndroidTimesheet.R;
import com.astinil.AndroidTimesheet.api.ApiClient;
import com.astinil.AndroidTimesheet.api.ApiService;
import com.astinil.AndroidTimesheet.api.model.CheckOutDto;
import com.astinil.AndroidTimesheet.api.model.CheckOutStatusDto;
import com.astinil.AndroidTimesheet.util.Prefs;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TimesheetActivity extends AppCompatActivity {

    private TextView tvUserIdName, tvEmpId, tvStatus, tvTimer;
    private ImageView imgProfile;
    private Button btnCheckInOut, btnLogout;
    private CalendarView calendarView;

    private Prefs prefs;
    private ApiService api;

    private Handler timerHandler = new Handler();
    private LocalDateTime checkInTime;
    private boolean isCheckedIn = false;

    private final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timesheet);

        prefs = new Prefs(this);
        api = ApiClient.getSecuredApi(this);

        if (prefs.getToken() == null) {
            goToLogin();
            return;
        }

        tvUserIdName = findViewById(R.id.tvUserIdName);
        tvEmpId = findViewById(R.id.tvEmpId);
        tvStatus = findViewById(R.id.tvStatus);
        tvTimer = findViewById(R.id.tvTimer);
        imgProfile = findViewById(R.id.imgProfile);
        btnCheckInOut = findViewById(R.id.btnCheckInOut);
        btnLogout = findViewById(R.id.btnLogout);
        calendarView = findViewById(R.id.calendarView);

        tvUserIdName.setText(prefs.getUsername());
        tvEmpId.setText("ID: " + prefs.getUsername());

        loadStatus();

        btnCheckInOut.setOnClickListener(v -> {
            if (isCheckedIn) doCheckOut();
            else doCheckIn();
        });

        btnLogout.setOnClickListener(v -> logout());
    }

    private void loadStatus() {

        api.getCheckOutStatus().enqueue(new Callback<CheckOutStatusDto>() {
            @Override
            public void onResponse(Call<CheckOutStatusDto> call, Response<CheckOutStatusDto> response) {

                if (!response.isSuccessful() || response.body() == null) {
                    setOutUI();
                    return;
                }

                CheckOutStatusDto status = response.body();

                if ("IN".equalsIgnoreCase(status.status)) {

                    isCheckedIn = true;

                    tvStatus.setText("IN");
                    tvStatus.setTextColor(0xFF2E7D32);

                    checkInTime = LocalDateTime.parse(status.lastCheckIn, formatter);

                    startTimer();
                    btnCheckInOut.setText("CHECK OUT");

                } else {
                    setOutUI();
                }
            }

            @Override
            public void onFailure(Call<CheckOutStatusDto> call, Throwable t) {
                setOutUI();
            }
        });
    }

    private void setOutUI() {
        isCheckedIn = false;
        tvStatus.setText("OUT");
        tvStatus.setTextColor(0xFFD32F2F);
        tvTimer.setText("00:00:00");
        btnCheckInOut.setText("CHECK IN");
    }

    private void doCheckIn() {

        api.checkInCheckout().enqueue(new Callback<CheckOutDto>() {
            @Override
            public void onResponse(Call<CheckOutDto> call, Response<CheckOutDto> response) {

                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(TimesheetActivity.this, "Check-in Failed!", Toast.LENGTH_SHORT).show();
                    return;
                }

                CheckOutDto data = response.body();

                Toast.makeText(TimesheetActivity.this,
                        "Checked In at: " + data.checkInTime,
                        Toast.LENGTH_SHORT).show();

                isCheckedIn = true;

                checkInTime = LocalDateTime.parse(data.checkInTime.replace(" ", "T"));

                tvStatus.setText("IN");
                tvStatus.setTextColor(0xFF2E7D32);
                btnCheckInOut.setText("CHECK OUT");

                startTimer();
            }

            @Override
            public void onFailure(Call<CheckOutDto> call, Throwable t) {
                Toast.makeText(TimesheetActivity.this, "Check-In Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void doCheckOut() {
        String totalHours = tvTimer.getText().toString();

        api.checkOut(totalHours).enqueue(new Callback<CheckOutDto>() {
            @Override
            public void onResponse(Call<CheckOutDto> call, Response<CheckOutDto> response) {

                if (response.isSuccessful() && response.body() != null) {

                    CheckOutDto data = response.body();

                    Toast.makeText(TimesheetActivity.this,
                            "Checked Out at: " + data.checkOutTime,
                            Toast.LENGTH_SHORT).show();

                    timerHandler.removeCallbacks(timerRunnable);
                    setOutUI();

                } else {
                    Toast.makeText(TimesheetActivity.this,
                            "Checkout Failed!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CheckOutDto> call, Throwable t) {
                Toast.makeText(TimesheetActivity.this,
                        "Check-Out Error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (checkInTime != null) {
                Duration diff = Duration.between(checkInTime, LocalDateTime.now());
                long h = diff.toHours();
                long m = diff.toMinutes() % 60;
                long s = diff.getSeconds() % 60;

                tvTimer.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s));
            }
            timerHandler.postDelayed(this, 1000);
        }
    };

    private void startTimer() {
        timerHandler.post(timerRunnable);
    }

    private void logout() {
        prefs.clear();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void goToLogin() {
        Intent i = new Intent(this, LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
