package com.likeminds.chatmm.chatroom.detail.view.adapter

import com.likeminds.chatmm.chatroom.detail.model.ChatroomViewData
import com.likeminds.chatmm.chatroom.detail.view.adapter.databinder.ChatroomDateItemViewDataBinder
import com.likeminds.chatmm.chatroom.detail.view.adapter.databinder.ChatroomItemViewDataBinder
import com.likeminds.chatmm.conversation.model.AttachmentViewData
import com.likeminds.chatmm.conversation.model.ConversationViewData
import com.likeminds.chatmm.conversation.model.ReportLinkExtras
import com.likeminds.chatmm.conversation.view.adapter.databinder.ChatroomAnnouncementItemViewDataBinder
import com.likeminds.chatmm.conversation.view.adapter.databinder.ConversationActionItemViewDataBinder
import com.likeminds.chatmm.conversation.view.adapter.databinder.ConversationAudioItemViewBinder
import com.likeminds.chatmm.conversation.view.adapter.databinder.ConversationAutoFollowedTaggedActionViewDataBinder
import com.likeminds.chatmm.conversation.view.adapter.databinder.ConversationFollowItemViewDataBinder
import com.likeminds.chatmm.conversation.view.adapter.databinder.ConversationItemViewDataBinder
import com.likeminds.chatmm.conversation.view.adapter.databinder.ConversationLinkItemViewDataBinder
import com.likeminds.chatmm.conversation.view.adapter.databinder.ConversationListShimmerViewDataBinder
import com.likeminds.chatmm.conversation.view.adapter.databinder.ConversationMultipleDocumentViewDataBinder
import com.likeminds.chatmm.conversation.view.adapter.databinder.ConversationMultipleMediaItemViewDataBinder
import com.likeminds.chatmm.conversation.view.adapter.databinder.ConversationPollItemViewDataBinder
import com.likeminds.chatmm.conversation.view.adapter.databinder.ConversationSingleGifItemViewDataBinder
import com.likeminds.chatmm.conversation.view.adapter.databinder.ConversationSingleImageItemViewDataBinder
import com.likeminds.chatmm.conversation.view.adapter.databinder.ConversationSinglePdfItemViewDataBinder
import com.likeminds.chatmm.conversation.view.adapter.databinder.ConversationSingleVideoItemViewDataBinder
import com.likeminds.chatmm.conversation.view.adapter.databinder.ConversationVoiceNoteItemViewDataBinder
import com.likeminds.chatmm.conversation.view.adapter.databinder.customwidgets.ConversationDMWidgetAItemViewDataBinder
import com.likeminds.chatmm.conversation.view.adapter.databinder.customwidgets.ConversationDMWidgetBItemViewDataBinder
import com.likeminds.chatmm.conversation.view.adapter.databinder.customwidgets.ConversationGroupChatWidgetAItemViewDataBinder
import com.likeminds.chatmm.conversation.view.adapter.databinder.customwidgets.ConversationGroupChatWidgetBItemViewDataBinder
import com.likeminds.chatmm.member.model.MemberViewData
import com.likeminds.chatmm.member.util.UserPreferences
import com.likeminds.chatmm.polls.model.PollViewData
import com.likeminds.chatmm.reactions.util.ReactionsPreferences
import com.likeminds.chatmm.utils.SDKPreferences
import com.likeminds.chatmm.utils.ValueUtils.getItemInList
import com.likeminds.chatmm.utils.customview.BaseRecyclerAdapter
import com.likeminds.chatmm.utils.customview.DataBoundViewHolder
import com.likeminds.chatmm.utils.customview.ViewDataBinder
import com.likeminds.chatmm.utils.model.BaseViewType
import com.likeminds.chatmm.utils.model.ITEM_CONVERSATION_AUTO_FOLLOWED_TAGGED_CHAT_ROOM
import com.likeminds.chatmm.utils.model.ITEM_CONVERSATION_FOLLOW
import com.likeminds.likemindschat.user.model.MemberBlockState
import java.util.UUID

