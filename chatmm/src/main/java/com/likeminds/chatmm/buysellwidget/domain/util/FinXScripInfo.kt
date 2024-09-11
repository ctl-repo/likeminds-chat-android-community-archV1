package com.likeminds.chatmm.buysellwidget.domain.util

object FinXScripInfo {

//    var priceDivisor: Float = if (isCds) 1_00_00_000f else 100f
    var ltpChange: Float? = null
    var ltp: Float = 0f
    var prevClose: Float = 0f


    fun setLTP(newValue: Int?, segment: Int?) {
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
}