package com.studytracker.models

data class StudySession(
    val id: Long = -1,
    val subjectId: Long,
    val date: String,
    val duration: Int,
    val score: Int,
    val subjectName: String? = null,
    val syncStatus: Int = 0,
    val lastModified: Long = System.currentTimeMillis()
) {
    fun toFirestoreMap(): Map<String, Any?> = hashMapOf(
        "subjectId" to subjectId,
        "date" to date,
        "duration" to duration,
        "score" to score,
        "lastModified" to lastModified
    )
}
