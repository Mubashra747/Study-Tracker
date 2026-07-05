package com.studytracker.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.studytracker.R
import com.studytracker.activities.LoginActivity

class SettingsFragment : Fragment() {

    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        val rgTheme = view.findViewById<RadioGroup>(R.id.rgTheme)
        val btnEdit = view.findViewById<TextView>(R.id.btnSettingsEdit)
        val relLogoutRow = view.findViewById<View>(R.id.relLogoutRow)
        
        setupProfileInfo(view)

        // --- Theme Selection ---
        when (AppCompatDelegate.getDefaultNightMode()) {
            AppCompatDelegate.MODE_NIGHT_NO -> rgTheme.check(R.id.rbLight)
            AppCompatDelegate.MODE_NIGHT_YES -> rgTheme.check(R.id.rbDark)
            else -> rgTheme.check(R.id.rbSystem)
        }

        rgTheme.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbLight -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                R.id.rbDark -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                R.id.rbSystem -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }

        // --- Edit Profile ---
        btnEdit.setOnClickListener {
            showEditProfileDialog(view)
        }

        // --- Logout ---
        relLogoutRow.setOnClickListener {
            showLogoutConfirmation()
        }

        return view
    }

    private fun setupProfileInfo(view: View) {
        val user = auth.currentUser
        val tvName = view.findViewById<TextView>(R.id.tvSettingsName)
        val tvSub = view.findViewById<TextView>(R.id.tvSettingsSub)

        if (user != null) {
            tvName.text = user.displayName ?: user.email?.substringBefore("@") ?: "Scholar"
            tvSub.text = user.email ?: getString(R.string.settings_profile_sub)
        }
    }

    private fun showEditProfileDialog(fragmentView: View) {
        val user = auth.currentUser ?: return
        val input = EditText(requireContext())
        val padding = (24 * resources.displayMetrics.density).toInt()
        input.setPadding(padding, padding, padding, padding)
        input.setText(user.displayName ?: "")
        input.hint = "Enter your name"

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Profile")
            .setMessage("Update your display name:")
            .setView(input)
            .setPositiveButton("Update") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty()) {
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(newName)
                        .build()

                    user.updateProfile(profileUpdates).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(context, "Profile updated!", Toast.LENGTH_SHORT).show()
                            setupProfileInfo(fragmentView)
                        } else {
                            Toast.makeText(context, "Update failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Logout") { _, _ ->
                auth.signOut()
                startActivity(Intent(requireContext(), LoginActivity::class.java))
                requireActivity().finish()
            }
            .setNegativeButton("Stay", null)
            .show()
    }
}
