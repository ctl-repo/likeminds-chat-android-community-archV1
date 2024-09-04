package com.likeminds.chatmm.buysellwidget.domain.util

import android.view.View
import androidx.lifecycle.MutableLiveData
import com.likeminds.chatmm.buysellwidget.data.ApiCallState
import retrofit2.Response

fun View.gone() {
    this.visibility = View.GONE
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun <T> handleApiResponse(response: Response<T>): ApiCallState<T> {
    return if (response.isSuccessful) {
        response.body()?.let {
            ApiCallState.Success(it)
        } ?: ApiCallState.Error("Empty response body")
    } else {
        val errorMessage = when (response.code()) {
            400 -> "Bad Request"
            401 -> "Unauthorized"
            404 -> "Not Found"
            500 -> "Internal Server Error"
            else -> "Unknown Error Having Response Code ${response.code()}"
        }
        ApiCallState.Error(errorMessage)
    }
}
