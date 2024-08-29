package com.likeminds.chatmm

import android.app.Application
import android.content.Context
import android.util.Log
import com.likeminds.chatmm.member.model.UserResponse
import com.likeminds.chatmm.member.util.UserResponseConvertor
import com.likeminds.chatmm.theme.model.LMChatTheme
import com.likeminds.chatmm.utils.user.LMChatUserMetaData
import com.likeminds.likemindschat.LMChatClient
import com.likeminds.likemindschat.user.model.InitiateUserRequest
import com.likeminds.likemindschat.user.model.ValidateUserRequest
import kotlinx.coroutines.*

object LMChatCore {
    /**
     * Call this function to configure SDK in client's app
     *
     * @param application: application instance of client's app
     * @param lmChatCoreCallback: callback to receive events from SDK
     * @param theme: branding request from client
     * @param domain: domain request from client
     * @param enablePushNotifications: enable/disable push notifications
     * @param deviceId: device id
     **/
    fun setup(
        application: Application,
        lmChatCoreCallback: LMChatCoreCallback? = null,
        theme: LMChatTheme? = null,
        domain: String? = null,
        enablePushNotifications: Boolean = false,
        deviceId: String? = null,
    ) {
        Log.d(SDKApplication.LOG_TAG, "LMChatCore setup called")

        //create object of SDKApplication
        val sdk = SDKApplication.getInstance()

        //call initSDKApplication to initialise sdk
        sdk.initSDKApplication(
            application,
            lmChatCoreCallback,
            theme,
            domain,
            enablePushNotifications,
            deviceId
        )
    }

    /**
     * Call this function to show chat without API Key Security
     * @param context: context of client's app
     * @param apiKey: api key of client
     * @param userName: name of user
     * @param uuid: uuid of user
     * @param success: callback to receive user response
     * @param error: callback to receive error message
     */
    fun showChat(
        context: Context,
        apiKey: String,
        userName: String,
        uuid: String,
        success: ((UserResponse) -> Unit)?,
        error: ((String?) -> Unit)?
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val lmChatClient = LMChatClient.getInstance()
            val tokens = lmChatClient.getTokens().data
            val lmChatUserMeta = LMChatUserMetaData.getInstance()
            val deviceId = lmChatUserMeta.deviceId

            if (tokens?.first == null || tokens.second == null) {
                val initiateUserRequest = InitiateUserRequest.Builder()
                    .apiKey(apiKey)
                    .userName(userName)
                    .userId(uuid)
                    .deviceId(deviceId)
                    .build()

                val response = lmChatClient.initiateUser(initiateUserRequest)
                if (response.success) {
                    success?.let { success ->
                        response.data?.let {
                            val userResponse = UserResponseConvertor.getUserResponse(it)

                            //perform post session actions
                            lmChatUserMeta.onPostUserSessionInit(context, userResponse)

                            //return user response
                            success(userResponse)
                        }
                    }
                } else {
                    error?.let { it(response.errorMessage) }
                }
            } else {
                showChat(context, tokens.first, tokens.second, success, error)
            }
        }
    }

    /**
     * Call this function to show chat with API Key Security
     * @param context: context of client's app
     * @param accessToken: access token of user
     * @param refreshToken: refresh token of user
     * @param success: callback to receive user response
     * @param error: callback to receive error message
     */
    fun showChat(
        context: Context,
        accessToken: String?,
        refreshToken: String?,
        success: ((UserResponse) -> Unit)?,
        error: ((String?) -> Unit)?
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val lmChatUserMeta = LMChatUserMetaData.getInstance()
            val deviceId = lmChatUserMeta.deviceId

            val lmFeedClient = LMChatClient.getInstance()
            val tokens = if (accessToken == null || refreshToken == null) {
                lmFeedClient.getTokens().data ?: Pair("", "")
            } else {
                Pair(accessToken, refreshToken)
            }

            val validateUserRequest = ValidateUserRequest.Builder()
                .accessToken(tokens.first)
                .refreshToken(tokens.second)
                .deviceId(deviceId)
                .build()

            val response = lmFeedClient.validateUser(validateUserRequest)
            if (response.success) {
                success?.let { success ->
                    response.data?.let {
                        val userResponse = UserResponseConvertor.getUserResponse(it)

                        lmChatUserMeta.onPostUserSessionInit(context, userResponse)

                        success(userResponse)
                    }
                }
            } else {
                error?.let { it(response.errorMessage) }
            }
        }
    }

    /**
     * Call this function to set theme for chat
     * @param lmChatTheme: branding request from client
     */
    fun setTheme(lmChatTheme: LMChatTheme) {
        val sdk = SDKApplication.getInstance()
        sdk.setupTheme(lmChatTheme)
    }
}