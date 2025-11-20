package com.astinil.AndroidTimesheet.api.model;

import com.google.gson.annotations.SerializedName;

public class CheckOutStatusDto {
    @SerializedName("user_id")
    public String userId;

    @SerializedName("status")
    public String status;

    @SerializedName("last_check_in")
    public String lastCheckIn;

    @SerializedName("last_check_out")
    public String lastCheckOut;

    @SerializedName("total_hours_today")
    public String totalHoursToday;
}
