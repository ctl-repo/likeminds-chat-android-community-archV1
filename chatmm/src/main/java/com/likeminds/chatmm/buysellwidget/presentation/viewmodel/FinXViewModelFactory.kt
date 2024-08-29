package com.likeminds.chatmm.buysellwidget.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.likeminds.chatmm.buysellwidget.domain.repository.FinXRepositoryImpl

class FinXViewModelFactory(private val repository: FinXRepositoryImpl) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FinXViewModel(repository) as T
    }


}