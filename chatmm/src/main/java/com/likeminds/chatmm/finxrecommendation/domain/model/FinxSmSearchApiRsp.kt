package com.likeminds.chatmm.finxrecommendation.domain.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FinxSmSearchApiRsp(
    //@SerializedName("EqORFO") var eqORFO: Int? = null,
    @SerializedName("ExchangeSegment") var exchangeSegment: String? = null,
    //@SerializedName("Expiry") var expiry: Int? = null,
    @SerializedName("InstrumentName") var instrumentName: String? = null,
    //@SerializedName("IsIndex") var isIndex: Int? = null,
    @SerializedName("MarketLot") var marketLot: Int? = null,
    @SerializedName("OptionType") var optionType: String? = null,
    @SerializedName("PriceDivisor") var priceDivisor: Int? = null,
    @SerializedName("PriceTick") var priceTick: Int? = null,
    @SerializedName("sExpiry") var sExpiry: String? = null,
    @SerializedName("SecDesc") var secDesc: String? = null,
    @SerializedName("SecName") var secName: String? = null,
    @SerializedName("SegmentId") var segment: Int? = null,
    @SerializedName("Series") var series: String? = null,
    @SerializedName("StrikePrice") var strikePrice: Float? = null,
    @SerializedName("Symbol") var symbol: String? = null,
    @SerializedName("Token") var token: Int? = null,
    //@SerializedName("MWTag") var mwTag: String? = "",
    //@SerializedName("isAdded") var isAdded: Boolean = false,
):Parcelable {
    fun getScripName() = secName?.replace("|", " ") ?: symbol
}