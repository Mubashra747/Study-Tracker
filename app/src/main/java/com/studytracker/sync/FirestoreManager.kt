package com.studytracker.sync

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.studytracker.models.StudySession
import com.studytracker.models.Subject
import kotlinx.coroutines.tasks.await

class FirestoreManager {
    private val db = FirebaseFirestore.getInstance()

    suspend fun uploadSubject(userId: String, subject: Subject): Boolean {
        return try {
            db.collection("users")
                .document(userId)
                .collection("subjects")
                .document(subject.id.toString())
                .set(subject.toFirestoreMap(), SetOptions.merge())
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun uploadSession(userId: String, session: StudySession): Boolean {
        return try {
            db.collection("users")
                .document(userId)
                .collection("sessions")
                .document(session.id.toString())
                .set(session.toFirestoreMap(), SetOptions.merge())
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getRemoteSubjects(userId: String, lastSync: Long): List<Subject> {
        return try {
            val snapshot = db.collection("users")
                .document(userId)
                .collection("subjects")
                .whereGreaterThan("lastModified", lastSync)
                .get()
                .await()
            
            snapshot.documents.map { doc ->
                Subject(
                    id = doc.id.toLong(),
                    name = doc.getString("name") ?: "",
                    emoji = doc.getString("emoji") ?: "",
                    color = doc.getString("color") ?: "",
                    lastModified = doc.getLong("lastModified") ?: 0L,
                    syncStatus = 1
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getRemoteSessions(userId: String, lastSync: Long): List<StudySession> {
        return try {
            val snapshot = db.collection("users")
                .document(userId)
                .collection("sessions")
                .whereGreaterThan("lastModified", lastSync)
                .get()
                .await()
            
            snapshot.documents.map { doc ->
                StudySession(
                    id = doc.id.toLong(),
                    subjectId = doc.getLong("subjectId") ?: 0L,
                    date = doc.getString("date") ?: "",
                    duration = doc.getLong("duration")?.toInt() ?: 0,
                    score = doc.getLong("score")?.toInt() ?: 0,
                    lastModified = doc.getLong("lastModified") ?: 0L,
                    syncStatus = 1
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
