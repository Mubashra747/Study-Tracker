package com.studytracker.models

data class SubjectStats(
    val subjectName: String,
    val totalMinutes: Int,
    val averageScore: Int,
    val emoji: String,
    val color: String
)
