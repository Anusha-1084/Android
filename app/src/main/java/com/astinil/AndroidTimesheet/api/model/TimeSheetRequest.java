package com.astinil.AndroidTimesheet.api.model;

public class TimeSheetRequest {
    public String date;
    public String attendanceType;  // CHECK_IN or CHECK_OUT
    public String remarks;

    public TimeSheetRequest() {}

    public TimeSheetRequest(String date, String attendanceType, String remarks) {
        this.date = date;
        this.attendanceType = attendanceType;
        this.remarks = remarks;
    }
}
