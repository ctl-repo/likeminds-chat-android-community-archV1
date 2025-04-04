package com.likeminds.chatmm.finxrecommendation.domain.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class FinXRecommendationMetadata(

    //Recommendation Data
    @SerializedName("entryPrice") val entryPrice: String? = "",
    @SerializedName("slPrice") val slPrice: String? = "",
    @SerializedName("targetPrice") val targetPrice: String? = "",
    @SerializedName("isBuy") val isBuy: Boolean? = true,

    //Scrip Data
    @SerializedName("searchRsp") val searchRsp: FinxSmSearchApiRsp? = null,
    @SerializedName("customWidgetType") val customWidgetType: String? = "",

    //MF-Scheme Data
    @SerializedName("mfSchemeCode") val mfSchemeCode: Int? = 0,
    @SerializedName("mfSchemePlanCode") val mfSchemePlanCode: Int? = 0,

    //Redirection Type
    @SerializedName("redirectionTypeOrderPlace") val redirectionTypeOrderPlace: String? = "",
    @SerializedName("redirectionTypeCompanyPage") val redirectionTypeCompanyPage: String? = "",
) : Parcelable
