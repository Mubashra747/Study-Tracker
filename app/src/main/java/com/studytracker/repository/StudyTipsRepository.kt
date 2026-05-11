package com.studytracker.repository

import com.studytracker.api.RetrofitClient
import com.studytracker.models.StudyTip
import retrofit2.Response

class StudyTipsRepository {
    private val apiService = RetrofitClient.instance

    suspend fun fetchStudyTips(): Response<List<StudyTip>> {
        return apiService.getStudyTips()
    }
}
