package com.likeminds.chatmm.chatroom.detail.view.adapter.databinder

import android.view.LayoutInflater
import android.view.ViewGroup
import com.likeminds.chatmm.chatroom.detail.model.LMChatAttachmentPickerItemViewData
import com.likeminds.chatmm.chatroom.detail.view.adapter.LMChatAttachmentBarAdapterListener
import com.likeminds.chatmm.databinding.LmChatItemAttachmentBinding
import com.likeminds.chatmm.utils.customview.ViewDataBinder
import com.likeminds.chatmm.utils.model.ITEM_ATTACHMENT_ITEM

class LMChatAttachmentPickerItemViewDataBinder(
    val listener: LMChatAttachmentBarAdapterListener
) : ViewDataBinder<LmChatItemAttachmentBinding, LMChatAttachmentPickerItemViewData>() {
    override val viewType: Int
        get() = ITEM_ATTACHMENT_ITEM

    override fun createBinder(parent: ViewGroup): LmChatItemAttachmentBinding {
        val binding = LmChatItemAttachmentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        setListener(binding)

        return binding
    }

    override fun bindData(
        binding: LmChatItemAttachmentBinding,
        data: LMChatAttachmentPickerItemViewData,
        position: Int
    ) {
        binding.apply {
            attachmentData = data

            ivAttachmentIcon.setImageResource(data.attachmentIcon)
            tvAttachmentLabel.text = data.attachmentName
        }
    }

    private fun setListener(binding: LmChatItemAttachmentBinding) {
        binding.root.setOnClickListener {
            val attachmentData = binding.attachmentData ?: return@setOnClickListener
            listener.onAttachmentBarItemClicked(attachmentData)
        }
    }
}