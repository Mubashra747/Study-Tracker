package com.studytracker.models

import com.google.gson.annotations.SerializedName

data class StudyTip(
    @SerializedName("q")
    val quote: String,
    @SerializedName("a")
    val author: String
)
