package com.studytracker.api

import com.studytracker.models.StudyTip
import retrofit2.Response
import retrofit2.http.GET

interface StudyTipsApiService {
    @GET("quotes")
    suspend fun getStudyTips(): Response<List<StudyTip>>
}
