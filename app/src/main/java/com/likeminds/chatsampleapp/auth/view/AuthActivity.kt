package com.likeminds.chatsampleapp.auth.view

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.dhaval2404.colorpicker.MaterialColorPickerDialog
import com.github.dhaval2404.colorpicker.model.ColorShape
import com.github.dhaval2404.colorpicker.model.ColorSwatch
import com.likeminds.chatmm.LMChatCore
import com.likeminds.chatmm.theme.model.LMChatTheme
import com.likeminds.chatmm.theme.model.LMFonts
import com.likeminds.chatmm.utils.Route
import com.likeminds.chatmm.utils.ViewUtils
import com.likeminds.chatsampleapp.ChatMMApplication
import com.likeminds.chatsampleapp.R
import com.likeminds.chatsampleapp.auth.model.LoginExtras
import com.likeminds.chatsampleapp.auth.util.AuthPreferences
import com.likeminds.chatsampleapp.databinding.ActivityAuthBinding

class AuthActivity : AppCompatActivity() {

    private val authPreferences: AuthPreferences by lazy {
        AuthPreferences(this)
    }

    private lateinit var binding: ActivityAuthBinding

    private var headerColor = DEFAULT_HEADER_COLOR
    private var buttonColor = DEFAULT_BUTTON_COLOR
    private var textLinkColor = DEFAULT_TEXT_LINK

    companion object {
        const val DEFAULT_HEADER_COLOR = "#FFFFFF"
        const val DEFAULT_BUTTON_COLOR = "#00897B"
        const val DEFAULT_TEXT_LINK = "#007AFF"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val isLoggedIn = authPreferences.getIsLoggedIn()

        if (isLoggedIn) {
            // user already logged in, navigate using deep linking or to [MainActivity]
            if (intent.data != null) {
                parseDeepLink()
            } else {
                navigateToAfterLoginActivity()
            }
        } else {
            // user is not logged in, ask login details.
            loginUser()
        }
    }

    private fun navigateToAfterLoginActivity() {
        val intent = Intent(this, AfterLoginActivity::class.java)
        val extras = LoginExtras(
            false,
            authPreferences.getUserName(),
            authPreferences.getUserId(),
        )
        intent.putExtra(ChatMMApplication.EXTRA_LOGIN, extras)
        startActivity(intent)
        finish()
    }

    // parses deep link to start corresponding activity
    private fun parseDeepLink() {
        //get intent for route
        val intent = Route.handleDeepLink(
            this,
            intent.data.toString()
        )
        startActivity(intent)
        finish()
    }

    // validates user input and save login details
    private fun loginUser() {
        binding.apply {
            val context = root.context

            ivBrandingButton.setOnClickListener {
                showColorDialog { colorRes, colorHex ->
                    buttonColor = colorHex
                    ivBrandingButton.backgroundTintList = ColorStateList.valueOf(colorRes)
                }
            }

            ivBrandingHeader.setOnClickListener {
                showColorDialog { colorRes, colorHex ->
                    headerColor = colorHex
                    ivBrandingHeader.backgroundTintList = ColorStateList.valueOf(colorRes)
                }
            }

            ivBrandingTextLink.setOnClickListener {
                showColorDialog { colorRes, colorHex ->
                    textLinkColor = colorHex
                    ivBrandingTextLink.backgroundTintList = ColorStateList.valueOf(colorRes)
                }
            }

            btnLogin.setOnClickListener {
                val apiKey = etApiKey.text.toString().trim()
                val userName = etUserName.text.toString().trim()
                val userId = etUserId.text.toString().trim()

                if (apiKey.isEmpty()) {
                    ViewUtils.showShortToast(
                        context,
                        getString(R.string.enter_api_key)
                    )
                    return@setOnClickListener
                }

                if (userName.isEmpty()) {
                    ViewUtils.showShortToast(
                        context,
                        getString(R.string.enter_user_name)
                    )
                    return@setOnClickListener
                }

                if (userId.isEmpty()) {
                    ViewUtils.showShortToast(
                        context,
                        getString(R.string.enter_user_id)
                    )
                    return@setOnClickListener
                }

                // save login details to auth prefs
                authPreferences.saveIsLoggedIn(true)
                authPreferences.saveApiKey(apiKey)
                authPreferences.saveUserName(userName)
                authPreferences.saveUserId(userId)

                authPreferences.saveHeaderColor(headerColor)
                authPreferences.saveButtonColor(buttonColor)
                authPreferences.saveTextLinkColor(textLinkColor)

                val brandingRequest = LMChatTheme.Builder()
                    .headerColor(headerColor)
                    .buttonsColor(buttonColor)
                    .textLinkColor(textLinkColor)
                    .fonts(
                        LMFonts.Builder()
                            .bold(com.likeminds.chatmm.R.font.lm_chat_roboto_bold)
                            .medium(com.likeminds.chatmm.R.font.lm_chat_roboto_regular)
                            .regular(com.likeminds.chatmm.R.font.lm_chat_roboto_regular)
                            .build()
                    )
                    .build()

                LMChatCore.setTheme(brandingRequest)
                navigateToAfterLoginActivity()
            }
        }
    }

    private fun showColorDialog(cb: (Int, String) -> Unit) {
        MaterialColorPickerDialog
            .Builder(this)
            .setTitle("Pick Theme")
            .setColorShape(ColorShape.SQAURE)
            .setColorSwatch(ColorSwatch._300)
            .setColorListener { colorRes, colorHex ->
                cb(colorRes, colorHex)
            }
            .show()
    }
}