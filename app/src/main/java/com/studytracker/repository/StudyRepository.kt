package com.studytracker.repository

import android.util.Log
import com.studytracker.database.StudyDatabaseHelper
import com.studytracker.models.Subject
import com.studytracker.models.StudySession
import com.studytracker.models.StudySessionWithSubject
import com.studytracker.models.SubjectStats
import com.studytracker.sync.FirestoreManager

class StudyRepository(
    private val dbHelper: StudyDatabaseHelper,
    private val firestoreManager: FirestoreManager
) {

    // --- Subjects ---
    suspend fun addSubject(subject: Subject, userId: Long): Long = dbHelper.insertSubject(subject, userId)
    suspend fun getSubjectsByUser(userId: Long): List<Subject> = dbHelper.getSubjectsByUser(userId)
    suspend fun getSubjectById(subjectId: Long): Subject? = dbHelper.getSubjectById(subjectId)
    suspend fun updateSubject(subject: Subject): Boolean = dbHelper.updateSubject(subject)
    suspend fun deleteSubject(subjectId: Long): Boolean = dbHelper.deleteSubject(subjectId)

    // --- StudySessions ---
    suspend fun addStudySession(session: StudySession, userId: Long): Long = dbHelper.insertStudySession(session, userId)
    suspend fun getSessionsByUser(userId: Long): List<StudySessionWithSubject> = dbHelper.getSessionsByUser(userId)
    suspend fun updateStudySession(session: StudySession): Boolean = dbHelper.updateStudySession(session)
    suspend fun deleteStudySession(sessionId: Long): Boolean = dbHelper.deleteStudySession(sessionId)
    suspend fun searchSessionsBySubjectName(userId: Long, subjectName: String): List<StudySessionWithSubject> = 
        dbHelper.searchSessionsBySubjectName(userId, subjectName)

    // --- Analytics ---
    suspend fun getSubjectStats(userId: Long): List<SubjectStats> = dbHelper.getSubjectStatistics(userId)
    suspend fun getWeeklySummary(userId: Long): Map<String, Int> = dbHelper.getWeeklySummary(userId)

    // --- Sync Logic ---
    suspend fun syncData(firebaseUserId: String, localUserId: Long, lastSyncTimestamp: Long): Boolean {
        return try {
            // 1. Push Unsynced Subjects
            val unsyncedSubjects = dbHelper.getUnsyncedSubjects(localUserId)
            unsyncedSubjects.forEach { subject ->
                val success = firestoreManager.uploadSubject(firebaseUserId, subject)
                if (success) dbHelper.markSubjectSynced(subject.id)
            }

            // 2. Push Unsynced Sessions
            val unsyncedSessions = dbHelper.getUnsyncedSessions(localUserId)
            unsyncedSessions.forEach { session ->
                val success = firestoreManager.uploadSession(firebaseUserId, session)
                if (success) dbHelper.markSessionSynced(session.id)
            }

            // 3. Pull Remote Subjects
            val remoteSubjects = firestoreManager.getRemoteSubjects(firebaseUserId, lastSyncTimestamp)
            remoteSubjects.forEach { remoteSubject ->
                val localSubject = dbHelper.getSubjectById(remoteSubject.id)
                if (localSubject == null || remoteSubject.lastModified > localSubject.lastModified) {
                    dbHelper.insertSubject(remoteSubject, localUserId)
                }
            }

            // 4. Pull Remote Sessions
            val remoteSessions = firestoreManager.getRemoteSessions(firebaseUserId, lastSyncTimestamp)
            remoteSessions.forEach { remoteSession ->
                dbHelper.insertStudySession(remoteSession, localUserId)
            }

            true
        } catch (e: Exception) {
            Log.e("StudyRepository", "Sync failed", e)
            false
        }
    }
}
