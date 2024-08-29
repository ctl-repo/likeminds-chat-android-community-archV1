package com.likeminds.chatmm.dm.view

import android.content.Context
import androidx.fragment.app.FragmentManager
import com.likeminds.chatmm.databinding.DialogFragmentSendDmRequestBinding
import com.likeminds.chatmm.utils.customview.BaseDialogFragment

class SendDMRequestDialogFragment :
    BaseDialogFragment<DialogFragmentSendDmRequestBinding>() {

    private lateinit var sendDMRequestDialogListener: SendDMRequestDialogListener

    companion object {
        private const val TAG = "SendDMRequestDialogFragment"

        @JvmStatic
        fun showDialog(
            supportFragmentManager: FragmentManager
        ) {
            SendDMRequestDialogFragment().show(supportFragmentManager, TAG)
        }
    }

    override fun getViewBinding(): DialogFragmentSendDmRequestBinding {
        return DialogFragmentSendDmRequestBinding.inflate(layoutInflater)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            sendDMRequestDialogListener = parentFragment as SendDMRequestDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException("Calling fragment must implement SendDMRequestDialogListener interface")
        }
    }

    override fun setUpViews() {
        super.setUpViews()
        initializeListeners()
    }

    private fun initializeListeners() {
        binding.apply {
            tvCancel.setOnClickListener {
                this@SendDMRequestDialogFragment.dismiss()
            }

            tvConfirm.setOnClickListener {
                sendDMRequestDialogListener.sendDMRequest()
                this@SendDMRequestDialogFragment.dismiss()
            }
        }
    }
}

interface SendDMRequestDialogListener {
    fun sendDMRequest()
}