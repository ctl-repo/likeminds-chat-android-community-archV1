package com.likeminds.chatmm.buysellwidget.presentation.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.likeminds.chatmm.buysellwidget.data.ApiCallState
import com.likeminds.chatmm.buysellwidget.data.FinXService
import com.likeminds.chatmm.buysellwidget.data.RetrofitHelper
import com.likeminds.chatmm.buysellwidget.domain.model.FinxRecommendationMetadata
import com.likeminds.chatmm.buysellwidget.domain.model.FinxSmSearchApiRsp
import com.likeminds.chatmm.buysellwidget.domain.repository.FinXRepositoryImpl
import com.likeminds.chatmm.buysellwidget.domain.util.gone
import com.likeminds.chatmm.buysellwidget.domain.util.visible
import com.likeminds.chatmm.buysellwidget.presentation.adapter.SearchAdapter
import com.likeminds.chatmm.buysellwidget.presentation.viewmodel.FinXViewModel
import com.likeminds.chatmm.buysellwidget.presentation.viewmodel.FinXViewModelFactory
import com.likeminds.chatmm.databinding.FragmentFinxRecommendationBinding


class FinxRecommendationFragment : Fragment() {

    private var _binding: FragmentFinxRecommendationBinding? = null
    private val binding get() = _binding!!

    private lateinit var finXViewModel: FinXViewModel
    private val searchResults = mutableListOf<FinxSmSearchApiRsp>()
    private lateinit var adapter: SearchAdapter

    // Variables to hold the input values
    private var entryPrice: String? = null
    private var slPrice: String? = null
    private var targetPrice: String? = null
    private var orderType: Boolean = true
    private var finxRecommendationMetadata: FinxRecommendationMetadata? = null
    private var selectedScrip: FinxSmSearchApiRsp? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentFinxRecommendationBinding.inflate(inflater, container, false)
        return binding.root
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
                    binding.pbSearchProgressIndicator.visible()
                }

                is ApiCallState.Success -> {
                    it.data?.let { response ->
                        binding.pbSearchProgressIndicator.gone()
                        searchResults.clear()
                        searchResults.addAll(response.response?.filterNotNull() ?: emptyList())
                        adapter.updateData(searchResults)
                    }
                }

                is ApiCallState.Error -> {
                    binding.pbSearchProgressIndicator.gone()
                    Log.e("TAG", "setUpObservers: Error ${it.errorMessage}")
                }
            }
        })
    }

    @SuppressLint("LogNotTimber")
    private fun setUpOnClickListners() {

        /*binding.svFinXSearchScrip.setOnQueryTextListener(object :
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
        })*/

        binding.etSearch.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.etSearch.text.toString()
                performSearch(query)
                true
            } else {
                false
            }
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                performSearch(query)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.etEntryPriceValue.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                entryPrice = s?.toString()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.etSlPriceValue.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                slPrice = s?.toString()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.etTargetPriceValue.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                targetPrice = s?.toString()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.rgOrderType.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                binding.rbBuy.id -> orderType = true
                binding.rbSell.id -> orderType = false
            }
        }

        binding.btnPost.setOnClickListener {
            // Collect all the data
            val entryPriceValue = entryPrice.orEmpty()
            val slPriceValue = slPrice.orEmpty()
            val targetPriceValue = targetPrice.orEmpty()

            if (entryPriceValue.isNotBlank() && slPriceValue.isNotBlank() && targetPriceValue.isNotBlank()) {
                finxRecommendationMetadata = FinxRecommendationMetadata(
                    entryPrice = entryPriceValue,
                    slPrice = slPriceValue,
                    targetPrice = targetPriceValue,
                    isBuy = orderType,
                    searchRsp = selectedScrip
                )
                val resultIntent = Intent().apply {
                    putExtra("recommendationData", finxRecommendationMetadata)
                }
                requireActivity().setResult(Activity.RESULT_OK, resultIntent)
                requireActivity().finish()
            } else {
                Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun performSearch(query: String) {
        if (query.isNotEmpty() && query.length > 2) {
            finXViewModel.getSearchScrip(query)
            binding.rvSearchScripResult.visible()
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
            binding.etSearch.setText(selectedItem.secDesc)
            binding.rvSearchScripResult.gone()
        }
        binding.rvSearchScripResult.adapter = adapter
        binding.rvSearchScripResult.layoutManager = LinearLayoutManager(context)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
