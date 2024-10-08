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
}