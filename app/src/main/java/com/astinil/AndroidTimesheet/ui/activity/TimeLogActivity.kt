package com.astinil.AndroidTimesheet.ui.activity



import android.os.Bundle
import android.widget.*
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.astinil.AndroidTimesheet.R
import com.astinil.AndroidTimesheet.api.ApiClient
import com.astinil.AndroidTimesheet.api.model.ApiResponse
import com.astinil.AndroidTimesheet.api.model.TimeSheetRequest
import com.astinil.AndroidTimesheet.api.model.TimesheetEntryRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*


class TimeLogActivity : AppCompatActivity() {

    private lateinit var rvDays: RecyclerView
    private lateinit var adapter: DayAdapter
    private lateinit var tvRange: TextView
    private lateinit var tvTotal: TextView

    private lateinit var spnProject: Spinner
    private lateinit var spnJob: Spinner
    private lateinit var spnWorkItem: Spinner

    private var viewType = "WEEKLY"
    private var weekOffset = 0
    private var monthOffset = 0

    private val dayList = mutableListOf<DayEntryModel>()

    private val dummyProjects = listOf("SWON123", "Project Y", "Project Z")
    private val dummyJobs = listOf("DEVELOPMENT", "SUPPORT", "TESTING")
    private val dummyWorkItems = listOf("WorkItem 1", "WorkItem 2", "WorkItem 3")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time_log)

        rvDays = findViewById(R.id.rvDays)
        tvRange = findViewById(R.id.tvRange)
        tvTotal = findViewById(R.id.tvTotal)

        spnProject = findViewById(R.id.spnProject)
        spnJob = findViewById(R.id.spnJob)
        spnWorkItem = findViewById(R.id.spnWorkItem)

        spnProject.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, dummyProjects)
        spnJob.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, dummyJobs)
        spnWorkItem.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, dummyWorkItems)

        adapter = DayAdapter(dayList) { updateTotal() }
        rvDays.layoutManager = GridLayoutManager(this, 3)
        rvDays.adapter = adapter

        val ivBack = findViewById<ImageView>(R.id.ivBack)
        ivBack.setOnClickListener { finish() }

        loadWeek()
        setupButtons()
    }

    private fun setupButtons() {

        findViewById<Button>(R.id.btnWeekly).setOnClickListener {
            viewType = "WEEKLY"
            weekOffset = 0
            loadWeek()
        }

        findViewById<Button>(R.id.btnMonthly).setOnClickListener {
            viewType = "MONTHLY"
            monthOffset = 0
            loadMonth()
        }

        findViewById<Button>(R.id.btnPrev).setOnClickListener {
            if (viewType == "WEEKLY") {
                weekOffset--
                loadWeek()
            } else {
                monthOffset--
                loadMonth()
            }
        }

        findViewById<Button>(R.id.btnNext).setOnClickListener {
            if (viewType == "WEEKLY") {
                weekOffset++
                loadWeek()
            } else {
                monthOffset++
                loadMonth()
            }
        }

        findViewById<Button>(R.id.btnSaveDraft).setOnClickListener {
            Toast.makeText(this, "Draft Saved", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnSubmit).setOnClickListener {
            submitTimesheetToBackend()
        }
    }

    // ---------------- WEEK VIEW ----------------
    private fun loadWeek() {
        dayList.clear()

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.WEEK_OF_YEAR, weekOffset)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        for (i in 0 until 7) {
            val date = calendar.time
            dayList.add(
                DayEntryModel(
                    name = SimpleDateFormat("EEEE", Locale.ENGLISH).format(date),
                    dateStr = SimpleDateFormat("MMM d", Locale.ENGLISH).format(date),
                    hours = 0
                )
            )
            calendar.add(Calendar.DATE, 1)
        }

        val first = dayList.first().dateStr
        val last = dayList.last().dateStr
        tvRange.text = "$first - $last"

        adapter.notifyDataSetChanged()
        updateTotal()
    }

    // --------------- MONTH VIEW -----------------
    private fun loadMonth() {
        dayList.clear()

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, monthOffset)

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)

        calendar.set(year, month, 1)
        val totalDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        for (i in 1..totalDays) {
            calendar.set(year, month, i)
            val date = calendar.time

            dayList.add(
                DayEntryModel(
                    name = SimpleDateFormat("EEE", Locale.ENGLISH).format(date),
                    dateStr = SimpleDateFormat("MMM d", Locale.ENGLISH).format(date),
                    hours = 0
                )
            )
        }

        tvRange.text = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH).format(calendar.time)

        adapter.notifyDataSetChanged()
        updateTotal()
    }

    // -------------------- TOTAL ---------------------
    private fun updateTotal() {
        val total = dayList.sumOf { it.hours }
        tvTotal.text = "Total Hours: $total hours"
    }

    // ---------------- SEND TO BACKEND ----------------
    private fun submitTimesheetToBackend() {

        val api = ApiClient.getSecuredApi(this)

        val rangeParts = tvRange.text.toString().split(" - ")
        val startDate = convertToBackendDate(rangeParts[0])
        val endDate = if (rangeParts.size > 1) convertToBackendDate(rangeParts[1]) else startDate

        val project = spnProject.selectedItem.toString()
        val job = spnJob.selectedItem.toString()

        val entries = dayList.map {
            TimesheetEntryRequest(
                workDate = convertToBackendDate(it.dateStr),
                hoursWorked = it.hours.toDouble(),
                job = job,
                projectId = project
            )
        }

        val request = TimeSheetRequest(
            timesheetType = viewType,
            startDate = startDate,
            endDate = endDate,
            job = job,
            projectId = project,
            entries = entries
        )

        api.submitTimesheet(request)
            .enqueue(object : Callback<ApiResponse<Void>> {
                override fun onResponse(
                    call: Call<ApiResponse<Void>>,
                    response: Response<ApiResponse<Void>>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@TimeLogActivity, "Submitted Successfully!", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this@TimeLogActivity, "Submit failed!", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse<Void>>, t: Throwable) {
                    Toast.makeText(this@TimeLogActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun convertToBackendDate(uiDate: String): String {
        val inputFormat = SimpleDateFormat("MMM d", Locale.ENGLISH)
        val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val date = inputFormat.parse(uiDate)
        return outputFormat.format(date!!)
    }
}
