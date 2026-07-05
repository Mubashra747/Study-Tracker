package com.studytracker.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.constraintlayout.helper.widget.Flow
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.studytracker.R
import com.studytracker.database.StudyDatabaseHelper
import com.studytracker.models.Subject
import com.studytracker.repository.StudyRepository
import com.studytracker.sync.FirestoreManager
import kotlinx.coroutines.launch
import java.util.Locale

class DashboardFragment : Fragment() {

    private lateinit var repository: StudyRepository
    private val auth = FirebaseAuth.getInstance()
    private val localUserId = 1L 

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dbHelper = StudyDatabaseHelper.getInstance(requireContext())
        repository = StudyRepository(dbHelper, FirestoreManager())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)
        setupClickListeners(view)
        setupProfile(view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refreshDashboardData(view)
    }

    private fun refreshDashboardData(view: View) {
        loadStats(view)
        loadRecentSubjects(view)
        loadSubjectTags(view)
    }

    private fun setupProfile(view: View) {
        val user = auth.currentUser
        val tvName = view.findViewById<TextView>(R.id.tvName)
        val tvSubtitle = view.findViewById<TextView>(R.id.tvSubtitle)

        if (user != null) {
            tvName.text = user.displayName ?: user.email?.substringBefore("@") ?: "Scholar"
            tvSubtitle.text = getString(R.string.settings_profile_sub)
        }
    }

    private fun loadStats(view: View) {
        val tvHoursVal = view.findViewById<TextView>(R.id.tvHoursVal)
        val tvSessionsVal = view.findViewById<TextView>(R.id.tvSessionsVal)
        val tvGoalsDesc = view.findViewById<TextView>(R.id.tvGoalsDesc)
        val progressGoal = view.findViewById<ProgressBar>(R.id.progressGoal)

        lifecycleScope.launch {
            val sessionsWithSubject = repository.getSessionsByUser(localUserId)
            val totalMinutes = sessionsWithSubject.sumOf { it.session.duration }
            val hours = totalMinutes / 60.0
            
            tvHoursVal?.text = String.format(Locale.getDefault(), "%.1f", hours)
            tvSessionsVal?.text = sessionsWithSubject.size.toString()

            val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val dailyGoal = prefs.getFloat("daily_goal_hours", 4.0f)
            
            tvGoalsDesc?.text = String.format(Locale.getDefault(), "%.1f / %.1f hrs done", hours, dailyGoal.toDouble())
            
            val progressPercentage = if (dailyGoal > 0) ((hours / dailyGoal) * 100).toInt() else 0
            progressGoal?.progress = progressPercentage.coerceAtMost(100)
            
            if (hours >= dailyGoal && dailyGoal > 0) {
                tvGoalsDesc?.setTextColor(requireContext().getColor(R.color.colorSuccess))
                progressGoal?.progressTintList = android.content.res.ColorStateList.valueOf(requireContext().getColor(R.color.colorSuccess))
            } else {
                tvGoalsDesc?.setTextColor(requireContext().getColor(R.color.colorTextSecondary))
                progressGoal?.progressTintList = android.content.res.ColorStateList.valueOf(requireContext().getColor(R.color.colorAccent))
            }
        }
    }

    private fun showSetGoalDialog() {
        val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val currentGoal = prefs.getFloat("daily_goal_hours", 4.0f)

        val input = EditText(requireContext())
        val padding = (24 * resources.displayMetrics.density).toInt()
        input.setPadding(padding, padding, padding, padding)
        input.setText(String.format(Locale.getDefault(), "%.1f", currentGoal))
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL

        AlertDialog.Builder(requireContext())
            .setTitle("Set Daily Study Goal")
            .setMessage("How many hours do you want to focus today?")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val newGoal = input.text.toString().toFloatOrNull() ?: currentGoal
                if (newGoal >= 0) {
                    prefs.edit { putFloat("daily_goal_hours", newGoal) }
                    Toast.makeText(context, "Goal updated to $newGoal hours!", Toast.LENGTH_SHORT).show()
                    view?.let { refreshDashboardData(it) }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAddSubjectDialog() {
        val input = EditText(requireContext())
        val padding = (24 * resources.displayMetrics.density).toInt()
        input.setPadding(padding, padding, padding, padding)
        input.hint = "e.g. Mathematics, Biology"

        AlertDialog.Builder(requireContext())
            .setTitle("Add New Subject")
            .setMessage("Enter the name of the subject:")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    lifecycleScope.launch {
                        val newSubject = Subject(
                            name = name,
                            emoji = "📚", // Default emoji
                            color = "#4F46E5" // Default color
                        )
                        repository.addSubject(newSubject, localUserId)
                        Toast.makeText(context, "$name added!", Toast.LENGTH_SHORT).show()
                        view?.let { refreshDashboardData(it) }
                    }
                } else {
                    Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun loadRecentSubjects(view: View) {
        val carouselRow = view.findViewById<LinearLayout>(R.id.carouselRow) ?: return
        
        lifecycleScope.launch {
            val stats = repository.getSubjectStats(localUserId)
            
            if (stats.isNotEmpty()) {
                carouselRow.removeAllViews()
                stats.take(10).forEach { stat ->
                    val cardView = layoutInflater.inflate(R.layout.item_subject, carouselRow, false)
                    
                    cardView.findViewById<TextView>(R.id.ivIcon).text = stat.emoji
                    cardView.findViewById<TextView>(R.id.tvName).text = stat.subjectName
                    
                    val hours = stat.totalMinutes / 60
                    val minutes = stat.totalMinutes % 60
                    cardView.findViewById<TextView>(R.id.tvHours).text = String.format(Locale.getDefault(), "%dh %02dm", hours, minutes)
                    
                    val progress = cardView.findViewById<ProgressBar>(R.id.progressBar)
                    progress.max = 300 
                    progress.progress = stat.totalMinutes.coerceAtMost(300)
                    
                    cardView.setOnClickListener {
                        startSubjectSession(stat.subjectName)
                    }
                    
                    carouselRow.addView(cardView)
                }
            }
        }
    }

    private fun loadSubjectTags(view: View) {
        val tagCloudContainer = view.findViewById<ConstraintLayout>(R.id.constraintTagCloud) ?: return
        val flowTags = view.findViewById<Flow>(R.id.flowTags) ?: return

        lifecycleScope.launch {
            val subjects = repository.getSubjectsByUser(localUserId)
            
            // Clear current chips
            tagCloudContainer.removeAllViews()
            tagCloudContainer.addView(flowTags)
            
            val viewIds = mutableListOf<Int>()
            
            subjects.forEach { subject ->
                val chip = TextView(requireContext()).apply {
                    id = View.generateViewId()
                    text = "${subject.emoji} ${subject.name}"
                    textSize = 12f
                    setTextColor(requireContext().getColor(R.color.colorChipText))
                    setBackgroundResource(R.drawable.bg_chip)
                    setPadding(32, 16, 32, 16)
                    setOnClickListener { startSubjectSession(subject.name) }
                }
                tagCloudContainer.addView(chip)
                viewIds.add(chip.id)
            }
            
            // Always add the "Add" chip at the end
            val addChip = TextView(requireContext()).apply {
                id = View.generateViewId()
                text = "+ Add Subject"
                textSize = 12f
                setTextColor(requireContext().getColor(R.color.colorAccent))
                setBackgroundResource(R.drawable.bg_chip)
                setPadding(32, 16, 32, 16)
                setOnClickListener { showAddSubjectDialog() }
            }
            tagCloudContainer.addView(addChip)
            viewIds.add(addChip.id)
            
            flowTags.referencedIds = viewIds.toIntArray()
        }
    }

    private fun setupClickListeners(view: View) {
        // Profile Card
        view.findViewById<TextView>(R.id.btnEdit)?.setOnClickListener {
            Toast.makeText(context, "Profile editing coming soon!", Toast.LENGTH_SHORT).show()
        }
        view.findViewById<TextView>(R.id.btnGoals)?.setOnClickListener {
            showSetGoalDialog()
        }
        view.findViewById<TextView>(R.id.btnShare)?.setOnClickListener {
            Toast.makeText(context, "Generating your progress report...", Toast.LENGTH_SHORT).show()
        }

        // Stats Bar
        view.findViewById<View>(R.id.statHours)?.setOnClickListener {
            navigateToTab(R.id.navItemAnalytics)
        }
        view.findViewById<View>(R.id.statSessions)?.setOnClickListener {
            navigateToTab(R.id.navItemHistory)
        }

        // Quick Start Grid
        view.findViewById<CardView>(R.id.cardStartSession)?.setOnClickListener {
            navigateToTab(R.id.navItemTimer)
        }
        view.findViewById<CardView>(R.id.cardAnalytics)?.setOnClickListener {
            navigateToTab(R.id.navItemAnalytics)
        }
        view.findViewById<CardView>(R.id.cardGoals)?.setOnClickListener {
            showSetGoalDialog()
        }
        view.findViewById<CardView>(R.id.cardAI)?.setOnClickListener {
            Toast.makeText(context, "AI Analysis: Peak focus time is 9 AM.", Toast.LENGTH_SHORT).show()
        }

        // AI Insight Card
        view.findViewById<CardView>(R.id.cardAiInsight)?.setOnClickListener {
            Toast.makeText(context, "Smart Tip: Study CS early for better retention.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startSubjectSession(subjectName: String) {
        Toast.makeText(context, "Resuming $subjectName session...", Toast.LENGTH_SHORT).show()
        navigateToTab(R.id.navItemTimer)
    }

    private fun navigateToTab(itemId: Int) {
        val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = itemId
    }
}
