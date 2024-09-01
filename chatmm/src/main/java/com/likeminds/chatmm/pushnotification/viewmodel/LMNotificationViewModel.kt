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
import com.likeminds.likemindschat.chatroom.model.Chatroom
import com.likeminds.likemindschat.community.model.Member
import com.likeminds.likemindschat.conversation.model.Conversation
import com.likeminds.likemindschat.notification.model.GetUnreadConversationNotificationRequest
import com.likeminds.likemindschat.user.model.SDKClientInfo
import javax.inject.Inject

class LMNotificationViewModel @Inject constructor(
    applicationContext: Application,
    private val userPreferences: UserPreferences
) : AndroidViewModel(applicationContext) {

    private val lmChatClient = LMChatClient.getInstance()

    fun fetchUnreadConversations(
        unreadFollowNotification: ChatroomNotificationViewData,
        cb: (List<ChatroomNotificationViewData>?) -> Unit
    ) {
        viewModelScope.launchIO {
            val request = GetUnreadConversationNotificationRequest.Builder()
                .chatroom(
                    Chatroom.Builder()
                        .id(unreadFollowNotification.chatroomId)
                        .title(unreadFollowNotification.chatroomTitle)
                        .header(unreadFollowNotification.chatroomName)
                        .communityName(unreadFollowNotification.communityName)
                        .communityId(unreadFollowNotification.communityId.toString())
                        .build()
                )
                .chatroomLastConversation(
                    Conversation.Builder()
                        .id(unreadFollowNotification.chatroomLastConversationId)
                        .answer(unreadFollowNotification.chatroomLastConversation ?: "")
                        .createdEpoch(unreadFollowNotification.chatroomLastConversationTimestamp)
                        .chatroomId(unreadFollowNotification.chatroomId)
                        .communityId(unreadFollowNotification.communityId.toString())
                        .member(
                            Member.Builder().id("1234").uuid("1234")
                                .imageUrl("https://www.google.com").userUniqueId("1234").name(
                                unreadFollowNotification.chatroomLastConversationUserName ?: ""
                            ).sdkClientInfo(
                                SDKClientInfo(
                                    unreadFollowNotification.communityId,
                                    "1234",
                                    "1234",
                                    "1234"
                                )
                            ).build()
                        )
                        .build()
                )
                .build()

            val response = lmChatClient.getUnreadConversationNotification(request)
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