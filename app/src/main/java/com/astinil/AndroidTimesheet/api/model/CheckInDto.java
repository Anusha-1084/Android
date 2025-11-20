package com.astinil.AndroidTimesheet.api.model;

import com.google.gson.annotations.SerializedName;

public class CheckInDto {
    @SerializedName("user_id")
    public String userId;

    @SerializedName("status")
    public String status;

    @SerializedName("check_in_time")
    public String checkInTime;
}