class ChatroomDetailAdapter constructor(
    val sdkPreferences: SDKPreferences,
    val userPreferences: UserPreferences,
    val reactionsPreferences: ReactionsPreferences,
    val listener: ChatroomDetailAdapterListener,
) : BaseRecyclerAdapter<BaseViewType>() {
    init {
        initViewDataBinders()
    }

    fun getGraphicViewTypes(): Array<Int> {
        return arrayOf(
            ITEM_CONVERSATION_FOLLOW,
            ITEM_CONVERSATION_AUTO_FOLLOWED_TAGGED_CHAT_ROOM,
        )
    }

    override fun getSupportedViewDataBinder(): MutableList<ViewDataBinder<*, *>> {
        val viewDataBinders = ArrayList<ViewDataBinder<*, *>>(18)

        val chatroomItemViewDataBinder =
            ChatroomItemViewDataBinder(
                userPreferences,
                reactionsPreferences,
                listener
            )
        viewDataBinders.add(chatroomItemViewDataBinder)

        val chatroomAnnouncementItemViewDataBinder =
            ChatroomAnnouncementItemViewDataBinder(userPreferences, listener)
        viewDataBinders.add(chatroomAnnouncementItemViewDataBinder)

        val conversationMultipleDocumentViewDataBinder =
            ConversationMultipleDocumentViewDataBinder(userPreferences, listener)
        viewDataBinders.add(conversationMultipleDocumentViewDataBinder)

        val conversationActionItemViewDataBinder =
            ConversationActionItemViewDataBinder(userPreferences, listener)
        viewDataBinders.add(conversationActionItemViewDataBinder)

        val conversationItemViewDataBinder =
            ConversationItemViewDataBinder(
                userPreferences,
                reactionsPreferences,
                listener
            )
        viewDataBinders.add(conversationItemViewDataBinder)

        val conversationFollowItemViewDataBinder = ConversationFollowItemViewDataBinder(listener)
        viewDataBinders.add(conversationFollowItemViewDataBinder)

        val conversationAutoFollowedTaggedActionViewDataBinder =
            ConversationAutoFollowedTaggedActionViewDataBinder(listener)
        viewDataBinders.add(conversationAutoFollowedTaggedActionViewDataBinder)

        val conversationSingleImageItemViewDataBinder = ConversationSingleImageItemViewDataBinder(
            userPreferences,
            reactionsPreferences,
            listener
        )
        viewDataBinders.add(conversationSingleImageItemViewDataBinder)

        val conversationSingleGifItemViewDataBinder = ConversationSingleGifItemViewDataBinder(
            userPreferences,
            reactionsPreferences,
            listener
        )
        viewDataBinders.add(conversationSingleGifItemViewDataBinder)

        val conversationMultipleMediaItemViewDataBinder =
            ConversationMultipleMediaItemViewDataBinder(userPreferences, listener)
        viewDataBinders.add(conversationMultipleMediaItemViewDataBinder)

        val conversationSinglePdfItemViewDataBinder = ConversationSinglePdfItemViewDataBinder(
            userPreferences,
            reactionsPreferences,
            listener
        )
        viewDataBinders.add(conversationSinglePdfItemViewDataBinder)

        val conversationLinkItemViewDataBinder = ConversationLinkItemViewDataBinder(
            userPreferences,
            reactionsPreferences,
            listener
        )
        viewDataBinders.add(conversationLinkItemViewDataBinder)

        val conversationSingleVideoItemViewDataBinder = ConversationSingleVideoItemViewDataBinder(
            userPreferences,
            reactionsPreferences,
            listener
        )
        viewDataBinders.add(conversationSingleVideoItemViewDataBinder)

        val conversationAudioItemViewBinder =
            ConversationAudioItemViewBinder(userPreferences, listener)
        viewDataBinders.add(conversationAudioItemViewBinder)

        val conversationVoiceNoteItemViewDataBinder = ConversationVoiceNoteItemViewDataBinder(
            userPreferences,
            reactionsPreferences,
            listener
        )
        viewDataBinders.add(conversationVoiceNoteItemViewDataBinder)

        val conversationPollItemViewDataBinder =
            ConversationPollItemViewDataBinder(
                userPreferences,
                reactionsPreferences,
                listener
            )
        viewDataBinders.add(conversationPollItemViewDataBinder)

        val conversationListShimmerViewDataBinder = ConversationListShimmerViewDataBinder()
        viewDataBinders.add(conversationListShimmerViewDataBinder)

        val chatroomDateItemViewDataBinder = ChatroomDateItemViewDataBinder()
        viewDataBinders.add(chatroomDateItemViewDataBinder)

        //custom widgets view data binders
        val conversationGroupChatWidgetAItemViewDataBinder =
            ConversationGroupChatWidgetAItemViewDataBinder(
                userPreferences,
                reactionsPreferences,
                listener
            )
        viewDataBinders.add(conversationGroupChatWidgetAItemViewDataBinder)

        val conversationGroupChatWidgetBItemViewDataBinder =
            ConversationGroupChatWidgetBItemViewDataBinder(
                userPreferences,
                reactionsPreferences,
                listener
            )
        viewDataBinders.add(conversationGroupChatWidgetBItemViewDataBinder)

        val conversationDMWidgetAItemViewDataBinder =
            ConversationDMWidgetAItemViewDataBinder(userPreferences, reactionsPreferences, listener)
        viewDataBinders.add(conversationDMWidgetAItemViewDataBinder)

        val conversationDMWidgetBItemViewDataBinder =
            ConversationDMWidgetBItemViewDataBinder(userPreferences, reactionsPreferences, listener)
        viewDataBinders.add(conversationDMWidgetBItemViewDataBinder)

        return viewDataBinders
    }

    operator fun get(position: Int): BaseViewType? {
        return items().getItemInList(position)
    }
}

