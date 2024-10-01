package com.likeminds.chatmm.finxrecommendation.data

interface FinXRepository {

    suspend fun getSearchScrip(strScripName: String)

    suspend fun getMultitouchLine(token: Int?,segment: Int?)
}