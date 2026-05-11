package com.studytracker.adapters

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.studytracker.R
import com.studytracker.models.StudySession
import com.studytracker.models.StudySessionWithSubject

class SessionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
    private val tvSubject: TextView = itemView.findViewById(R.id.tvSubject)
    private val tvDuration: TextView = itemView.findViewById(R.id.tvDuration)
    private val tvScore: TextView = itemView.findViewById(R.id.tvScore)

    fun bind(item: StudySessionWithSubject) {
        val session = item.session
        tvDate.text = session.date
        tvSubject.text = "${item.emoji} ${item.subjectName}"
        tvDuration.text = "${session.duration} mins"
        tvScore.text = "Score: ${session.score}"
    }
}