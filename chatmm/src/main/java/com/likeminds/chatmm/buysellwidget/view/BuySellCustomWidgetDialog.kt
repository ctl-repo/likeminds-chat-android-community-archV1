package com.likeminds.chatmm.buysellwidget.view

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.likeminds.chatmm.R
import com.likeminds.chatmm.buysellwidget.adapter.SearchAdapter
import com.likeminds.chatmm.buysellwidget.data.ApiCallState
import com.likeminds.chatmm.buysellwidget.data.FinXService
import com.likeminds.chatmm.buysellwidget.data.RetrofitHelper
import com.likeminds.chatmm.buysellwidget.domain.model.Response
import com.likeminds.chatmm.buysellwidget.domain.repository.FinXRepository
import com.likeminds.chatmm.buysellwidget.domain.util.gone
import com.likeminds.chatmm.buysellwidget.domain.util.visible
import com.likeminds.chatmm.buysellwidget.viewmodel.FinXViewModel
import com.likeminds.chatmm.buysellwidget.viewmodel.FinXViewModelFactory
import com.likeminds.chatmm.chatroom.detail.view.ChatroomDetailFragment
import com.likeminds.chatmm.databinding.DialogBuySellCustomWidgetBinding
import com.likeminds.chatmm.xapp.XAppInstance

class BuySellCustomWidgetDialog : BottomSheetDialogFragment() {

    private var _binding: DialogBuySellCustomWidgetBinding? = null
    private val binding get() = _binding!!

    private lateinit var finXViewModel: FinXViewModel

    //    private lateinit var adapter: ArrayAdapter<String>
    private val searchResults = mutableListOf<Response>()
    private lateinit var adapter: SearchAdapter

    // Variables to hold the input values
    private var entryPrice: String? = null
    private var slPrice: String? = null
    private var targetPrice: String? = null
    private var orderType: String? = null
    private var selectedScrip: Response? = null
    private var secDescList: List<String> = emptyList()

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
        serUpOnClickListners()
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
                        Log.e("TAG", "setUpObservers: Response : $response")
                        searchResults.clear()
                        searchResults.addAll(response.response?.filterNotNull() ?: emptyList())

                        secDescList = searchResults.mapNotNull { it.secDesc }
                        Log.e("TAG", "setUpObservers: secDescList : $secDescList")
//                        updateSearchResults(secDescList)
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
    private fun serUpOnClickListners() {

//        adapter = ArrayAdapter(
//            requireContext(), android.R.layout.simple_dropdown_item_1line,
//            mutableListOf()
//        )
//        binding.searchAutoComplete.setAdapter(adapter)
//
//        binding.searchAutoComplete.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//            }
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                if (s != null && s.length > 2) {
//                    performSearch(s.toString())
//                }
//            }
//
//            override fun afterTextChanged(s: Editable?) {
//            }
//
//        })

        adapter = SearchAdapter(emptyList()) { selectedItem ->
            // Handle item click
            Log.e("Tag", "Item Clicked : ${selectedItem.secDesc}")
            binding.searchView.setQuery(selectedItem.secDesc, false)
            binding.searchResultRv.visibility = View.GONE
        }
        binding.searchResultRv.adapter = adapter
        binding.searchResultRv.layoutManager = LinearLayoutManager(context)

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
                binding.radioBuy.id -> orderType = "Buy"
                binding.radioSell.id -> orderType = "Sell"
            }
        }

//        binding.searchAutoComplete.setOnItemClickListener { parent, view, position, id ->
//            val selectedItem = adapter.getItem(position)
//            selectedScrip = searchResults.find { it.secDesc == selectedItem }
//
//            Log.e("TAG", "serUpOnClickListners: $selectedScrip")
//
//        }

        binding.postButton.setOnClickListener {
            // Collect all the data
            val entryPriceValue = entryPrice ?: ""
            val slPriceValue = slPrice ?: ""
            val targetPriceValue = targetPrice ?: ""
            val orderTypeValue = orderType ?: ""

            if (orderTypeValue.isNotEmpty() && entryPriceValue.isNotEmpty() &&
                slPriceValue.isNotEmpty() && targetPriceValue.isNotEmpty()
            ) {
                Log.e("TAG", "serUpOnClickListners: Selected Scrip :$selectedScrip")
                Log.e(
                    "TAG",
                    "serUpOnClickListners: EntryPrice:$entryPriceValue ,slPriveValue:$slPriceValue ,targetPriveValue: $targetPriceValue , orderType: $orderTypeValue"
                )

                val stockMetadata = Bundle().apply {
                    putString("entryPrice", entryPriceValue)
                    putString("slPrice", slPriceValue)
                    putString("targetPrice", targetPriceValue)
                    putString("orderType", orderTypeValue)
                    putString("selectedScrip", selectedScrip?.secDesc)
                }
//                sendMetadataChatroomDetailFragment(stockMetadata)
            } else {
                Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendMetadataChatroomDetailFragment(stockMetadata: Bundle) {
        val fragment = ChatroomDetailFragment().apply {
            arguments = stockMetadata
        }

        childFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun performSearch(query: String) {
        if (query.isNotEmpty() && query.length > 2) {
            Log.e("TAG", "performSearch: Search Query: $query")
            finXViewModel.getSearchScrip(query)
            binding.searchResultRv.visibility = View.VISIBLE
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

    private fun updateSearchResults(secDescList: List<String>) {
        Log.e("TAG", "updateSearchResults: Show DropDown")
//        adapter.clear()
//        adapter.addAll(secDescList)
//        adapter.notifyDataSetChanged()
//        binding.searchAutoComplete.showDropDown()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}