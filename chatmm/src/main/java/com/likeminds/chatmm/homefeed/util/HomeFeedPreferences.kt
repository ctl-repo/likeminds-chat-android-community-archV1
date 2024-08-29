package com.likeminds.chatmm.homefeed.util

import android.content.Context
import com.likeminds.chatmm.utils.sharedpreferences.BasePreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeFeedPreferences @Inject constructor(
    context: Context,
) : BasePreferences(HOME_FEED_PREFS, context) {
    companion object {
        const val HOME_FEED_PREFS = "home_feed_prefs"

        private const val API_KEY = "API_KEY"
        private const val IS_GUEST = "IS_GUEST"
        private const val HIDE_SECRET_CHATROOM_LOCK_ICON = "HIDE_SECRET_CHATROOM_LOCK_ICON"
        private const val SHOW_HOME_FEED_SHIMMER = "SHOW_HOME_FEED_SHIMMER"
    }

    fun setShowHomeFeedShimmer(setHomeFeedShimmer: Boolean) {
        putPreference(SHOW_HOME_FEED_SHIMMER, setHomeFeedShimmer)
    }

    fun getShowHomeFeedShimmer(): Boolean {
        return getPreference(SHOW_HOME_FEED_SHIMMER, false)
    }
}