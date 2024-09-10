package com.likeminds.chatmm.buysellwidget.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.likeminds.chatmm.buysellwidget.data.ApiCallState
import com.likeminds.chatmm.buysellwidget.domain.model.SearchScripResponse
import com.likeminds.chatmm.buysellwidget.domain.model.multipletouchlineV2.FinXMultiTouchlineResponse
import com.likeminds.chatmm.buysellwidget.domain.repository.FinXRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FinXViewModel(private val repository: FinXRepositoryImpl) : ViewModel() {

    val searchScrip: LiveData<ApiCallState<SearchScripResponse>>
        get() = repository.searchScripResponse

    val multiTouchLineRes: LiveData<ApiCallState<FinXMultiTouchlineResponse>>
        get() = repository.multiTouchlineLiveData

    fun getSearchScrip(strScripName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getSearchScrip(strScripName)
        }
    }

    fun getMultiTouchLine(token: Int?, segment: Int?) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getMultitouchLine(token, segment)
        }
    }
}