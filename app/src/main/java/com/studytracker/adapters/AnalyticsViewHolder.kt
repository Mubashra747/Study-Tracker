package com.studytracker.adapters

import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.studytracker.R
import com.studytracker.models.AnalyticsData

class AnalyticsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val tvSubject: TextView = itemView.findViewById(R.id.tvSubject)
    private val pbProgress: ProgressBar = itemView.findViewById(R.id.pbProgress)
    private val tvPercentage: TextView = itemView.findViewById(R.id.tvPercentage)

    fun bind(data: AnalyticsData) {
        tvSubject.text = data.subject
        pbProgress.progress = data.progress
        tvPercentage.text = "${data.progress}%"
    }
}
