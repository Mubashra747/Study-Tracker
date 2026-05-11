package com.studytracker.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.studytracker.database.StudyDatabaseHelper
import com.studytracker.repository.StudyRepository

class SyncWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val firebaseUser = FirebaseAuth.getInstance().currentUser ?: return Result.failure()
        
        // In a real app, you'd fetch the localUserId from SharedPreferences or similar.
        val localUserId = 1L 
        
        val dbHelper = StudyDatabaseHelper.getInstance(applicationContext)
        val firestoreManager = FirestoreManager()
        val repository = StudyRepository(dbHelper, firestoreManager)

        // Get the last sync time from SharedPreferences
        val prefs = applicationContext.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)
        val lastSync = prefs.getLong("last_sync_timestamp", 0L)

        val success = repository.syncData(firebaseUser.uid, localUserId, lastSync)

        return if (success) {
            prefs.edit().putLong("last_sync_timestamp", System.currentTimeMillis()).apply()
            Result.success()
        } else {
            Result.retry()
        }
    }
}
