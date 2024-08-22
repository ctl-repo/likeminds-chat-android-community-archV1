package com.likeminds.chatmm.buysellwidget.domain.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.likeminds.chatmm.buysellwidget.data.ApiCallState
import com.likeminds.chatmm.buysellwidget.data.FinXService
import com.likeminds.chatmm.buysellwidget.domain.model.SearchScripRequest
import com.likeminds.chatmm.buysellwidget.domain.model.SearchScripResponse
import com.likeminds.chatmm.xapp.XAppInstance

class FinXRepository(private val finXService: FinXService) {

    private val searchScripLiveData = MutableLiveData<ApiCallState<SearchScripResponse>>()

    val searchScripResponse: LiveData<ApiCallState<SearchScripResponse>>
        get() = searchScripLiveData


    suspend fun getSearchScrip(strScripName: String) {
        val result = finXService.searchScrip(
            XAppInstance.sessionID.toString(),
            searchScripReq = SearchScripRequest(
                noOfRecords = 5,
                startPos = 0,
                strScripName = strScripName,
                strSegment = ""
            )
        )
        if (result.body() != null) {
            searchScripLiveData.postValue(ApiCallState.Success(result.body()))
        } else {
            searchScripLiveData.postValue(ApiCallState.Error("Error occurred while fetching search scrip"))
        }
    }
}