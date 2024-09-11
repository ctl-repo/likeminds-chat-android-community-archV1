package com.likeminds.chatmm.buysellwidget.domain.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.likeminds.chatmm.buysellwidget.data.ApiCallState
import com.likeminds.chatmm.buysellwidget.data.FinXRepository
import com.likeminds.chatmm.buysellwidget.data.FinXService
import com.likeminds.chatmm.buysellwidget.domain.model.SearchScripRequest
import com.likeminds.chatmm.buysellwidget.domain.model.SearchScripResponse
import com.likeminds.chatmm.buysellwidget.domain.model.multipletouchlineV2.FinXMultiTouchlineRequest
import com.likeminds.chatmm.buysellwidget.domain.model.multipletouchlineV2.FinXMultiTouchlineResponse
import com.likeminds.chatmm.buysellwidget.domain.util.handleApiResponse
import com.likeminds.chatmm.xapp.XAppInstance

class FinXRepositoryImpl(private val finXService: FinXService) : FinXRepository {

    private val _searchScripLiveData = MutableLiveData<ApiCallState<SearchScripResponse>>()

    val searchScripResponse: LiveData<ApiCallState<SearchScripResponse>>
        get() = _searchScripLiveData

    private val _multiTouchlineLiveData =
        MutableLiveData<ApiCallState<FinXMultiTouchlineResponse>>()

    val multiTouchlineLiveData: LiveData<ApiCallState<FinXMultiTouchlineResponse>>
        get() = _multiTouchlineLiveData


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
        _searchScripLiveData.postValue(apiCallState)
    }

    override suspend fun getMultitouchLine(token: Int?, segment: Int?) {
        val apiCallState = try {
            val result = finXService.multitouchLine(
                XAppInstance.sessionID.toString(),
                multiTouchlineReq = FinXMultiTouchlineRequest(
                    multipleTokens = "$segment@$token",
                    sessionId = XAppInstance.sessionID.toString(),
                    userId = XAppInstance.userID.toString()
                )
            )
            handleApiResponse(result)
        } catch (e: Exception) {
            ApiCallState.Error(e.message ?: "Unknown Error")
        }
        _multiTouchlineLiveData.postValue(apiCallState)
    }
}