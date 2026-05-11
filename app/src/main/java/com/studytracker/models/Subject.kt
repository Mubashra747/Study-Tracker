package com.studytracker.models

data class Subject(
    val id: Long = -1,
    val name: String,
    val emoji: String,
    val color: String,
    val hours: String = "0h",
    val progress: Int = 0,
    val syncStatus: Int = 0,
    val lastModified: Long = System.currentTimeMillis()
) {
    fun toFirestoreMap(): Map<String, Any?> = hashMapOf(
        "name" to name,
        "emoji" to emoji,
        "color" to color,
        "lastModified" to lastModified
    )
}
