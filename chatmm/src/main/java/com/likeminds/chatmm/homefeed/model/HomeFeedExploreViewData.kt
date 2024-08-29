package com.likeminds.chatmm.homefeed.model

import android.os.Parcelable
import com.likeminds.chatmm.utils.model.BaseViewType
import com.likeminds.chatmm.utils.model.ITEM_HOME_FEED_EXPLORE
import kotlinx.parcelize.Parcelize

@Parcelize
class HomeFeedExploreViewData private constructor(
    val totalChatRooms: Int,
    val newChatRooms: Int
) : Parcelable, BaseViewType {

    override val viewType: Int
        get() = ITEM_HOME_FEED_EXPLORE

    class Builder {
        private var totalChatRooms: Int = 0
        private var newChatRooms: Int = 0

        fun totalChatRooms(totalChatRooms: Int) = apply {
            this.totalChatRooms = totalChatRooms
        }

        fun newChatRooms(newChatRooms: Int) = apply {
            this.newChatRooms = newChatRooms
        }

        fun build() = HomeFeedExploreViewData(totalChatRooms, newChatRooms)
    }

    fun toBuilder(): Builder {
        return Builder()
            .totalChatRooms(totalChatRooms)
            .newChatRooms(newChatRooms)
    }
}
