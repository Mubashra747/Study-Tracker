package com.studytracker.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.studytracker.R
import com.studytracker.adapters.StudyTipsAdapter
import com.studytracker.repository.StudyTipsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TipsFragment : Fragment() {
    private lateinit var rvTips: RecyclerView
    private lateinit var tipsRepository: StudyTipsRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tips, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvTips = view.findViewById(R.id.rvTips)
        rvTips.layoutManager = LinearLayoutManager(requireContext())

        tipsRepository = StudyTipsRepository()

        fetchTips()
    }

    private fun fetchTips() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = tipsRepository.fetchStudyTips()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val tips = response.body()!!
                        rvTips.adapter = StudyTipsAdapter(tips)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Failed to fetch tips: ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
