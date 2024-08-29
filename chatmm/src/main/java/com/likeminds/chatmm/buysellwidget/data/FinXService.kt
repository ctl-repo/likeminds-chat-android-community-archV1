package com.likeminds.chatmm.buysellwidget.data

import com.likeminds.chatmm.buysellwidget.domain.model.SearchScripRequest
import com.likeminds.chatmm.buysellwidget.domain.model.SearchScripResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface FinXService {

    @Headers("Content-Type: application/json")
    @POST("api/cm/ScripContract/Search")
    suspend fun searchScrip(
        @Header("Authorization") authorization: String,
        @Body searchScripReq: SearchScripRequest
    ): Response<SearchScripResponse>
}