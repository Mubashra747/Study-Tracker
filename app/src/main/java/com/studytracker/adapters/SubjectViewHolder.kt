package com.studytracker.adapters

import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.studytracker.R
import com.studytracker.models.Subject

class SubjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val tvIcon: TextView = itemView.findViewById(R.id.ivIcon)
    private val tvName: TextView = itemView.findViewById(R.id.tvName)
    private val tvHours: TextView = itemView.findViewById(R.id.tvHours)
    private val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)

    fun bind(subject: Subject) {
        tvIcon.text = subject.emoji
        tvName.text = subject.name
        tvHours.text = subject.hours
        progressBar.progress = subject.progress
        try {
            progressBar.progressTintList = android.content.res.ColorStateList.valueOf(
                itemView.context.getColor(
                    itemView.context.resources.getIdentifier(
                        subject.color,
                        "color",
                        itemView.context.packageName
                    )
                )
            )
        } catch (e: Exception) {
            // Fallback
        }
    }
}
