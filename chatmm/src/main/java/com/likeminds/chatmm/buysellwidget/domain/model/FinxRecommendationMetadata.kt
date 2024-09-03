package com.likeminds.chatmm.buysellwidget.domain.model

import com.google.gson.annotations.SerializedName

data class FinxRecommendationMetadata(

    //Recommendation Data
    @SerializedName("entryPrice") val entryPrice: String? = "",
    @SerializedName("slPrice") val slPrice: String? = "",
    @SerializedName("targetPrice") val targetPrice: String? = "",
    @SerializedName("isBuy") val isBuy: Boolean? = true,

    //Scrip Data
    @SerializedName("searchRsp") val searchRsp: FinxSmSearchApiRsp,
)
