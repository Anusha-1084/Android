package com.astinil.AndroidTimesheet.ui.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.astinil.AndroidTimesheet.R;
import com.astinil.AndroidTimesheet.api.ApiClient;
import com.astinil.AndroidTimesheet.api.ApiService;
import com.astinil.AndroidTimesheet.api.model.ApiResponse;
import com.astinil.AndroidTimesheet.api.model.SignupRequest;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText etFirstName, etLastName, etUsername, etEmail, etMobile, etPassword, etConfirmPassword;
    private Button btnRegister;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        apiService = ApiClient.getApiService(this);

        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etMobile = findViewById(R.id.etMobile);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {

        String first = etFirstName.getText().toString().trim();
        String last = etLastName.getText().toString().trim();
        String user = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String mobile_no = etMobile.getText().toString().trim();   // ✅ snake_case
        String pass = etPassword.getText().toString().trim();
        String confirm = etConfirmPassword.getText().toString().trim();

        if (first.isEmpty() || last.isEmpty() || user.isEmpty() ||
                email.isEmpty() || mobile_no.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!pass.equals(confirm)) {
            Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
            return;
        }

        // backend optional fields
        String bio = null;
        String location = null;
        String imageBase64 = null;

        SignupRequest signupRequest = new SignupRequest(
                first,
                last,
                user,
                pass,
                email,
                mobile_no,      // ✅ backend expects this
                bio,
                location,
                imageBase64
        );

        apiService.register(signupRequest).enqueue(new Callback<ApiResponse<Map<String, String>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, String>>> call,
                                   Response<ApiResponse<Map<String, String>>> response) {

                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    Toast.makeText(RegisterActivity.this,
                            "Account created successfully!", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(RegisterActivity.this,
                            "Registration failed: " +
                                    (response.body() != null ? response.body().message : "Unknown error"),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Map<String, String>>> call, Throwable t) {
                Toast.makeText(RegisterActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
