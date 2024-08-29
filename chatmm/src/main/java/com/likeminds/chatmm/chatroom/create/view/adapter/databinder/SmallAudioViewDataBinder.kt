package com.likeminds.chatmm.chatroom.create.view.adapter.databinder

import android.view.*
import androidx.core.content.ContextCompat
import com.likeminds.chatmm.R
import com.likeminds.chatmm.chatroom.create.view.adapter.ImageAdapterListener
import com.likeminds.chatmm.databinding.ItemAudioSmallBinding
import com.likeminds.chatmm.media.model.SmallMediaViewData
import com.likeminds.chatmm.utils.DateUtil
import com.likeminds.chatmm.utils.customview.ViewDataBinder
import com.likeminds.chatmm.utils.databinding.ImageBindingUtil
import com.likeminds.chatmm.utils.model.ITEM_AUDIO_SMALL

class SmallAudioViewDataBinder constructor(private val imageAdapterListener: ImageAdapterListener) :
    ViewDataBinder<ItemAudioSmallBinding, SmallMediaViewData>() {

    override val viewType: Int
        get() = ITEM_AUDIO_SMALL

    override fun createBinder(parent: ViewGroup): ItemAudioSmallBinding {
        return ItemAudioSmallBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    }

    override fun bindData(
        binding: ItemAudioSmallBinding,
        data: SmallMediaViewData,
        position: Int
    ) {
        binding.apply {
            smallMediaViewData = data

            if (data.isSelected) {
                constraintLayout.background = ContextCompat.getDrawable(
                    binding.root.context,
                    R.drawable.lm_chat_background_transparent_turquoise_2
                )
            } else {
                constraintLayout.background = ContextCompat.getDrawable(
                    binding.root.context,
                    R.drawable.lm_chat_background_transparent
                )
            }

            if (data.singleUriData.thumbnailUri == null) {
                cvNoThumbnail.visibility = View.VISIBLE
                cvThumbnail.visibility = View.GONE
                tvAudioDuration.text =
                    DateUtil.formatSeconds(data.singleUriData.duration ?: 0)
            } else {
                cvNoThumbnail.visibility = View.GONE
                cvThumbnail.visibility = View.VISIBLE
                ImageBindingUtil.loadImage(
                    ivThumbnail,
                    data.singleUriData.thumbnailUri
                )
            }

            root.setOnClickListener {
                imageAdapterListener.mediaSelected(position, data)
            }
        }
    }
}