package com.likeminds.chatmm.buysellwidget.data

interface FinXRepository {

    suspend fun getSearchScrip(strScripName: String)
}