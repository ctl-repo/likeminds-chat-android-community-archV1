package com.likeminds.chatmm.conversation.view.adapter.databinder

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.likeminds.chatmm.LMAnalytics
import com.likeminds.chatmm.R
import com.likeminds.chatmm.buysellwidget.domain.model.FinxRecommendationMetadata
import com.likeminds.chatmm.chatroom.detail.util.ChatroomConversationItemViewDataBinderUtil
import com.likeminds.chatmm.chatroom.detail.view.adapter.ChatroomDetailAdapterListener
import com.likeminds.chatmm.conversation.model.ConversationViewData
import com.likeminds.chatmm.databinding.ItemConversationCustomWidgetBinding
import com.likeminds.chatmm.member.util.UserPreferences
import com.likeminds.chatmm.reactions.util.ReactionUtil
import com.likeminds.chatmm.reactions.util.ReactionsPreferences
import com.likeminds.chatmm.theme.model.LMTheme
import com.likeminds.chatmm.utils.ViewUtils.hide
import com.likeminds.chatmm.utils.ViewUtils.show
import com.likeminds.chatmm.utils.customview.ViewDataBinder
import com.likeminds.chatmm.utils.model.ITEM_CONVERSATION_CUSTOM_WIDGET
import org.json.JSONObject

class ConversationWidgetItemViewDataBinder(
    private val userPreferences: UserPreferences,
    private val reactionsPreferences: ReactionsPreferences,
    private val adapterListener: ChatroomDetailAdapterListener,
    private val onClick: () -> Unit
) : ViewDataBinder<ItemConversationCustomWidgetBinding, ConversationViewData>() {
    override val viewType: Int
        get() = ITEM_CONVERSATION_CUSTOM_WIDGET

    override fun createBinder(parent: ViewGroup): ItemConversationCustomWidgetBinding {
        val binding = ItemConversationCustomWidgetBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return binding
    }

    override fun bindData(
        binding: ItemConversationCustomWidgetBinding,
        data: ConversationViewData,
        position: Int
    ) {
        binding.apply {
            buttonColor = LMTheme.getButtonsColor()
            viewReply.buttonColor = LMTheme.getButtonsColor()
            conversation = data

            val context = root.context

            //Custom Widget Data
            val metadata = JSONObject(data.widgetViewData?.metadata.toString())
            val postConversationMetadata =
                Gson().fromJson(metadata.toString(), FinxRecommendationMetadata::class.java)

//            tvCustomWidgetMsg.text =
//                "${if (postConversationMetadata.isBuy == true) "Buy" else "Sell"} ${postConversationMetadata.symbol} at ${postConversationMetadata.entryPrice} SL ${postConversationMetadata.slPrice} target Price ${postConversationMetadata.targetPrice}"
//            Log.e(
//                "TAG",
//                "Meta Data String: \"${if (postConversationMetadata.isBuy == true) "Buy" else "Sell"} ${postConversationMetadata.symbol} at ${postConversationMetadata.entryPrice} SL ${postConversationMetadata.slPrice} target Price ${postConversationMetadata.targetPrice}\""
//            )
            with(postConversationMetadata) {
                tvCustomWidgetMsg.text = searchRsp.secDesc
                tvCwStopLossValue.text = slPrice
                tvCwEntryPriceValue.text = entryPrice
                tvCwTargetPriceValue.text = targetPrice
                btnCustomWidgetBuy.let {
                    it.text = if (isBuy == true) "Buy" else "Sell"
                    it.setBackgroundColor(
                        ContextCompat.getColor(
                            context,
                            if (isBuy == true) R.color.lm_chat_blue else R.color.lm_chat_green
                        )
                    )
                }
            }

            btnCustomWidgetBuy.setOnClickListener {
                adapterListener.onBuySellItemClicked(postConversationMetadata)
                onClick.invoke()
            }

            btnCustomWidgetScripInfo.setOnClickListener {
                Toast.makeText(context, "Scrip Info is Clicked", Toast.LENGTH_SHORT).show()
            }

            ChatroomConversationItemViewDataBinderUtil.initConversationBubbleView(
                clConversationRoot,
                clConversationBubble,
                memberImage,
                tvConversationMemberName,
                tvCustomTitle,
                tvCustomTitleDot,
                data.memberViewData,
                userPreferences.getUUID(),
                adapterListener,
                position,
                conversationViewData = data,
                imageViewStatus = ivConversationStatus,
                imageViewFailed = ivConversationFailed
            )

            if (data.deletedBy != null) {
                ChatroomConversationItemViewDataBinderUtil.initConversationBubbleDeletedTextView(
                    tvConversation,
                    tvDeleteMessage,
                    userPreferences.getUUID(),
                    conversationViewData = data
                )
                ivAddReaction.hide()
            } else {
                ChatroomConversationItemViewDataBinderUtil.initConversationBubbleTextView(
                    tvConversation,
                    data.answer,
                    position,
                    createdAt = data.createdAt,
                    conversation = data,
                    adapterListener = adapterListener,
                    tvDeleteMessage = tvDeleteMessage
                )

                val uuid = data.memberViewData.sdkClientInfo.uuid
                if (uuid == userPreferences.getUUID()) {
                    ivAddReaction.hide()
                } else {
                    if (data.answer.length > ConversationItemViewDataBinder.ADD_REACTION_CHARACTER_CHECK || !data.reactions
                            .isNullOrEmpty()
                    ) {
                        ivAddReaction.show()
                    } else {
                        ivAddReaction.hide()
                    }
                }
            }
            ChatroomConversationItemViewDataBinderUtil.initTimeAndStatus(
                tvTime,
                userPreferences.getUUID(),
                data.createdAt,
                imageViewStatus = ivConversationStatus,
                conversation = data
            )

            ChatroomConversationItemViewDataBinderUtil.initReplyView(
                viewReply,
                userPreferences.getUUID(),
                data.replyConversation,
                data.replyChatroomId,
                adapterListener,
                itemPosition = position,
                conversation = data
            )

            ChatroomConversationItemViewDataBinderUtil.initSelectionAnimation(
                binding.viewSelectionAnimation, position, adapterListener
            )

            ChatroomConversationItemViewDataBinderUtil.initReportView(
                ivReport,
                userPreferences.getUUID(),
                adapterListener,
                conversationViewData = data
            )

            val viewList = listOf(
                root,
                memberImage,
                tvConversation,
                viewReply.root,
                ivReport
            )
            isSelected =
                ChatroomConversationItemViewDataBinderUtil.initConversationSelection(
                    root,
                    viewList,
                    data,
                    position,
                    adapterListener
                )

            val messageReactionsGridViewData = ReactionUtil.getReactionsGrid(data)

            ChatroomConversationItemViewDataBinderUtil.initMessageReactionGridView(
                messageReactionsGridViewData,
                clConversationRoot,
                binding.clConversationBubble,
                binding.messageReactionsGridLayout,
                userPreferences.getUUID(),
                adapterListener,
                data
            )

            val isReactionHintShown =
                ChatroomConversationItemViewDataBinderUtil.isReactionHintViewShown(
                    data.isLastItem,
                    reactionsPreferences.getHasUserReactedOnce(),
                    reactionsPreferences.getNoOfTimesHintShown(),
                    reactionsPreferences.getTotalNoOfHintsAllowed(),
                    binding.tvDoubleTap,
                    data.memberViewData,
                    userPreferences.getUUID(),
                    clConversationRoot,
                    clConversationBubble
                )
            if (isReactionHintShown) {
                adapterListener.reactionHintShown()
            }

            ivAddReaction.setOnClickListener {
                adapterListener.onLongPressConversation(
                    data,
                    position,
                    LMAnalytics.Source.MESSAGE_REACTIONS_FROM_REACTION_BUTTON
                )
            }
        }
    }
}