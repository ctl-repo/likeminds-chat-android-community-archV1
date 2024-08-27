package com.likeminds.chatmm.buysellwidget.presentation.view

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.likeminds.chatmm.buysellwidget.presentation.adapter.SearchAdapter
import com.likeminds.chatmm.buysellwidget.data.ApiCallState
import com.likeminds.chatmm.buysellwidget.data.FinXService
import com.likeminds.chatmm.buysellwidget.data.RetrofitHelper
import com.likeminds.chatmm.buysellwidget.domain.model.Response
import com.likeminds.chatmm.buysellwidget.domain.repository.FinXRepositoryImpl
import com.likeminds.chatmm.buysellwidget.domain.util.gone
import com.likeminds.chatmm.buysellwidget.domain.util.visible
import com.likeminds.chatmm.buysellwidget.presentation.viewmodel.FinXViewModel
import com.likeminds.chatmm.buysellwidget.presentation.viewmodel.FinXViewModelFactory
import com.likeminds.chatmm.databinding.DialogBuySellCustomWidgetBinding

class BuySellCustomWidgetDialog(val onPostClicked: (Bundle) -> Unit) : BottomSheetDialogFragment() {

    private var _binding: DialogBuySellCustomWidgetBinding? = null
    private val binding get() = _binding!!

    private lateinit var finXViewModel: FinXViewModel
    private val searchResults = mutableListOf<Response>()
    private lateinit var adapter: SearchAdapter

    // Variables to hold the input values
    private var entryPrice: String? = null
    private var slPrice: String? = null
    private var targetPrice: String? = null
    private var orderType: Boolean = true
    private var selectedScrip: Response? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout using DataBindingUtil
        _binding = DialogBuySellCustomWidgetBinding.inflate(inflater, container, false)
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
        initSetup()
        setUpOnClickListners()
        setUpObservers()
    }

    private fun setUpObservers() {
        finXViewModel.searchScrip.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ApiCallState.Loading -> {
                    binding.searchProgressIndicator.visible()
                }

                is ApiCallState.Success -> {
                    it.data?.let { response ->
                        binding.searchProgressIndicator.gone()
                        searchResults.clear()
                        searchResults.addAll(response.response?.filterNotNull() ?: emptyList())
                        adapter.updateData(searchResults)
                    }
                }

                is ApiCallState.Error -> {
                    binding.searchProgressIndicator.gone()
                    Log.e("TAG", "setUpObservers: Error ${it.errorMessage}")
                }
            }
        })
    }

    @SuppressLint("LogNotTimber")
    private fun setUpOnClickListners() {

        binding.searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Handle query submission if needed
                query?.let {
                    performSearch(it)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Handle query text change to update the search results
                newText?.let {
                    // Assume you have a function to fetch filtered data based on query
                    performSearch(it)
                }
                return true
            }
        })

        binding.entryEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                entryPrice = s?.toString()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.slPriceEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                slPrice = s?.toString()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.targetPriceEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                targetPrice = s?.toString()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.orderTypeRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                binding.radioBuy.id -> orderType = true
                binding.radioSell.id -> orderType = false
            }
        }

        binding.postButton.setOnClickListener {
            // Collect all the data
            val entryPriceValue = entryPrice.orEmpty()
            val slPriceValue = slPrice.orEmpty()
            val targetPriceValue = targetPrice.orEmpty()

            if (entryPriceValue.isNotBlank() && slPriceValue.isNotBlank() && targetPriceValue.isNotBlank()) {
                val stockMetadata = Bundle().apply {
                    putString("entryPrice", entryPriceValue)
                    putString("slPrice", slPriceValue)
                    putString("targetPrice", targetPriceValue)
                    putBoolean("isBuy", orderType)
                    selectedScrip?.let {
                        putInt("segment", it.segmentId ?: 0)
                        putInt("token", it.token ?: 0)
                        putString("symbol", it.symbol)
                        putString("secDesc", it.secDesc)
                    }
                }
                onPostClicked(stockMetadata)
                dismiss()
            } else {
                Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun performSearch(query: String) {
        if (query.isNotEmpty() && query.length > 2) {
            finXViewModel.getSearchScrip(query)
            binding.searchResultRv.visible()
        } else {
            Log.e("TAG", "performSearch: Search Query is empty")
        }
    }

    private fun initSetup() {
        //init ViewModel
        val finXService = RetrofitHelper.getInstance().create(FinXService::class.java)
        val repository = FinXRepositoryImpl(finXService)
        finXViewModel =
            ViewModelProvider(this, FinXViewModelFactory(repository))[FinXViewModel::class.java]

        //init RecyclerView
        adapter = SearchAdapter(emptyList()) { selectedItem ->
            selectedScrip = selectedItem
            binding.searchView.setQuery(selectedItem.secDesc, false)
            binding.searchResultRv.gone()
        }
        binding.searchResultRv.adapter = adapter
        binding.searchResultRv.layoutManager = LinearLayoutManager(context)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}