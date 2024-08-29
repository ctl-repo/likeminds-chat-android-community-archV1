package com.likeminds.chatmm.pushnotification.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.likeminds.chatmm.LMAnalytics
import com.likeminds.chatmm.SDKApplication
import com.likeminds.chatmm.conversation.model.ConversationViewData
import com.likeminds.chatmm.member.util.UserPreferences
import com.likeminds.chatmm.pushnotification.model.ChatroomNotificationViewData
import com.likeminds.chatmm.utils.ViewDataConverter
import com.likeminds.chatmm.utils.coroutine.launchIO
import com.likeminds.likemindschat.LMChatClient
import javax.inject.Inject

class LMNotificationViewModel @Inject constructor(
    applicationContext: Application,
    private val userPreferences: UserPreferences
) : AndroidViewModel(applicationContext) {

    private val lmChatClient = LMChatClient.getInstance()

    fun fetchUnreadConversations(cb: (List<ChatroomNotificationViewData>?) -> Unit) {
        viewModelScope.launchIO {
            val response = lmChatClient.getUnreadConversationNotification()
            if (response.success) {
                val data = response.data?.unreadConversation ?: return@launchIO
                val conversations = ViewDataConverter.convertChatroomNotificationDataList(data)
                cb(conversations)
            } else {
                Log.e(
                    SDKApplication.LOG_TAG,
                    "unread notification failed: ${response.errorMessage}"
                )
            }
        }
    }

    fun sendChatroomResponded(
        conversation: ConversationViewData?,
        chatroomId: String,
        chatroomName: String
    ) {
        LMAnalytics.track(
            LMAnalytics.Events.CHATROOM_RESPONDED,
            mapOf(
                LMAnalytics.Keys.CHATROOM_ID to chatroomId,
                LMAnalytics.Keys.CHATROOM_NAME to chatroomName,
                "message_type" to "text",
                "message" to conversation?.answer,
                LMAnalytics.Keys.UUID to userPreferences.getUUID(),
                LMAnalytics.Keys.SOURCE to "notification"
            )
        )
    }
}