interface ChatroomDetailAdapterListener {
    fun follow(value: Boolean, source: String)
    fun getChatRoomType(): Int?
    fun getChatRoom(): ChatroomViewData?
    fun updateSeenFullConversation(position: Int, alreadySeenFullConversation: Boolean)
    fun isSelectionEnabled(): Boolean
    fun isChatRoomSelected(chatRoomId: String): Boolean
    fun isConversationSelected(conversationId: String): Boolean
    fun scrollToRepliedAnswer(conversation: ConversationViewData, repliedConversationId: String)
    fun scrollToRepliedChatroom(repliedChatRoomId: String)
    fun isScrolledConversation(position: Int): Boolean
    fun isReportedConversation(conversationId: String?): Boolean
    fun showActionDialogForReportedMessage()
    fun keepFollowingChatRoomClicked()
    fun unFollowChatRoomClicked()
    fun onAudioConversationActionClicked(
        data: AttachmentViewData,
        parentPositionId: String,
        childPosition: Int,
        progress: Int,
    )

    fun onLongPressConversation(
        conversation: ConversationViewData,
        itemPosition: Int,
        from: String,
    )

    fun onConversationSeekbarChanged(
        progress: Int,
        attachmentViewData: AttachmentViewData,
        parentConversationId: String,
        childPosition: Int,
    )

    fun onLongPressChatRoom(chatRoom: ChatroomViewData, itemPosition: Int)
    fun externalLinkClicked(
        conversationId: String?,
        url: String,
        reportLinkExtras: ReportLinkExtras?
    )

    fun onMultipleItemsExpanded(conversation: ConversationViewData, position: Int) {}

    fun observeMediaUpload(uuid: UUID, conversation: ConversationViewData)
    fun onRetryConversationMediaUpload(conversationId: String, attachmentCount: Int)
    fun onFailedConversationClick(conversation: ConversationViewData, itemPosition: Int)
    fun showMemberProfile(member: MemberViewData)

    /**
     * add this function for every navigation from chatroom
     * */
    fun onScreenChanged()
    fun onLinkClicked(conversationId: String?, url: String) {}
    fun onConversationPollSubmitClicked(
        conversation: ConversationViewData,
        pollViewDataList: List<PollViewData>,
    )

    fun dismissToastMessage()
    fun getBinding(conversationId: String?): DataBoundViewHolder<*>?
    fun addConversationPollOptionClicked(conversationId: String)
    fun onConversationMembersVotedCountClick(
        conversation: ConversationViewData,
        hasPollEnded: Boolean,
        isAnonymous: Boolean?,
        isCreator: Boolean,
    ) {
    }

    fun getPollRemainingTime(expiryTime: Long?): String?
    fun showToastMessage(message: String)
    fun showConversationPollVotersList(
        conversationId: String,
        pollId: String?,
        hasPollEnded: Boolean,
        toShowResult: Boolean?,
        positionOfPoll: Int,
    )

    fun reactionHintShown()
    fun emoticonGridClicked(
        conversationViewData: ConversationViewData,
        reaction: String?,
        position: Int,
    )

    fun chatroomEmoticonGridClicked(
        chatroomViewData: ChatroomViewData,
        reaction: String?,
        position: Int,
    )

    fun blockMember(index: Int, state: MemberBlockState) {}
}