package com.likeminds.chatmm.buysellwidget.domain.model.multipletouchlineV2

import com.google.gson.annotations.SerializedName

data class FinXMultiTouchlineRequest(
    @SerializedName("MultipleTokens") var multipleTokens: String,
    @SerializedName("SessionId") var sessionId: String,
    @SerializedName("UserId") var userId: String
)
