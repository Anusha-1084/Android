package com.astinil.AndroidTimesheet.ui.activity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.astinil.AndroidTimesheet.R


class DayAdapter(
    private val days: MutableList<DayEntryModel>,
    private val onChange: () -> Unit
) : RecyclerView.Adapter<DayAdapter.DayVH>() {

    inner class DayVH(view: View) : RecyclerView.ViewHolder(view) {
        val tvDayName = view.findViewById<TextView>(R.id.tvDayName)
        val tvDate = view.findViewById<TextView>(R.id.tvDate)
        val etHours = view.findViewById<EditText>(R.id.etHours)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayVH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_day, parent, false)
        return DayVH(v)
    }

    override fun onBindViewHolder(holder: DayVH, position: Int) {
        val model = days[position]

        holder.tvDayName.text = model.name
        holder.tvDate.text = model.dateStr
        holder.etHours.setText(model.hours.toString())

        holder.etHours.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                model.hours = holder.etHours.text.toString().toIntOrNull() ?: 0
                onChange()
            }
        }
    }

    override fun getItemCount(): Int = days.size
}
