package com.studytracker.models

data class User(
    val id: Long = -1,
    val name: String,
    val email: String,
    val level: Int = 1,
    val streak: Int = 0,
    val createdAt: String? = null
)
