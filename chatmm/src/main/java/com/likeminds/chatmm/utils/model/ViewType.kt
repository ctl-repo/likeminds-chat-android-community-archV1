package com.likeminds.chatmm.utils.model

import androidx.annotation.IntDef

//todo to remove unnecessary ViewTypes
const val ITEM_NONE = 0
const val ITEM_ACCESS_CONTROL_SINGLE_COMMUNITY = 1
const val ITEM_MEMBER = 2
const val ITEM_CONVERSATION_POLL = 3
const val ITEM_COMMUNITY_FEED_LIST_SHIMMER_VIEW = 4
const val ITEM_COMMUNITY_HOME_META = 5
const val ITEM_COMMUNITY_JOIN_QUESTION = 6
const val ITEM_CHATROOM_IMAGE = 7
const val ITEM_MEMBER_JOIN = 8
const val ITEM_MEMBER_PENDING = 9
const val ITEM_CREATED_BY = 10
const val ITEM_PROGRESS = 11
const val ITEM_CHATROOM_PDF = 13
const val ITEM_MEMBER_PENDING_MEMBER_ID = 14
const val ITEM_MEDIA_PICKER_FOLDER = 15
const val ITEM_ONBOARDING_ATTRIBUTE = 17
const val ITEM_ONBOARDING = 18
const val ITEM_ONBOARDING_MORE_TAG = 19
const val ITEM_ONBOARDING_CREATE_TAG = 20
const val ITEM_MEMBER_VERTICAL = 21
const val ITEM_COMMUNITY_JOIN_QUESTION_DROPDOWN = 22
const val ITEM_MORE = 23
const val ITEM_REPORT_TAG = 24
const val ITEM_CREATE_CHATROOM_POLL = 25
const val ITEM_CHATROOM_POLL = 26
const val ITEM_SELECT_OPTION_ADD_NEW = 27
const val ITEM_ACCESS_CONTROL_HEADER = 28
const val ITEM_EVENT_BANNER = 29
const val ITEM_ADD_PARTICIPANT = 30
const val ITEM_SECRET_PARTICIPANT = 31
const val ITEM_ADD_PARTICIPANT_OPTION = 32
const val ITEM_EVENT_MEMBER = 33

