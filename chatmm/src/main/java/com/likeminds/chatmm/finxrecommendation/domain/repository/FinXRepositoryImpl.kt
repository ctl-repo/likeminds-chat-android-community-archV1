package com.likeminds.chatmm.finxrecommendation.domain.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.likeminds.chatmm.finxrecommendation.data.ApiCallState
import com.likeminds.chatmm.finxrecommendation.data.FinXRepository
import com.likeminds.chatmm.finxrecommendation.data.FinXService
import com.likeminds.chatmm.finxrecommendation.domain.model.finxsearchscrip.SearchScripRequest
import com.likeminds.chatmm.finxrecommendation.domain.model.finxsearchscrip.SearchScripResponse
import com.likeminds.chatmm.finxrecommendation.domain.model.finxmultipletouchlineV2.FinXMultiTouchlineRequest
import com.likeminds.chatmm.finxrecommendation.domain.model.finxmultipletouchlineV2.FinXMultiTouchlineResponse
import com.likeminds.chatmm.finxrecommendation.domain.util.handleApiResponse
import com.likeminds.chatmm.xapp.XLmcAppInstance

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
                XLmcAppInstance.sessionID.toString(),
                searchScripReq = SearchScripRequest(
                    noOfRecords = 20,
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
                XLmcAppInstance.sessionID.toString(),
                multiTouchlineReq = FinXMultiTouchlineRequest(
                    multipleTokens = "$segment@$token",
                    sessionId = XLmcAppInstance.sessionID.toString(),
                    userId = XLmcAppInstance.userID.toString()
                )
            )
            handleApiResponse(result)
        } catch (e: Exception) {
            ApiCallState.Error(e.message ?: "Unknown Error")
        }
        _multiTouchlineLiveData.postValue(apiCallState)
    }
}