package com.likeminds.chatmm.buysellwidget.data

sealed class ApiCallState<T>(val data: T? = null, val errorMessage: String? = null) {

    class Loading<T>() : ApiCallState<T>()
    class Success<T>(data: T? = null) : ApiCallState<T>(data = data)
    class Error<T>(errorMessage: String) : ApiCallState<T>(errorMessage = errorMessage)
}