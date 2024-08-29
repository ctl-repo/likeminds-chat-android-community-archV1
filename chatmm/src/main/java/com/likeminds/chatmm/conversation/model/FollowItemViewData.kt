package com.likeminds.chatmm.conversation.model

import com.likeminds.chatmm.utils.model.BaseViewType
import com.likeminds.chatmm.utils.model.ITEM_CONVERSATION_FOLLOW

class FollowItemViewData : BaseViewType {
    override val viewType: Int
        get() = ITEM_CONVERSATION_FOLLOW
}