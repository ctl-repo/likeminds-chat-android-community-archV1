package com.likeminds.chatmm.chatroom.detail.view.adapter

import com.likeminds.chatmm.chatroom.detail.model.LMChatAttachmentItemViewData
import com.likeminds.chatmm.chatroom.detail.view.adapter.databinder.LMChatAttachmentItemViewDataBinder
import com.likeminds.chatmm.utils.customview.BaseRecyclerAdapter
import com.likeminds.chatmm.utils.customview.ViewDataBinder
import com.likeminds.chatmm.utils.model.BaseViewType

class LMChatAttachmentBarAdapter(
    val listener: LMChatAttachmentBarAdapterListener
) : BaseRecyclerAdapter<BaseViewType>() {

    init {
        initViewDataBinders()
    }

    override fun getSupportedViewDataBinder(): MutableList<ViewDataBinder<*, *>> {
        val viewDataBinders = ArrayList<ViewDataBinder<*, *>>(1)

        val lmChatAttachmentItemViewDataBinder =
            LMChatAttachmentItemViewDataBinder(listener)
        viewDataBinders.add(lmChatAttachmentItemViewDataBinder)

        return viewDataBinders
    }
}

interface LMChatAttachmentBarAdapterListener {
    fun onAttachmentClicked(attachmentItem: LMChatAttachmentItemViewData)
}