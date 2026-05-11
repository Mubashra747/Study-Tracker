package com.studytracker.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.studytracker.R

class DashboardFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        val cardStartSession = view.findViewById<CardView>(R.id.cardStartSession)
        val cardAnalytics = view.findViewById<CardView>(R.id.cardAnalytics)

        cardStartSession.setOnClickListener {
            navigateToTab(R.id.navItemTimer)
        }

        cardAnalytics.setOnClickListener {
            navigateToTab(R.id.navItemAnalytics)
        }

        return view
    }

    private fun navigateToTab(itemId: Int) {
        val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = itemId
    }
}