//not used in any adapter as of now
const val ITEM_COMMUNITY_CHATROOM_INVITE = 34
const val ITEM_ACCESS_CONTROL_COMMUNITY_HEADER = 35
const val ITEM_COMMUNITY_JOIN_SINGLE_QUESTION_DROPDOWN = 36
const val ITEM_ACCESS_CONTROL_BOTTOM = 37
const val ITEM_CHATROOM_LIST_MY = 38
const val ITEM_CHATROOM_LIST_OTHER = 39
const val ITEM_CHATROOM_LIST_EVENT = 40
const val ITEM_CHATROOM_LIST_POLL = 41
const val ITEM_MEDIA_PICKER_SINGLE = 42
const val ITEM_CREATE_WHATSAPP_COMMUNITY = 43
const val ITEM_CREATE_COMMUNITY_DROPDOWN = 44
const val ITEM_QUESTION_PARAGRAPH = 45
const val ITEM_QUESTION_FILE_UPLOAD = 46
const val ITEM_QUESTION_DATE_TIME = 47
const val ITEM_QUESTION_INTRODUCTION = 48
const val ITEM_QUESTION_PROFILE_LINK = 49
const val ITEM_QUESTION_MOBILE_NO = 50
const val ITEM_QUESTION_EMAIL_ID = 51
const val ITEM_QUESTION_SHORT_ANSWER = 52
const val ITEM_QUESTION_DROPDOWN_SINGLE = 53
const val ITEM_QUESTION_DROPDOWN_MULTIPLE = 54
const val ITEM_QUESTION_ADD = 55
const val ITEM_QUESTION_MASTER = 56
const val ITEM_APP_BAR = 57
const val ITEM_QUESTION_HEADER = 58
const val ITEM_MEMBER_DIRECTORY = 59
const val ITEM_MEMBER_DIRECTORY_FILTER = 60
const val ITEM_QUESTION_FILTER = 61
const val ITEM_MEMBER_PENDING_BOTTOM = 62
const val ITEM_MEDIA_PICKER_HEADER = 63
const val ITEM_MEDIA_PICKER_BROWSE = 64
const val ITEM_MEDIA_PICKER_DOCUMENT = 65
const val ITEM_CREATE_WHATSAPP_COMMUNITY_PURPOSE = 66
const val ITEM_CHATROOM_LIST_PURPOSE = 67
const val ITEM_CHATROOM_LIST_INTRODUCTION = 68
const val ITEM_CREATE_EVENT_BANNER = 69
const val ITEM_MEMBER_TAGGING = 70
const val ITEM_MEMBER_TAGGING_SELECTED = 71
const val ITEM_CHAT_ROOM_LINK = 72
const val ITEM_CHAT_ROOM = 73
const val ITEM_CHAT_ROOM_SINGLE_IMAGE = 75
const val ITEM_CHAT_ROOM_SINGLE_PDF = 76
const val ITEM_CHAT_ROOM_POLL = 77
const val ITEM_CHAT_MULTIPLE_MEDIA = 78
const val ITEM_CONVERSATION_ACTION = 79
const val ITEM_CONVERSATION = 80
const val ITEM_CONVERSATION_FOLLOW = 81
const val ITEM_PROGRESS_HORIZONTAL = 82
const val ITEM_CONVERSATION_SINGLE_IMAGE = 83
const val ITEM_IMAGE = 84
const val ITEM_MY_PENDING_COMMUNITIES = 85
const val ITEM_COMMUNITY_QUESTIONS_APP_BAR = 86
const val ITEM_PDF = 87
const val ITEM_IMAGE_EXPANDED = 88
const val ITEM_PDF_EXPANDED = 89
const val ITEM_CHATROOM_LIST_NEW_CHAT = 90
const val ITEM_MEDIA_SMALL = 91
const val ITEM_CONVERSATION_MULTIPLE_MEDIA = 92
const val ITEM_CONVERSATION_SINGLE_PDF = 93
const val ITEM_CHATROOM_GET_STARTED = 94
const val ITEM_ACTION_LEVEL_UP = 95
const val ITEM_ACTION_LEVEL_LOCKED = 96
const val ITEM_QUESTION_CLICK_SELECT = 97
const val ITEM_CREATE_COMMUNITY_DROP_SIDE = 98
const val ITEM_SELECT_COMMUNITY_CREATE_CHAT = 99
const val ITEM_CONVERSATION_NEW_CHAT_ROOM = 100
const val ITEM_CONVERSATION_LINK = 101
const val ITEM_ON_BOARDING_SAMPLES = 102
const val ITEM_CHATROOM_DATE = 103
const val ITEM_CHATROOM_POLL_MORE_OPTIONS = 104
const val ITEM_PRIMARY_EMAIL_VIEW = 106
const val ITEM_SECONDARY_EMAIL_LIST_VIEW = 107
const val ITEM_SECONDARY_EMAIL_VIEW = 108
const val ITEM_PRIMARY_PHONE_VIEW = 109
const val ITEM_SECONDARY_PHONE_LIST_VIEW = 110
const val ITEM_SECONDARY_PHONE_VIEW = 111
const val ITEM_FEEDBACK_ATTACHMENT_IMAGE_VIEW = 112
const val ITEM_FEEDBACK_ATTACHMENT_ADD_IMAGE_VIEW = 113
const val ITEM_COMMUNITY_DETAIL_COMMUNITY_MANAGERS_LIST = 114
const val ITEM_COMMUNITY_DETAIL_MEMBER_DIRECTORY_LIST = 115
const val ITEM_COMMUNITY_DETAIL_CHAT_ROOM_LIST = 116
const val ITEM_COMMUNITY_DETAIL_CHAT_ROOM = 117
const val ITEM_IMAGE_SWIPE = 118
const val ITEM_CHATROOM_PROFILE = 119
const val ITEM_QUESTION_ANSWER = 120
const val ITEM_COMMUNITY_VERTICAL = 121
const val ITEM_SELECT_OPTION = 122
const val ITEM_MY_COMMUNITY_HOME = 123
const val ITEM_HOME_CHAT_ROOM = 124
const val ITEM_DOCUMENT_SMALL = 125
const val ITEM_COMMUNITY_HOME_ACTION = 126
const val ITEM_EMPTY_VIEW = 128
const val ITEM_CONVERSATION_AUTO_FOLLOWED_TAGGED_CHAT_ROOM = 130
const val ITEM_CHAT_ROOM_INTRODUCTION = 131
const val ITEM_CHAT_ROOM_ANNOUNCEMENT = 132
const val ITEM_FEED_CHAT_ROOM_PREVIEW = 133
const val ITEM_CHAT_ROOM_PREVIEW = 134
const val ITEM_CONVERSATION_PREVIEW = 135
const val ITEM_SHARE_COMMUNITY = 136
const val ITEM_SHARE_CHAT_ROOM = 137
const val ITEM_MEMBER_ACTION = 138
const val ITEM_REASON_CHOOSE = 139
const val ITEM_COMMUNITY_MANAGEMENT_TOOL = 140
const val ITEM_MANAGEMENT_RIGHT_PERMISSION = 141
const val ITEM_MANAGEMENT_RIGHT_PERMISSION_SINGLE = 142
const val ITEM_MODERATION_HISTORY = 143
const val ITEM_CONVERSATION_PENDING_APPROVAL = 144
const val ITEM_REVIEW_REPORT_LIST = 145
const val ITEM_REVIEW_REPORT = 146
const val ITEM_CHATROOM_VIDEO = 147
const val ITEM_CONVERSATION_SINGLE_VIDEO = 148
const val ITEM_CHAT_ROOM_SINGLE_VIDEO = 149
const val ITEM_VIDEO = 150
const val ITEM_VIDEO_SWIPE = 151
const val ITEM_VIDEO_EXPANDED = 152
const val ITEM_CHAT_MULTIPLE_PDFS = 153
const val ITEM_CONTENT_HEADER_VIEW = 154
const val ITEM_HOME_LINE_BREAK_VIEW = 155
const val ITEM_HOME_MY_COMMUNITY_LIST_VIEW = 156
const val ITEM_HOME_BLANK_SPACE_VIEW = 157
const val ITEM_CHATROOM_ADD_MEDIA = 158
const val ITEM_HOME_MY_COMMUNITY_LIST_SHIMMER_VIEW = 159
const val ITEM_CHATROOM_LIST_SHIMMER_VIEW = 160
const val ITEM_SEARCH_CHATROOM = 161
const val ITEM_NO_SEARCH_RESULTS_VIEW = 162
const val ITEM_SEARCH_CONTENT_HEADER_VIEW = 163
const val ITEM_SEARCH_LINE_BREAK_VIEW = 164
const val ITEM_SEARCH_MESSAGE = 165
const val ITEM_CONVERSATION_LIST_SHIMMER_VIEW = 166
const val ITEM_MESSAGE_REACTION = 167
const val ITEM_MY_COMMUNITY_HOME_SINGLE = 168
const val ITEM_COMMUNITY_FEED_LIST_RENEW = 169
const val ITEM_CONVERSATION_SINGLE_GIF = 170
const val ITEM_ACCESS_CONTROL_CTA = 171
const val ITEM_SEARCH_TITLE = 172
const val ITEM_SINGLE_SHIMMER = 173
const val ITEM_MY_SUBSCRIPTION_ITEM = 174
const val ITEM_MY_SUBSCRIPTION_FOOTER = 175
const val ITEM_FAQ_HEADER = 176
const val ITEM_FAQ = 177
const val ITEM_REFERRAL_PLAN = 178
const val ITEM_MEMBERSHIP_HISTORY = 179
const val ITEM_DISCOVER_COMMUNITIES = 180
const val ITEM_OVERFLOW_MENU_ITEM = 181
const val ITEM_CONVERSATION_MULTIPLE_DOCUMENT = 182
const val ITEM_DOCUMENT = 183
const val ITEM_CHAT_MULTIPLE_DOCUMENT = 184
const val ITEM_MEDIA_PICKER_AUDIO = 185
const val ITEM_AUDIO_SMALL = 186
const val ITEM_CONVERSATION_AUDIO = 187
const val ITEM_AUDIO = 188
const val ITEM_CREATE_CHATROOM_AUDIO = 189
const val ITEM_CHAT_ROOM_AUDIO = 190
const val ITEM_COMMUNITY_FEED_AUDIO = 191
const val ITEM_COMMUNITY_FEED_ITEM_AUDIO = 192
const val ITEM_UPCOMING_EVENT = 193
const val ITEM_PAST_EVENT = 194
const val ITEM_CONVERSATION_VOICE_NOTE = 195
const val ITEM_MANAGEMENT_RIGHT_CONTENT_DOWNLOAD_SETTING = 196
const val ITEM_CONTENT_DOWNLOAD_SETTING = 197
const val ITEM_HINT_CONTENT_DOWNLOAD_SETTING = 198
const val ITEM_CHATROOM_UPDATE_APP = 199
const val ITEM_CONVERSATION_UPDATE_APP = 200
const val ITEM_DM_HOME_FEED = 201
const val ITEM_DM_FEED_DISCLAIMER = 202
const val ITEM_DIRECT_MESSAGES = 203
const val ITEM_DM_FILTER_COMMUNITY = 204
const val ITEM_DM_COMMUNITY_DETAIL = 205
const val ITEM_SETTINGS_TOP_VIEW = 206
const val ITEM_SETTINGS_SIMPLE_VIEW = 207
const val ITEM_SETTINGS_SWITCH_VIEW = 208
const val ITEM_SETTINGS_SWITCH_VIEW_WITH_MESSAGE = 209
const val ITEM_SETTINGS_BUTTON_VIEW = 210
const val ITEM_MEMBER_COUNT = 211
const val ITEM_SELECT_MEMBER_GROUP_OPTION = 212
const val ITEM_MEMBER_GROUP = 213
const val ITEM_MEMBER_GROUP_OPTION = 214
const val ITEM_VIEW_PARTICIPANTS = 215
const val ITEM_EVENT_ATTACHMENT = 216
const val ITEM_EVENT_ATTACHMENT_UPLOAD = 217
const val ITEM_COMMUNITY_SETTINGS_SWITCH = 218
const val ITEM_POLL_RESULT_USER = 219
const val ITEM_MEMBERSHIP_PLAN = 220
const val ITEM_ACCESS_CONTROL_MEMBERSHIP_BLOCKED = 221
const val ITEM_MEMBER_DIRECTORY_SEARCH = 222
const val ITEM_DRAWER_COMMUNITY = 223
const val ITEM_DRAWER_JOIN_COMMUNITY = 224
const val ITEM_HOME_FEED = 225
const val ITEM_DRAWER_LINE_BREAK_VIEW = 226
const val ITEM_DRAWER_BOTTOM_MENU = 227
const val ITEM_SUBSCRIPTION_RENEW = 228
const val ITEM_BRAND_COLOR = 229
const val ITEM_GET_STARTED_COMMUNITY = 230
const val ITEM_ANSWER_TYPE = 231
const val ITEM_QUESTION_LOCATION = 232
const val ITEM_QUESTION_FIELD = 233
const val ITEM_QUESTION_CJF_MESSAGE = 234
const val ITEM_QUESTION_DELETE = 235
const val ITEM_QUESTION_FIELD_EMAIL = 236
const val ITEM_EXPLORE = 251
const val ITEM_PROFILE_NO_CHATROOM_JOINED = 252
const val ITEM_QUESTION_ALIAS = 253

