package com.astinil.AndroidTimesheet.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.astinil.AndroidTimesheet.R;
import com.astinil.AndroidTimesheet.api.ApiClient;
import com.astinil.AndroidTimesheet.api.ApiService;
import com.astinil.AndroidTimesheet.api.model.AuthResponse;
import com.astinil.AndroidTimesheet.api.model.LoginRequest;
import com.astinil.AndroidTimesheet.util.Prefs;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private ProgressBar loginProgress;
    private Prefs prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        prefs = new Prefs(this);

        if (prefs.getToken() != null) {
            goToTimesheet();
            return;
        }

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        loginProgress = findViewById(R.id.loginProgress);
        Button btnLogin = findViewById(R.id.btnLogin);

        TextView tvSignup = findViewById(R.id.tvSignup);
        tvSignup.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class))
        );

        btnLogin.setOnClickListener(v -> doLogin());
    }

    private void doLogin() {
        String u = etUsername.getText().toString().trim();
        String p = etPassword.getText().toString().trim();

        if (u.isEmpty() || p.isEmpty()) {
            Toast.makeText(this, "Enter credentials", Toast.LENGTH_SHORT).show();
            return;
        }

        loginProgress.setVisibility(View.VISIBLE);

        ApiService api = ApiClient.getApiService(this);
        Call<AuthResponse> call = api.login(new LoginRequest(u, p));

        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                loginProgress.setVisibility(View.GONE);

                if (response.isSuccessful() &&
                        response.body() != null &&
                        response.body().data != null) {

                    prefs.saveToken(response.body().data.accessToken);
                    prefs.saveUsername(u);

                    goToTimesheet();
                } else {
                    String errorMsg = "Login failed";

                    try {
                        String errorBody = response.errorBody().string();
                        JSONObject obj = new JSONObject(errorBody);
                        errorMsg = obj.getString("message");
                    } catch (Exception ignored) {}

                    Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                loginProgress.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this,
                        "Error: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void goToTimesheet() {
        startActivity(new Intent(LoginActivity.this, TimesheetActivity.class));
        finish();
    }
}
