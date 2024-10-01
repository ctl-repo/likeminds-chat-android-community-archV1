package com.likeminds.chatmm.finxrecommendation.domain.model.finxsearchscrip

import com.google.gson.annotations.SerializedName
import com.likeminds.chatmm.finxrecommendation.domain.model.FinxSmSearchApiRsp

data class SearchScripResponse(
//    var Reason: String? = "",
//    var Response: List<Response?>? = listOf(),
//    var Status: String? = ""
    @SerializedName("Reason") var reason: String? = null,
    @SerializedName("Response") var response: List<FinxSmSearchApiRsp?>? = null,
    @SerializedName("Status") var status: String? = null
)