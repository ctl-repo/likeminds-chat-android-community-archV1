package com.likeminds.chatmm.xapp.helper

import android.content.Context
import android.content.Intent
import com.likeminds.chatmm.chatroom.detail.model.ChatroomDetailExtras

interface AuthChecker{
    fun isUserLoggedIn():Boolean
}

interface FinXNavigator{
    fun startSplashActivity(context: Context,chatroomDetailExtras: ChatroomDetailExtras):Intent
}