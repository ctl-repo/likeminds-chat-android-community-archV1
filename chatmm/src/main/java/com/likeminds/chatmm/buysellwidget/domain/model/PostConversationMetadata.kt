package com.likeminds.chatmm.buysellwidget.domain.model

data class PostConversationMetadata(
    val entryPrice: String? = "",
    val slPrice: String? = "",
    val targetPrice: String? = "",
    val isBuy: Boolean? = true,
    val segment: Int? = 0,
    val token: Int? = 0,
    val symbol: String? = "",
    val secDesc: String? = ""
)
