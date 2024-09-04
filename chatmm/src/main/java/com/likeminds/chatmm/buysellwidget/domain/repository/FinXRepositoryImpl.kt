package com.likeminds.chatmm.buysellwidget.domain.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.likeminds.chatmm.buysellwidget.data.ApiCallState
import com.likeminds.chatmm.buysellwidget.data.FinXRepository
import com.likeminds.chatmm.buysellwidget.data.FinXService
import com.likeminds.chatmm.buysellwidget.domain.model.SearchScripRequest
import com.likeminds.chatmm.buysellwidget.domain.model.SearchScripResponse
import com.likeminds.chatmm.buysellwidget.domain.util.handleApiResponse
import com.likeminds.chatmm.xapp.XAppInstance

class FinXRepositoryImpl(private val finXService: FinXService) : FinXRepository {

    private val searchScripLiveData = MutableLiveData<ApiCallState<SearchScripResponse>>()

    val searchScripResponse: LiveData<ApiCallState<SearchScripResponse>>
        get() = searchScripLiveData


    override suspend fun getSearchScrip(strScripName: String) {
        val apiCallState = try {
            val result = finXService.searchScrip(
                XAppInstance.sessionID.toString(),
                searchScripReq = SearchScripRequest(
                    noOfRecords = 5,
                    startPos = 0,
                    strScripName = strScripName,
                    strSegment = ""
                )
            )
            handleApiResponse(result)
        } catch (e: Exception) {
            ApiCallState.Error(e.message ?: "Unknown Error")
        }
        searchScripLiveData.postValue(apiCallState)
    }
}