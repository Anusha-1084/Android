package com.astinil.AndroidTimesheet.api.model


import com.astinil.AndroidTimesheet.api.model.TimesheetEntryRequest;
data class TimeSheetRequest(
    val timesheetType: String,
    val startDate: String,
    val endDate: String,
    val job: String,
    val projectId: String,
    val entries: List<TimesheetEntryRequest>
)
