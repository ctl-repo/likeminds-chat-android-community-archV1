package com.likeminds.chatmm.buysellwidget.view

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.likeminds.chatmm.buysellwidget.data.ApiCallState
import com.likeminds.chatmm.buysellwidget.data.FinXService
import com.likeminds.chatmm.buysellwidget.data.RetrofitHelper
import com.likeminds.chatmm.buysellwidget.domain.model.Response
import com.likeminds.chatmm.buysellwidget.domain.repository.FinXRepository
import com.likeminds.chatmm.buysellwidget.viewmodel.FinXViewModel
import com.likeminds.chatmm.buysellwidget.viewmodel.FinXViewModelFactory
import com.likeminds.chatmm.databinding.DialogBuySellCustomWidgetBinding
import com.likeminds.chatmm.xapp.XAppInstance

class BuySellCustomWidgetDialog : BottomSheetDialogFragment() {

    private var _binding: DialogBuySellCustomWidgetBinding? = null
    private val binding get() = _binding!!

    private lateinit var finXViewModel: FinXViewModel
    private lateinit var adapter: ArrayAdapter<String>
    private val searchResults = mutableListOf<Response>()

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
        setupSearchHandler()
        setUpObservers()
    }

    private fun setUpObservers() {
        finXViewModel.searchScrip.observe(requireActivity(), Observer {
            when (it) {
                is ApiCallState.Loading -> {}
                is ApiCallState.Success -> {
                    it.data?.let { response ->
                        Log.e("TAG", "setUpObservers: Response : $response")
                        searchResults.clear()
                        searchResults.addAll(response.response?.filterNotNull() ?: emptyList())

                        val secDescList = searchResults.mapNotNull { it.secDesc }
                        Log.e("TAG", "setUpObservers: secDescList : $secDescList")
                        adapter.clear()
                        adapter.addAll(secDescList)
                        adapter.notifyDataSetChanged()
                        binding.searchAutoComplete.showDropDown()
                    }
                }

                is ApiCallState.Error -> {
                    Log.e("TAG", "setUpObservers: Error ${it.errorMessage}")
                }
            }
        })
    }

    private fun setupSearchHandler() {

        adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_dropdown_item_1line,
            mutableListOf()
        )
        binding.searchAutoComplete.setAdapter(adapter)

        binding.searchAutoComplete.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null && s.length > 2) {
                    performSearch(s.toString())
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

        binding.searchAutoComplete.setOnItemClickListener { parent, view, position, id ->
            val selectedItem = adapter.getItem(position)
            val selectedScrip = searchResults.find { it.secDesc == selectedItem }

            Log.e("TAG", "setupSearchHandler: $selectedScrip")

        }
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