package com.likeminds.chatmm.finxrecommendation.domain.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FinxRecommendationMetadata(

    //Recommendation Data
    @SerializedName("entryPrice") val entryPrice: String? = "",
    @SerializedName("slPrice") val slPrice: String? = "",
    @SerializedName("targetPrice") val targetPrice: String? = "",
    @SerializedName("isBuy") val isBuy: Boolean? = true,

    //Scrip Data
    @SerializedName("searchRsp") val searchRsp: FinxSmSearchApiRsp? = null,
    @SerializedName("customWidgetType") val customWidgetType: String? = ""
) : Parcelable
