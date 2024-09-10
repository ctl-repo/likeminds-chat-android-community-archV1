package com.likeminds.chatmm.buysellwidget.domain.model.multipletouchlineV2

import com.google.gson.annotations.SerializedName

data class FinXMtlResponse(
    @SerializedName("lMT") var alMt: ArrayList<FinXMultitouchline?>? = null
)