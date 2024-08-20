package com.likeminds.chatmm.buysellwidget.domain.model

import com.google.gson.annotations.SerializedName

data class SearchScripRequest(
    @SerializedName("NoOfRecords")
    var noOfRecords: Int? = null,
    @SerializedName("StartPos")
    var startPos: Int? = null,
    @SerializedName("strScripName")
    var strScripName: String? = null,
    @SerializedName("strSegment")
    var strSegment: String? = null
)
