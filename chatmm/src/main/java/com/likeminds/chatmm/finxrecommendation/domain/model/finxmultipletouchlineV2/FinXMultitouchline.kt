package com.likeminds.chatmm.finxrecommendation.domain.model.finxmultipletouchlineV2

import com.google.gson.annotations.SerializedName

data class FinXMultitouchline(
    @SerializedName("BBP") var bbp: Int? = null,
    @SerializedName("BBQ") var bbq: Int? = null,
    @SerializedName("BSP") var bsp: Int? = null,
    @SerializedName("BSQ") var bsq: Int? = null,
    @SerializedName("LTP") var ltp: Int? = null,
    @SerializedName("LTQ") var ltq: Int? = null,
    @SerializedName("LTT") var ltt: String? = null,
    @SerializedName("LUT") var lut: String? = null,
    @SerializedName("ML") var ml: Int? = null,
    @SerializedName("OI") var oi: Long? = null,
    @SerializedName("PC") var pc: Int? = null,
    @SerializedName("PD") var pd: Int? = null,
    @SerializedName("POI") var pOI: Long? = null,
    @SerializedName("PT") var pt: Int? = null,
    @SerializedName("Seg") var segment: Int? = null,
    @SerializedName("Tok") var token: Int? = null
)