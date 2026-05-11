package com.studytracker.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.studytracker.R
import com.studytracker.adapters.SessionAdapter
import com.studytracker.database.StudyDatabaseHelper
import com.studytracker.models.StudySessionWithSubject
import com.studytracker.repository.StudyRepository
import com.studytracker.sync.FirestoreManager
import kotlinx.coroutines.launch

class SessionListFragment : Fragment() {
    private lateinit var repository: StudyRepository
    private var sessions: List<StudySessionWithSubject> = emptyList()
    private val localUserId = 1L 

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize repository inside the fragment to prevent crashes on recreation
        val dbHelper = StudyDatabaseHelper.getInstance(requireContext())
        val firestoreManager = FirestoreManager()
        repository = StudyRepository(dbHelper, firestoreManager)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_session_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView: RecyclerView = view.findViewById(R.id.rvSessions)
        val searchView: SearchView = view.findViewById(R.id.searchView)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        loadSessions()

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchSessions(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { searchSessions(it) }
                return true
            }
        })
    }

    private fun loadSessions() {
        lifecycleScope.launch {
            sessions = repository.getSessionsByUser(localUserId)
            updateRecyclerView(sessions)
        }
    }

    private fun searchSessions(query: String) {
        lifecycleScope.launch {
            val filteredSessions = if (query.isEmpty()) {
                repository.getSessionsByUser(localUserId)
            } else {
                repository.searchSessionsBySubjectName(localUserId, query)
            }
            updateRecyclerView(filteredSessions)
        }
    }

    private fun updateRecyclerView(list: List<StudySessionWithSubject>) {
        val adapter = SessionAdapter(list) { session ->
            // Handle item click
        }
        view?.findViewById<RecyclerView>(R.id.rvSessions)?.adapter = adapter
    }
}
