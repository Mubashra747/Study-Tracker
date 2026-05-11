package com.studytracker.adapters

import android.graphics.Color
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.studytracker.R
import com.studytracker.models.AnalyticsData

class AnalyticsAdapter(private val dataList: List<AnalyticsData>) :
    RecyclerView.Adapter<AnalyticsAdapter.AnalyticsViewHolder>() {

    class AnalyticsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvSubject: TextView = view.findViewById(R.id.tvSubject)
        val pbProgress: ProgressBar = view.findViewById(R.id.pbProgress)
        val tvPercentage: TextView = view.findViewById(R.id.tvPercentage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnalyticsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_analytics, parent, false)
        return AnalyticsViewHolder(view)
    }

    override fun onBindViewHolder(holder: AnalyticsViewHolder, position: Int) {
        val item = dataList[position]
        holder.tvSubject.text = item.subject
        holder.pbProgress.progress = item.progress
        holder.tvPercentage.text = "${item.progress}%"

        try {
            val color = Color.parseColor(item.color)
            holder.pbProgress.progressTintList = ColorStateList.valueOf(color)
            holder.tvPercentage.setTextColor(color)
        } catch (e: Exception) {
            // Fallback if color string is invalid
        }
    }

    override fun getItemCount(): Int = dataList.size
}
