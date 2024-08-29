package com.likeminds.chatmm.buysellwidget.domain.model

import com.google.gson.annotations.SerializedName

data class SearchScripResponse(
//    var Reason: String? = "",
//    var Response: List<Response?>? = listOf(),
//    var Status: String? = ""
    @SerializedName("Reason") var reason: String? = null,
    @SerializedName("Response") var response: List<Response?>? = null,
    @SerializedName("Status") var status: String? = null
)