package com.likeminds.chatmm.finxrecommendation.domain.model.finxmultipletouchlineV2

import com.google.gson.annotations.SerializedName

data class FinXMtlResponse(
    @SerializedName("lMT") var alMt: ArrayList<FinXMultitouchline?>? = null
)