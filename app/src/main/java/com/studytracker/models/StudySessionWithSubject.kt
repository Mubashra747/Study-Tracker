package com.studytracker.models

data class StudySessionWithSubject(
    val session: StudySession,
    val subjectName: String,
    val emoji: String,
    val color: String
)
