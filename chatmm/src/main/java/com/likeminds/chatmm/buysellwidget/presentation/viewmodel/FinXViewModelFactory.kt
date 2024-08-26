package com.likeminds.chatmm.buysellwidget.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.likeminds.chatmm.buysellwidget.domain.repository.FinXRepository

class FinXViewModelFactory(private val repository: FinXRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FinXViewModel(repository) as T
    }


}