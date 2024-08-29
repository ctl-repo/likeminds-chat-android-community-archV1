package com.likeminds.chatmm.buysellwidget.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.likeminds.chatmm.buysellwidget.data.ApiCallState
import com.likeminds.chatmm.buysellwidget.domain.model.SearchScripResponse
import com.likeminds.chatmm.buysellwidget.domain.repository.FinXRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FinXViewModel(private val repository: FinXRepositoryImpl) : ViewModel() {

    val searchScrip: LiveData<ApiCallState<SearchScripResponse>>
        get() = repository.searchScripResponse

    fun getSearchScrip(strScripName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getSearchScrip(strScripName)
        }
    }
}