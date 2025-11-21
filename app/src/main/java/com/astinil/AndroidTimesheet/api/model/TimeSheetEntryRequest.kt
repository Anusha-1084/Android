package com.astinil.AndroidTimesheet.api.model

data class TimesheetEntryRequest(
    val workDate: String,
    val hoursWorked: Double,
    val job: String,
    val projectId: String
)
