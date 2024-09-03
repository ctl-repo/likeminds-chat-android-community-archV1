package com.likeminds.chatsampleapp.likemindschat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.likeminds.chatmm.LMChatCore
import com.likeminds.chatmm.chat.view.LMChatFragment
import com.likeminds.chatmm.member.model.UserResponse
import com.likeminds.chatsampleapp.R
import com.likeminds.chatsampleapp.auth.util.AuthPreferences

class LikeMindsChatActivity : AppCompatActivity() {

    private val authPreferences: AuthPreferences by lazy {
        AuthPreferences(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_likeminds_chat)

        val successCallback = { userResponse: UserResponse ->
            replaceFragment()
        }

        val errorCallback = { error: String? ->

        }
        LMChatCore.showChat(
            this,
            apiKey = authPreferences.getApiKey(),
            userName = authPreferences.getUserName(),
            uuid = authPreferences.getUserId(),
            successCallback,
            errorCallback
        )
    }

    private fun replaceFragment() {
        val containerViewId = R.id.frame_layout

        val chatFragment = LMChatFragment.getInstance()

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(containerViewId, chatFragment, containerViewId.toString())
        transaction.commit()
    }
}