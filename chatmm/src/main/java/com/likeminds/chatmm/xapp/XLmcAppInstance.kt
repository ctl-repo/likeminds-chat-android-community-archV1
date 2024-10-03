package com.likeminds.chatmm.xapp

object XLmcAppInstance {
    var userId: String? = null
        private set
    var sessionId: String? = null
        private set
    var researchPostAllowed: Boolean = false
        private set

    fun setUserData(userId: String?, sessionId: String?, researchPostAllowed: Boolean) {
        this.userId = userId
        this.sessionId = sessionId
        this.researchPostAllowed = researchPostAllowed
    }
}