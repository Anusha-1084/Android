package com.astinil.AndroidTimesheet.api;

import com.astinil.AndroidTimesheet.api.model.ApiResponse;
import com.astinil.AndroidTimesheet.api.model.AuthResponse;
import com.astinil.AndroidTimesheet.api.model.CheckOutDto;
import com.astinil.AndroidTimesheet.api.model.CheckOutStatusDto;
import com.astinil.AndroidTimesheet.api.model.LoginRequest;
import com.astinil.AndroidTimesheet.api.model.SignupRequest;
import com.astinil.AndroidTimesheet.api.model.TimeSheetRequest;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    @POST("/auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("/auth/register")
    Call<ApiResponse<Map<String, String>>> register(@Body SignupRequest signupRequest);

    @POST("/checkout/checkin")
    Call<CheckOutDto> checkIn();

    @POST("/checkout")
    Call<CheckOutDto> checkOut(@Query("totalHours") String totalHours);

    @GET("/checkout/status")
    Call<CheckOutStatusDto> getCheckOutStatus();

    // ⭐ Timesheet Submit API
    @POST("/timesheet/log")
    Call<ApiResponse<Void>> submitTimesheet(@Body TimeSheetRequest request);
}
