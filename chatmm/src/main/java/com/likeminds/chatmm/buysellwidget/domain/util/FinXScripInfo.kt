package com.likeminds.chatmm.buysellwidget.domain.util

import com.likeminds.chatmm.R
import com.likeminds.chatmm.buysellwidget.domain.model.multipletouchlineV2.FinXMultitouchline
import java.text.DecimalFormat

object FinXScripInfo {

    //var priceDivisor: Float = if (isCds) 1_00_00_000f else 100f
    var ltpChange: Float? = null
    var ltp: Float = 0f
    var prevClose: Float = 0f

    fun getPriceDivisor(segment: Int) = if (segment == 13 || segment == 14) 1_00_00_000f else 100f

    fun setLtpAndCpp(mtl: FinXMultitouchline?) {
        mtl ?: return
        val segment = mtl.segment ?: 1
        ltp = ((mtl.ltp ?: 0) / getPriceDivisor(segment))
        prevClose = ((mtl.pc ?: 0) / getPriceDivisor(segment))
    }

    fun getChange(): Float = ltp - prevClose

    fun getChangePer(): Float {
        return if (prevClose != 0.0f) {
            (ltp - prevClose) / prevClose * 100
        } else {
            0.0f
        }
    }


    fun setLTP(segment: Int?, newValue: Int?) {
        if (newValue != null) {
            val newLTP = if (newValue == 0) {
                if (ltp == 0f) prevClose
                else ltp
            } else newValue / if (segment == 13 || segment == 14) 1_00_00_000f else 100f

            setLtpChanged(newLTP, ltp)

            ltp = newLTP
        }
    }

    private fun setLtpChanged(newLTP: Float, oldLTP: Float) {
        if (ltpChange == null)
            ltpChange = 0f
        else
            ltpChange = newLTP - oldLTP // 100- 101 = -1
    }

    private const val mdPattern2 = "##,##,##0.00"
    private val m2DecimalFormat = DecimalFormat(mdPattern2)
    fun cf2(value: Float) = m2DecimalFormat.format(value)

    fun getCcp(): String {
        return "${cf2(getChange())} (${cf2(getChangePer())}%)"
    }

    fun getCcpColor(): Int {
        val isLtpChange = when {
            ((ltpChange ?: 0f) > 0f) -> true
            ((ltpChange ?: 0f) < 0f) -> false
            else -> null
        }

        return when (isLtpChange) {
            null -> R.color.lm_chat_black
            true -> R.color.lm_chat_teal
            false -> R.color.lm_chat_red
        }
    }
}