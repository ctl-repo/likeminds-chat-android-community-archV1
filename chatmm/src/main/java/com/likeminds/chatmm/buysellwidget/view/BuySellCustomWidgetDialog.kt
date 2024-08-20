package com.likeminds.chatmm.buysellwidget.view

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.likeminds.chatmm.buysellwidget.data.ApiCallState
import com.likeminds.chatmm.buysellwidget.data.FinXService
import com.likeminds.chatmm.buysellwidget.data.RetrofitHelper
import com.likeminds.chatmm.buysellwidget.domain.repository.FinXRepository
import com.likeminds.chatmm.buysellwidget.viewmodel.FinXViewModel
import com.likeminds.chatmm.buysellwidget.viewmodel.FinXViewModelFactory
import com.likeminds.chatmm.databinding.DialogBuySellCustomWidgetBinding
import com.likeminds.chatmm.xapp.XAppInstance

class BuySellCustomWidgetDialog : BottomSheetDialogFragment() {

    private var _binding: DialogBuySellCustomWidgetBinding? = null
    private val binding get() = _binding!!

    lateinit var finXViewModel: FinXViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout using DataBindingUtil
        _binding = DialogBuySellCustomWidgetBinding.inflate(inflater, container, false)

        Log.e("ChatFragment", "onCreateView: ${XAppInstance.sessionID}")

        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        setOnClickListners()
        setUpObservers()
    }

    private fun setUpObservers() {
        finXViewModel.searchScrip.observe(requireActivity(), Observer {
            when (it) {
                is ApiCallState.Loading -> {}
                is ApiCallState.Success -> {
                    it.data?.let {
                        Log.e("TAG", "setUpObservers: Response : $it")
                    }
                }

                is ApiCallState.Error -> {
                    Log.e("TAG", "setUpObservers: Error ${it.errorMessage}")
                }
            }
        })
    }

    private fun setOnClickListners() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                performSearch(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
    }

    private fun performSearch(query: String) {
        if (query.isNotEmpty()) {
            Log.e("TAG", "performSearch: Search Query: $query")
            finXViewModel.getSearchScrip(query)
        } else {
            Log.e("TAG", "performSearch: Search Query is empty")
        }
    }

    private fun initViewModel() {
        val finXService = RetrofitHelper.getInstance().create(FinXService::class.java)
        val repository = FinXRepository(finXService)
        finXViewModel =
            ViewModelProvider(this, FinXViewModelFactory(repository))[FinXViewModel::class.java]
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}