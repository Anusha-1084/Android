package com.astinil.AndroidTimesheet.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.astinil.AndroidTimesheet.R;
import com.astinil.AndroidTimesheet.api.ApiClient;
import com.astinil.AndroidTimesheet.api.ApiService;
import com.astinil.AndroidTimesheet.api.model.CheckInDto;
import com.astinil.AndroidTimesheet.api.model.CheckOutDto;
import com.astinil.AndroidTimesheet.api.model.CheckOutStatusDto;
import com.astinil.AndroidTimesheet.util.Prefs;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TimesheetActivity extends AppCompatActivity {

    private static final String TAG = "TimesheetActivity";
    private static final long CHECKOUT_COOLDOWN_MS = 4000; // 4 seconds

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
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            showDailyTimesheetDialog(year, month + 1, dayOfMonth);
        });

//
        ImageView ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> finish());


        setupDropdowns();

        tvUserIdName.setText(prefs.getUsername());
        tvEmpId.setText("ID: " + prefs.getUsername());

        loadStatus();

        btnCheckInOut.setOnClickListener(v -> {
            if (isCheckedIn) doCheckOut();
            else doCheckIn();
        });

        btnLogout.setOnClickListener(v -> logout());
    }


    private void setupDropdowns() {
        Spinner spinnerMonth = findViewById(R.id.spinnerMonth);
        Spinner spinnerYear = findViewById(R.id.spinnerYear);

        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};

        ArrayAdapter<String> monthAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, months);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(monthAdapter);

        ArrayList<String> years = new ArrayList<>();
        for (int y = 2020; y <= 2040; y++) years.add(String.valueOf(y));

        ArrayAdapter<String> yearAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(yearAdapter);
    }

    private void showDailyTimesheetDialog(int year, int month, int day) {

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_daily_timesheet, null);

        EditText etWorkDone = dialogView.findViewById(R.id.etWorkDone);
        EditText etBlockers = dialogView.findViewById(R.id.etBlockers);
        EditText etTomorrow = dialogView.findViewById(R.id.etTomorrow);

        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnSubmitDaily = dialogView.findViewById(R.id.btnSubmitDaily);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        dialog.show();

        // ❌ CANCEL
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // ✅ SUBMIT
        btnSubmitDaily.setOnClickListener(v -> {

            String workDone = etWorkDone.getText().toString().trim();
            String blockers = etBlockers.getText().toString().trim();
            String tomorrow = etTomorrow.getText().toString().trim();

            Toast.makeText(
                    this,
                    "Saved:\nWork: " + workDone + "\nBlockers: " + blockers + "\nTomorrow: " + tomorrow,
                    Toast.LENGTH_LONG
            ).show();

            dialog.dismiss();
        });
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

                boolean backendSaysIn = "IN".equalsIgnoreCase(status.status);

                // 🟢 CHECK if last check-in is today
                boolean todaySession = false;
                if (status.lastCheckIn != null) {
                    todaySession = status.lastCheckIn.startsWith(
                            LocalDateTime.now().toLocalDate().toString()
                    );
                }

                if (backendSaysIn && todaySession) {

                    // 🟢 READ total hours already worked today
                    long oldSeconds = 0;
                    if (status.totalHoursToday != null) {
                        String[] parts = status.totalHoursToday.split(":");
                        oldSeconds =
                                Integer.parseInt(parts[0]) * 3600 +
                                        Integer.parseInt(parts[1]) * 60 +
                                        Integer.parseInt(parts[2]);
                    }

                    // 🟢 CONTINUE timer from previous worked hours
                    checkInTime = LocalDateTime.now().minusSeconds(oldSeconds);

                    isCheckedIn = true;
                    tvStatus.setText("IN");
                    tvStatus.setTextColor(0xFF2E7D32);
                    btnCheckInOut.setText("CHECK OUT");

                    startTimer();
                }
                else {
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

        checkInTime = null;
        timerHandler.removeCallbacks(timerRunnable);
    }
    private void doCheckIn() {

        api.checkIn().enqueue(new Callback<CheckOutDto>() {
            @Override
            public void onResponse(Call<CheckOutDto> call, Response<CheckOutDto> response) {

                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(TimesheetActivity.this, "Check-in Failed!", Toast.LENGTH_SHORT).show();
                    return;
                }

                CheckOutDto data = response.body();
                isCheckedIn = true;

                long oldSeconds = 0;
                if (data.totalHours != null) {
                    String[] parts = data.totalHours.split(":");
                    oldSeconds =
                            Integer.parseInt(parts[0]) * 3600 +
                                    Integer.parseInt(parts[1]) * 60 +
                                    Integer.parseInt(parts[2]);
                }

                // 🟢 Continue timer from old seconds
                checkInTime = LocalDateTime.now().minusSeconds(oldSeconds);

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

                    Toast.makeText(TimesheetActivity.this,
                            "Checked Out at: " + response.body().checkOutTime,
                            Toast.LENGTH_SHORT).show();

                    // 🚀 STOP TIMER COMPLETELY
                    timerHandler.removeCallbacks(timerRunnable);
                    checkInTime = null;

                    setOutUI(); // Reset UI
                } else {
                    Toast.makeText(TimesheetActivity.this, "Checkout Failed!", Toast.LENGTH_SHORT).show();
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

    private final Runnable timerRunnable = this::runTimer;

    private void runTimer() {
        if (checkInTime != null) {
            Duration diff = Duration.between(checkInTime, LocalDateTime.now());
            long h = diff.toHours();
            long m = diff.toMinutes() % 60;
            long s = diff.getSeconds() % 60;

            tvTimer.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s));
        }

        timerHandler.postDelayed(timerRunnable, 1000);
    }

    private void startTimer() {
        timerHandler.removeCallbacks(timerRunnable);
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
