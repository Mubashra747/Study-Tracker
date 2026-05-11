package com.studytracker.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.studytracker.R
import com.studytracker.fragments.AnalyticsFragment
import com.studytracker.fragments.DashboardFragment
import com.studytracker.fragments.SessionListFragment
import com.studytracker.fragments.SettingsFragment
import com.studytracker.fragments.TimerFragment
import com.studytracker.fragments.TipsFragment

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        auth = FirebaseAuth.getInstance()
        
        // Check if user is logged in
        if (auth.currentUser == null) {
            goToLogin()
            return
        }

        setContentView(R.layout.activity_main)

        val bottomNav: BottomNavigationView = findViewById(R.id.bottomNav)
        val ivToolbarSettings: ImageView = findViewById(R.id.ivToolbarSettings)

        // Default fragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.navHostFragment, DashboardFragment())
                .commit()
        }

        // Bottom nav item selection (Strictly max 5 items to avoid crash)
        bottomNav.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.navItemDashboard -> DashboardFragment()
                R.id.navItemTimer -> TimerFragment()
                R.id.navItemAnalytics -> AnalyticsFragment()
                R.id.navItemTips -> TipsFragment()
                R.id.navItemHistory -> SessionListFragment() // Removed repository passing
                else -> DashboardFragment()
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.navHostFragment, fragment)
                .commit()
            true
        }

        // Settings accessed via Toolbar icon
        ivToolbarSettings.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.navHostFragment, SettingsFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun goToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
