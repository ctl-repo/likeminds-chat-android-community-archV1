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
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.likeminds.chatmm.R
import com.likeminds.chatmm.buysellwidget.data.ApiCallState
import com.likeminds.chatmm.buysellwidget.data.FinXService
import com.likeminds.chatmm.buysellwidget.data.RetrofitHelper
import com.likeminds.chatmm.buysellwidget.domain.model.FinxRecommendationMetadata
import com.likeminds.chatmm.buysellwidget.domain.model.FinxSmSearchApiRsp
import com.likeminds.chatmm.buysellwidget.domain.repository.FinXRepositoryImpl
import com.likeminds.chatmm.buysellwidget.domain.util.FinXDialog
import com.likeminds.chatmm.buysellwidget.domain.util.FinXScripInfo
import com.likeminds.chatmm.buysellwidget.domain.util.gone
import com.likeminds.chatmm.buysellwidget.domain.util.visible
import com.likeminds.chatmm.buysellwidget.presentation.adapter.SearchAdapter
import com.likeminds.chatmm.buysellwidget.presentation.helper.DecimalDigitsInputFilter
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
    /*
    private var slPrice: String? = null
    private var targetPrice: String? = null
    */
    private var entryPrice: String? = null
    private var orderType: Boolean = true
    private var finxRecommendationMetadata: FinxRecommendationMetadata? = null
    private var selectedScrip: FinxSmSearchApiRsp? = null
    private var selectedSearchScrip: String? = null

    //validation for input fields
    private val maxDigitsBeforeDecimal = 10
    private val maxDigitsAfterDecimal = 2

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
        setUpOnClickListeners()
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

        finXViewModel.multiTouchLineRes.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ApiCallState.Loading -> {}
                is ApiCallState.Success -> {
                    it.data?.let {
                        Log.e("TAG", "setUpObservers: Success $it")
                        FinXScripInfo.setLTP(it.Response?.alMt?.get(0)?.ltp, selectedScrip?.segment)
                        entryPrice = FinXScripInfo.ltp.toString()
                        binding.etEntryPriceValue.setText(entryPrice)
                    }
                }

                is ApiCallState.Error -> {
                    Log.e("TAG", "setUpObservers: Error ${it.errorMessage}")
                }
            }
        })
    }

    @SuppressLint("LogNotTimber")
    private fun setUpOnClickListeners() {

        binding.etSearchScrip.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.isNotEmpty() == true && s.toString().length > 2) {
                    binding.ibClear.visible()
                    performSearch(s.toString())
                } else {
                    binding.ibClear.gone()
                    binding.rvSearchScripResult.gone()

                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.ibClear.setOnClickListener {
            binding.etSearchScrip.text?.clear()
            binding.ibClear.gone()
            binding.rvSearchScripResult.gone()
        }

        binding.rgOrderType.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                binding.rbBuy.id -> orderType = true
                binding.rbSell.id -> orderType = false
            }
        }

        binding.etEntryPriceValue.filters =
            arrayOf(DecimalDigitsInputFilter(maxDigitsBeforeDecimal, maxDigitsAfterDecimal))
        binding.etSlPriceValue.filters =
            arrayOf(DecimalDigitsInputFilter(maxDigitsBeforeDecimal, maxDigitsAfterDecimal))
        binding.etTargetPriceValue.filters =
            arrayOf(DecimalDigitsInputFilter(maxDigitsBeforeDecimal, maxDigitsAfterDecimal))

        binding.btnPost.setOnClickListener {
            val entryPriceValue =
                binding.etEntryPriceValue.text.toString().ifEmpty { " 0.0" }.toDouble()
            val slPriceValue = binding.etSlPriceValue.text.toString().ifEmpty { " 0.0" }.toDouble()
            val targetPriceValue =
                binding.etTargetPriceValue.text.toString().ifEmpty { " 0.0" }.toDouble()

            val message: String
            val isValid = if (orderType) {
                message = "For Buy: Please enter all fields correctly"
                slPriceValue < entryPriceValue && entryPriceValue < targetPriceValue
            } else {
                message =
                    "For Sell: Please enter all fields correctly"
                slPriceValue > entryPriceValue && entryPriceValue > targetPriceValue
            }

            if (isValid && !selectedSearchScrip.isNullOrEmpty()) {
                finxRecommendationMetadata = FinxRecommendationMetadata(
                    entryPrice = entryPriceValue.toString(),
                    slPrice = slPriceValue.toString(),
                    targetPrice = targetPriceValue.toString(),
                    isBuy = orderType,
                    searchRsp = selectedScrip,
                    customWidgetType = "FinXRecommendation"
                )

                val resultIntent = Intent().apply {
                    putExtra("recommendationData", finxRecommendationMetadata)
                }
                requireActivity().setResult(Activity.RESULT_OK, resultIntent)
                requireActivity().finish()
            } else {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }

        binding.ivRetryEntryPrice.setOnClickListener {
            finXViewModel.getMultiTouchLine(selectedScrip?.token, selectedScrip?.segment)
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
            selectedSearchScrip = selectedItem.secDesc
            binding.etSearchScrip.setText(selectedItem.secDesc)
            binding.rvSearchScripResult.gone()
            finXViewModel.getMultiTouchLine(selectedItem.token, selectedItem.segment)
        }
        binding.rvSearchScripResult.adapter = adapter
        binding.rvSearchScripResult.layoutManager = LinearLayoutManager(context)

        onBackPressedHandler {
            FinXDialog.alertDialogF2(
                frag = this,
                msg = getString(R.string.do_you_want_to_exit),
                positiveText = R.string.yes,
                positiveClickListener = { dialog, _ ->
                    requireActivity().finish()
                },
                negativeText = R.string.no,
                negativeClickListener = { dialog, _ ->
                    dialog.dismiss()
                })
        }
    }

    private fun onBackPressedHandler(onClickFunction: () -> Unit) = try {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = onClickFunction()
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    } catch (e: Exception) {
        e.printStackTrace()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
