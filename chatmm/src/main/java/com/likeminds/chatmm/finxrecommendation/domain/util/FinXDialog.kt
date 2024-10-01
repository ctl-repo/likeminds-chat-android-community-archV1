package com.likeminds.chatmm.finxrecommendation.domain.util

import android.content.DialogInterface
import android.graphics.Color
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.likeminds.chatmm.R

object FinXDialog {

    fun alertDialogF2(
        frag: Fragment,
        title: Int = R.string.attention,
        msg: String,
        positiveText: Int = R.string.ok,
        positiveClickListener: DialogInterface.OnClickListener,
        negativeText: Int = R.string.back,
        negativeClickListener: DialogInterface.OnClickListener = DialogInterface.OnClickListener { dialog, _ -> dialog.dismiss() },
        shouldUseNegativeAsNeuralButton: Boolean = false,
        isCancellable: Boolean = true
    ): AlertDialog? {
        try {
            val ad = AlertDialog.Builder(frag.requireContext()/*, R.style.AppAlertDialog*/)
            /*ad.setTitle(
                HtmlCompat.fromHtml(
                    "<font color='#283593>${frag.getString(title)}</font>",
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
            )*/
            ad.setTitle(frag.getString(title))
            ad.setMessage(msg)

            if (positiveText != 0) ad.setPositiveButton(
                frag.getString(positiveText),
                positiveClickListener
            )
            if (negativeText != 0) {
                if (shouldUseNegativeAsNeuralButton)
                    ad.setNeutralButton(frag.getString(negativeText), negativeClickListener)
                else
                    ad.setNegativeButton(frag.getString(negativeText), negativeClickListener)
            }
            ad.setCancelable(false)
            //ad.show();
            val alert = ad.create()

            alert.show()

            alert.setCancelable(isCancellable)

            val negBtn: Int = if (shouldUseNegativeAsNeuralButton) DialogInterface.BUTTON_NEUTRAL
            else DialogInterface.BUTTON_NEGATIVE

            alert.getButton(negBtn).setTextColor(Color.GRAY)

            alert.getButton(AlertDialog.BUTTON_POSITIVE).isAllCaps = false
            if (shouldUseNegativeAsNeuralButton)
                alert.getButton(AlertDialog.BUTTON_NEUTRAL).isAllCaps = false
            else
                alert.getButton(AlertDialog.BUTTON_NEGATIVE).isAllCaps = false

            return alert

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        } catch (e: java.lang.Error) {
            if (frag.isAdded)
                Toast.makeText(
                    frag.requireContext(),
                    R.string.out_of_memory_err_1,
                    Toast.LENGTH_LONG
                ).show()
        }
        return null
    }
}