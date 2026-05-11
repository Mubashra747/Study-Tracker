package com.studytracker.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.studytracker.R
import com.studytracker.models.StudyTip

class StudyTipsAdapter(
    private val tips: List<StudyTip>
) : RecyclerView.Adapter<StudyTipsAdapter.TipViewHolder>() {

    class TipViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvQuote: TextView = itemView.findViewById(R.id.tvQuote)
        val tvAuthor: TextView = itemView.findViewById(R.id.tvAuthor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TipViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_study_tip, parent, false)
        return TipViewHolder(view)
    }

    override fun onBindViewHolder(holder: TipViewHolder, position: Int) {
        val tip = tips[position]
        holder.tvQuote.text = tip.quote
        holder.tvAuthor.text = tip.author
    }

    override fun getItemCount(): Int = tips.size
}
