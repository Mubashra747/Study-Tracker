package com.studytracker.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.studytracker.R
import com.studytracker.models.StudySession
import com.studytracker.models.StudySessionWithSubject

class SessionAdapter(
    private val sessions: List<StudySessionWithSubject>,
    private val onItemClick: (StudySession) -> Unit
) : RecyclerView.Adapter<SessionAdapter.SessionViewHolder>() {

    class SessionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvSubject: TextView = itemView.findViewById(R.id.tvSubject)
        val tvDuration: TextView = itemView.findViewById(R.id.tvDuration)
        val tvScore: TextView = itemView.findViewById(R.id.tvScore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_session, parent, false)
        return SessionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        val item = sessions[position]
        val session = item.session
        
        holder.tvDate.text = session.date
        holder.tvSubject.text = "${item.emoji} ${item.subjectName}"
        holder.tvDuration.text = "${session.duration} mins"
        holder.tvScore.text = "Score: ${session.score}"

        holder.itemView.setOnClickListener { onItemClick(session) }
    }

    override fun getItemCount(): Int = sessions.size
}
