package com.astinil.AndroidTimesheet.api.model;

import com.google.gson.annotations.SerializedName;

public class CheckOutDto {

    @SerializedName("user_id")
    public String userId;

    @SerializedName("status")
    public String status;

    @SerializedName("check_in_time")
    public String checkInTime;

    @SerializedName("check_out_time")
    public String checkOutTime;

    @SerializedName("total_hours")
    public String totalHours;
}