@IntDef(
    ITEM_NONE,
    ITEM_ACCESS_CONTROL_SINGLE_COMMUNITY,
    ITEM_MEMBER,
    ITEM_CONVERSATION_POLL,
    ITEM_COMMUNITY_FEED_LIST_SHIMMER_VIEW,
    ITEM_COMMUNITY_HOME_META,
    ITEM_COMMUNITY_JOIN_QUESTION,
    ITEM_MEMBER_PENDING,
    ITEM_CREATED_BY,
    ITEM_PROGRESS,
    ITEM_MEMBER_PENDING_MEMBER_ID,
    ITEM_MEDIA_PICKER_FOLDER,
    ITEM_ONBOARDING_ATTRIBUTE,
    ITEM_ONBOARDING,
    ITEM_ONBOARDING_MORE_TAG,
    ITEM_ONBOARDING_CREATE_TAG,
    ITEM_MEMBER_VERTICAL,
    ITEM_COMMUNITY_JOIN_QUESTION_DROPDOWN,
    ITEM_CONVERSATION_AUTO_FOLLOWED_TAGGED_CHAT_ROOM,
    ITEM_CHAT_ROOM_INTRODUCTION,
    ITEM_MORE,
    ITEM_REPORT_TAG,
    ITEM_CREATE_CHATROOM_POLL,
    ITEM_CHATROOM_POLL,
    ITEM_SELECT_OPTION_ADD_NEW,
    ITEM_ACCESS_CONTROL_HEADER,
    ITEM_EVENT_BANNER,
    ITEM_ADD_PARTICIPANT,
    ITEM_SECRET_PARTICIPANT,
    ITEM_ADD_PARTICIPANT_OPTION,
    ITEM_COMMUNITY_CHATROOM_INVITE,
    ITEM_ACCESS_CONTROL_COMMUNITY_HEADER,
    ITEM_COMMUNITY_JOIN_SINGLE_QUESTION_DROPDOWN,
    ITEM_ACCESS_CONTROL_BOTTOM,
    ITEM_CHATROOM_LIST_MY,
    ITEM_CHATROOM_LIST_OTHER,
    ITEM_CHATROOM_LIST_EVENT,
    ITEM_CHATROOM_LIST_POLL,
    ITEM_MEDIA_PICKER_SINGLE,
    ITEM_CREATE_WHATSAPP_COMMUNITY,
    ITEM_CREATE_COMMUNITY_DROPDOWN,
    ITEM_QUESTION_PARAGRAPH,
    ITEM_QUESTION_FILE_UPLOAD,
    ITEM_QUESTION_DATE_TIME,
    ITEM_QUESTION_INTRODUCTION,
    ITEM_QUESTION_PROFILE_LINK,
    ITEM_QUESTION_MOBILE_NO,
    ITEM_QUESTION_EMAIL_ID,
    ITEM_QUESTION_SHORT_ANSWER,
    ITEM_QUESTION_DROPDOWN_SINGLE,
    ITEM_QUESTION_DROPDOWN_MULTIPLE,
    ITEM_QUESTION_ADD,
    ITEM_QUESTION_MASTER,
    ITEM_APP_BAR,
    ITEM_QUESTION_HEADER,
    ITEM_MEMBER_DIRECTORY,
    ITEM_MEMBER_DIRECTORY_FILTER,
    ITEM_QUESTION_FILTER,
    ITEM_MEMBER_PENDING_BOTTOM,
    ITEM_MEDIA_PICKER_HEADER,
    ITEM_MEDIA_PICKER_BROWSE,
    ITEM_MEDIA_PICKER_DOCUMENT,
    ITEM_CREATE_WHATSAPP_COMMUNITY_PURPOSE,
    ITEM_CHATROOM_LIST_PURPOSE,
    ITEM_CHATROOM_LIST_INTRODUCTION,
    ITEM_CREATE_EVENT_BANNER,
    ITEM_MEMBER_TAGGING,
    ITEM_MEMBER_TAGGING_SELECTED,
    ITEM_CHAT_ROOM_LINK,
    ITEM_CHAT_ROOM,
    ITEM_CHAT_ROOM_SINGLE_IMAGE,
    ITEM_CHAT_ROOM_SINGLE_PDF,
    ITEM_CHAT_ROOM_POLL,
    ITEM_CHAT_MULTIPLE_MEDIA,
    ITEM_CONVERSATION_ACTION,
    ITEM_CONVERSATION,
    ITEM_CONVERSATION_FOLLOW,
    ITEM_PROGRESS_HORIZONTAL,
    ITEM_CONVERSATION_SINGLE_IMAGE,
    ITEM_IMAGE,
    ITEM_MY_PENDING_COMMUNITIES,
    ITEM_COMMUNITY_QUESTIONS_APP_BAR,
    ITEM_PDF,
    ITEM_IMAGE_EXPANDED,
    ITEM_PDF_EXPANDED,
    ITEM_CHATROOM_LIST_NEW_CHAT,
    ITEM_MEDIA_SMALL,
    ITEM_CONVERSATION_MULTIPLE_MEDIA,
    ITEM_CONVERSATION_SINGLE_PDF,
    ITEM_CHATROOM_GET_STARTED,
    ITEM_ACTION_LEVEL_UP,
    ITEM_ACTION_LEVEL_LOCKED,
    ITEM_QUESTION_CLICK_SELECT,
    ITEM_CREATE_COMMUNITY_DROP_SIDE,
    ITEM_SELECT_COMMUNITY_CREATE_CHAT,
    ITEM_CONVERSATION_NEW_CHAT_ROOM,
    ITEM_CONVERSATION_LINK,
    ITEM_ON_BOARDING_SAMPLES,
    ITEM_CHATROOM_DATE,
    ITEM_CHATROOM_POLL_MORE_OPTIONS,
    ITEM_PRIMARY_EMAIL_VIEW,
    ITEM_SECONDARY_EMAIL_LIST_VIEW,
    ITEM_SECONDARY_EMAIL_VIEW,
    ITEM_PRIMARY_PHONE_VIEW,
    ITEM_SECONDARY_PHONE_LIST_VIEW,
    ITEM_SECONDARY_PHONE_VIEW,
    ITEM_FEEDBACK_ATTACHMENT_IMAGE_VIEW,
    ITEM_FEEDBACK_ATTACHMENT_ADD_IMAGE_VIEW,
    ITEM_COMMUNITY_DETAIL_COMMUNITY_MANAGERS_LIST,
    ITEM_COMMUNITY_DETAIL_MEMBER_DIRECTORY_LIST,
    ITEM_COMMUNITY_DETAIL_CHAT_ROOM_LIST,
    ITEM_COMMUNITY_DETAIL_CHAT_ROOM,
    ITEM_IMAGE_SWIPE,
    ITEM_CHATROOM_PROFILE,
    ITEM_QUESTION_ANSWER,
    ITEM_COMMUNITY_VERTICAL,
    ITEM_SELECT_OPTION,
    ITEM_HOME_CHAT_ROOM,
    ITEM_DOCUMENT_SMALL,
    ITEM_COMMUNITY_HOME_ACTION,
    ITEM_EMPTY_VIEW,
    ITEM_CHAT_ROOM_ANNOUNCEMENT,
    ITEM_FEED_CHAT_ROOM_PREVIEW,
    ITEM_CHAT_ROOM_PREVIEW,
    ITEM_CONVERSATION_PREVIEW,
    ITEM_SHARE_COMMUNITY,
    ITEM_SHARE_CHAT_ROOM,
    ITEM_MEMBER_ACTION,
    ITEM_REASON_CHOOSE,
    ITEM_COMMUNITY_MANAGEMENT_TOOL,
    ITEM_MANAGEMENT_RIGHT_PERMISSION,
    ITEM_MANAGEMENT_RIGHT_PERMISSION_SINGLE,
    ITEM_MODERATION_HISTORY,
    ITEM_CONVERSATION_PENDING_APPROVAL,
    ITEM_REVIEW_REPORT_LIST,
    ITEM_REVIEW_REPORT,
    ITEM_CHATROOM_VIDEO,
    ITEM_CONVERSATION_SINGLE_VIDEO,
    ITEM_CHAT_ROOM_SINGLE_VIDEO,
    ITEM_VIDEO,
    ITEM_VIDEO_SWIPE,
    ITEM_VIDEO_EXPANDED,
    ITEM_CHAT_MULTIPLE_PDFS,
    ITEM_CONTENT_HEADER_VIEW,
    ITEM_HOME_LINE_BREAK_VIEW,
    ITEM_HOME_MY_COMMUNITY_LIST_VIEW,
    ITEM_HOME_BLANK_SPACE_VIEW,
    ITEM_CHATROOM_ADD_MEDIA,
    ITEM_HOME_MY_COMMUNITY_LIST_SHIMMER_VIEW,
    ITEM_CHATROOM_LIST_SHIMMER_VIEW,
    ITEM_SEARCH_CHATROOM,
    ITEM_NO_SEARCH_RESULTS_VIEW,
    ITEM_SEARCH_CONTENT_HEADER_VIEW,
    ITEM_SEARCH_LINE_BREAK_VIEW,
    ITEM_SEARCH_MESSAGE,
    ITEM_CONVERSATION_LIST_SHIMMER_VIEW,
    ITEM_MESSAGE_REACTION,
    ITEM_MY_COMMUNITY_HOME_SINGLE,
    ITEM_COMMUNITY_FEED_LIST_RENEW,
    ITEM_CONVERSATION_SINGLE_GIF,
    ITEM_ACCESS_CONTROL_CTA,
    ITEM_SEARCH_TITLE,
    ITEM_SINGLE_SHIMMER,
    ITEM_MY_SUBSCRIPTION_ITEM,
    ITEM_MY_SUBSCRIPTION_FOOTER,
    ITEM_FAQ_HEADER,
    ITEM_FAQ,
    ITEM_REFERRAL_PLAN,
    ITEM_MEMBERSHIP_HISTORY,
    ITEM_DISCOVER_COMMUNITIES,
    ITEM_OVERFLOW_MENU_ITEM,
    ITEM_CONVERSATION_MULTIPLE_DOCUMENT,
    ITEM_DOCUMENT,
    ITEM_CHAT_MULTIPLE_DOCUMENT,
    ITEM_MEDIA_PICKER_AUDIO,
    ITEM_AUDIO_SMALL,
    ITEM_CONVERSATION_AUDIO,
    ITEM_AUDIO,
    ITEM_CREATE_CHATROOM_AUDIO,
    ITEM_CHAT_ROOM_AUDIO,
    ITEM_COMMUNITY_FEED_AUDIO,
    ITEM_COMMUNITY_FEED_ITEM_AUDIO,
    ITEM_UPCOMING_EVENT,
    ITEM_PAST_EVENT,
    ITEM_CONVERSATION_VOICE_NOTE,
    ITEM_MANAGEMENT_RIGHT_CONTENT_DOWNLOAD_SETTING,
    ITEM_CONTENT_DOWNLOAD_SETTING,
    ITEM_HINT_CONTENT_DOWNLOAD_SETTING,
    ITEM_CHATROOM_UPDATE_APP,
    ITEM_CONVERSATION_UPDATE_APP,
    ITEM_DM_HOME_FEED,
    ITEM_DM_FEED_DISCLAIMER,
    ITEM_DIRECT_MESSAGES,
    ITEM_DM_FILTER_COMMUNITY,
    ITEM_DM_COMMUNITY_DETAIL,
    ITEM_EVENT_ATTACHMENT,
    ITEM_EVENT_ATTACHMENT_UPLOAD,
    ITEM_SETTINGS_TOP_VIEW,
    ITEM_SETTINGS_SIMPLE_VIEW,
    ITEM_SETTINGS_SWITCH_VIEW,
    ITEM_SETTINGS_SWITCH_VIEW_WITH_MESSAGE,
    ITEM_SETTINGS_BUTTON_VIEW,
    ITEM_MEMBER_COUNT,
    ITEM_SELECT_MEMBER_GROUP_OPTION,
    ITEM_MEMBER_GROUP,
    ITEM_MEMBER_GROUP_OPTION,
    ITEM_COMMUNITY_SETTINGS_SWITCH,
    ITEM_POLL_RESULT_USER,
    ITEM_MEMBERSHIP_PLAN,
    ITEM_ACCESS_CONTROL_MEMBERSHIP_BLOCKED,
    ITEM_MEMBER_DIRECTORY_SEARCH,
    ITEM_DRAWER_COMMUNITY,
    ITEM_DRAWER_JOIN_COMMUNITY,
    ITEM_HOME_FEED,
    ITEM_DRAWER_LINE_BREAK_VIEW,
    ITEM_DRAWER_BOTTOM_MENU,
    ITEM_SUBSCRIPTION_RENEW,
    ITEM_BRAND_COLOR,
    ITEM_GET_STARTED_COMMUNITY,
    ITEM_ANSWER_TYPE,
    ITEM_QUESTION_LOCATION,
    ITEM_QUESTION_FIELD,
    ITEM_QUESTION_CJF_MESSAGE,
    ITEM_QUESTION_DELETE,
    ITEM_QUESTION_FIELD_EMAIL,
    ITEM_CHATROOM_PDF,
    ITEM_CHATROOM_IMAGE,
    ITEM_VIEW_PARTICIPANTS,
    ITEM_EXPLORE,
    ITEM_PROFILE_NO_CHATROOM_JOINED,
    ITEM_QUESTION_ALIAS
)
@Retention(AnnotationRetention.SOURCE)
internal annotation class ViewType