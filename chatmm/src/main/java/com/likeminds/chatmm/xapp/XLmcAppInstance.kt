package com.likeminds.chatmm.xapp

object XLmcAppInstance {
    var userId: String? = null
        private set
    var sessionId: String? = null
        private set
    var isResearchPostAllowed: Boolean = false
        private set

    fun setUserData(userId: String?, sessionId: String?, isResearchPostAllowed: Boolean) {
        this.userId = userId
        this.sessionId = sessionId
        this.isResearchPostAllowed = isResearchPostAllowed
    }

    var chatroomId: String? = null
        private set
    var communityId: String? = null
        private set
    var conversationId: String? = null
        private set

    fun setChatPersistData(chatroomId: String?, communityId: String?, conversationId: String?) {
        this.chatroomId = chatroomId
        this.communityId = communityId
        this.conversationId = conversationId
    }
}