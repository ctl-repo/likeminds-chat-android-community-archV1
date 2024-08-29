package com.likeminds.chatmm.media.customviews

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.SimpleItemAnimator
import com.likeminds.chatmm.LMAnalytics
import com.likeminds.chatmm.chatroom.create.view.adapter.ChatroomItemAdapter
import com.likeminds.chatmm.chatroom.create.view.adapter.ChatroomItemAdapterListener
import com.likeminds.chatmm.chatroom.detail.model.ChatroomViewData
import com.likeminds.chatmm.chatroom.detail.view.adapter.ChatroomDetailAdapterListener
import com.likeminds.chatmm.conversation.model.AttachmentViewData
import com.likeminds.chatmm.conversation.model.ConversationViewData
import com.likeminds.chatmm.databinding.LayoutAudioBinding
import com.likeminds.chatmm.member.util.UserPreferences

class AudioView(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs),
    ChatroomItemAdapterListener {

    var binding = LayoutAudioBinding.inflate(
        LayoutInflater.from(context),
        this,
        true
    )

    private val attachments = arrayListOf<AttachmentViewData>()

    private var adapter: ChatroomItemAdapter? = null

    private lateinit var chatroomAdapterListener: ChatroomDetailAdapterListener
    private var mediaActionVisible = false
    private var mediaUploadFailed = false
    private var isProgressFocussed = false

    private lateinit var userPreferences: UserPreferences

    fun initialize(
        userPreferences: UserPreferences,
        attachments: List<AttachmentViewData>,
        chatroomAdapterListener: ChatroomDetailAdapterListener,
        mediaActionVisible: Boolean,
        mediaUploadFailed: Boolean = false,
    ) {
        this.userPreferences = userPreferences
        this.attachments.clear()
        this.attachments.addAll(attachments)
        this.chatroomAdapterListener = chatroomAdapterListener
        this.mediaActionVisible = mediaActionVisible
        this.mediaUploadFailed = mediaUploadFailed
        initRecyclerView()
    }

    private fun initRecyclerView() {
        adapter = ChatroomItemAdapter(
            userPreferences,
            chatroomItemAdapterListener = this@AudioView
        )
        adapter?.replace(attachments.toList())
        (binding.rvAudio.itemAnimator as SimpleItemAnimator).supportsChangeAnimations =
            false
        binding.rvAudio.adapter = adapter
    }

    fun replaceList(attachments: List<AttachmentViewData>) {
        this.attachments.clear()
        this.attachments.addAll(attachments)
        adapter?.replace(attachments)
    }

    override fun onLongPressConversation(
        conversation: ConversationViewData,
        itemPosition: Int
    ) {
        chatroomAdapterListener.onLongPressConversation(
            conversation,
            itemPosition,
            LMAnalytics.Source.MESSAGE_REACTIONS_FROM_LONG_PRESS
        )
    }

    override fun onLongPressChatRoom(chatRoom: ChatroomViewData, itemPosition: Int) {
        chatroomAdapterListener.onLongPressChatRoom(chatRoom, itemPosition)
    }

    override fun isMediaActionVisible(): Boolean {
        return mediaActionVisible
    }

    override fun isMediaUploadFailed(): Boolean {
        return mediaUploadFailed
    }

    override fun onAudioConversationActionClicked(
        data: AttachmentViewData,
        position: String,
        childPosition: Int,
        progress: Int
    ) {
        chatroomAdapterListener.onAudioConversationActionClicked(
            data,
            position,
            childPosition,
            progress
        )
    }

    override fun isSelectionEnabled(): Boolean {
        return chatroomAdapterListener.isSelectionEnabled()
    }

    override fun onConversationSeekBarChanged(
        progress: Int,
        attachmentViewData: AttachmentViewData,
        parentConversationId: String,
        childPosition: Int
    ) {
        chatroomAdapterListener.onConversationSeekbarChanged(
            progress,
            attachmentViewData,
            parentConversationId,
            childPosition
        )
    }

    override fun onSeekBarFocussed(value: Boolean) {
        isProgressFocussed = value
    }

    fun isProgressFocussed(): Boolean {
        return isProgressFocussed
    }
}