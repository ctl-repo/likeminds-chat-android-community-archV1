package com.likeminds.chatmm.chatroom.detail.model

class ChatroomDetailActionModeData private constructor(
    val showReplyAction: Boolean,
    val showCopyAction: Boolean,
    val showEditAction: Boolean,
    val showDeleteAction: Boolean,
    val showReportAction: Boolean,
    val showSetAsTopic: Boolean
) {
    class Builder {
        private var showReplyAction: Boolean = false
        private var showCopyAction: Boolean = false
        private var showEditAction: Boolean = false
        private var showDeleteAction: Boolean = false
        private var showReportAction: Boolean = false
        private var showSetAsTopic: Boolean = false

        fun showReplyAction(showReplyAction: Boolean) =
            apply { this.showReplyAction = showReplyAction }

        fun showCopyAction(showCopyAction: Boolean) = apply { this.showCopyAction = showCopyAction }
        fun showEditAction(showEditAction: Boolean) = apply { this.showEditAction = showEditAction }
        fun showDeleteAction(showDeleteAction: Boolean) =
            apply { this.showDeleteAction = showDeleteAction }

        fun showReportAction(showReportAction: Boolean) =
            apply { this.showReportAction = showReportAction }

        fun showSetAsTopic(showSetAsTopic: Boolean) = apply { this.showSetAsTopic = showSetAsTopic }

        fun build() = ChatroomDetailActionModeData(
            showReplyAction,
            showCopyAction,
            showEditAction,
            showDeleteAction,
            showReportAction,
            showSetAsTopic
        )
    }

    fun toBuilder(): Builder {
        return Builder().showReplyAction(showReplyAction)
            .showCopyAction(showCopyAction)
            .showEditAction(showEditAction)
            .showDeleteAction(showDeleteAction)
            .showReportAction(showReportAction)
            .showSetAsTopic(showSetAsTopic)
    }
}