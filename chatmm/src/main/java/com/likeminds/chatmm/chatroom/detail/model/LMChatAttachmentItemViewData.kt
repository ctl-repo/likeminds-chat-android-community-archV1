package com.likeminds.chatmm.chatroom.detail.model

import android.os.Parcelable
import androidx.annotation.DrawableRes
import com.likeminds.chatmm.utils.model.BaseViewType
import com.likeminds.chatmm.utils.model.ITEM_ATTACHMENT_ITEM
import kotlinx.parcelize.Parcelize

@Parcelize
class LMChatAttachmentItemViewData private constructor(
    val attachmentName: String,
    @DrawableRes val attachmentIcon: Int,
    val attachmentType: String
) : Parcelable, BaseViewType {
    override val viewType: Int
        get() = ITEM_ATTACHMENT_ITEM

    fun toBuilder(): Builder {
        return Builder()
            .attachmentName(attachmentName)
            .attachmentIcon(attachmentIcon)
            .attachmentType(attachmentType)
    }

    class Builder {
        private var attachmentName: String = ""
        private var attachmentIcon: Int = 0
        private var attachmentType: String = ""

        fun attachmentName(attachmentName: String) = apply { this.attachmentName = attachmentName }
        fun attachmentIcon(@DrawableRes attachmentIcon: Int) = apply { this.attachmentIcon = attachmentIcon }
        fun attachmentType(attachmentType: String) = apply { this.attachmentType = attachmentType }

        fun build(): LMChatAttachmentItemViewData {
            return LMChatAttachmentItemViewData(
                attachmentName,
                attachmentIcon,
                attachmentType
            )
        }
    }
}