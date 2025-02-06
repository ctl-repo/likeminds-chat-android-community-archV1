package com.likeminds.chatmm.finxrecommendation.presentation.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.likeminds.chatmm.R
import com.likeminds.chatmm.finxrecommendation.data.ApiCallState
import com.likeminds.chatmm.finxrecommendation.data.FinXService
import com.likeminds.chatmm.finxrecommendation.data.RetrofitHelper
import com.likeminds.chatmm.finxrecommendation.domain.model.FinXRecommendationMetadata
import com.likeminds.chatmm.finxrecommendation.domain.model.FinxSmSearchApiRsp
import com.likeminds.chatmm.finxrecommendation.domain.repository.FinXRepositoryImpl
import com.likeminds.chatmm.finxrecommendation.domain.util.FinXDialog
import com.likeminds.chatmm.finxrecommendation.domain.util.FinXScripInfo
import com.likeminds.chatmm.finxrecommendation.domain.util.gone
import com.likeminds.chatmm.finxrecommendation.domain.util.visible
import com.likeminds.chatmm.finxrecommendation.presentation.adapter.SearchAdapter
import com.likeminds.chatmm.finxrecommendation.presentation.helper.DecimalDigitsInputFilter
import com.likeminds.chatmm.finxrecommendation.presentation.viewmodel.FinXViewModel
import com.likeminds.chatmm.finxrecommendation.presentation.viewmodel.FinXViewModelFactory
import com.likeminds.chatmm.databinding.FragmentFinxRecommendationBinding


class FinXRecommendationFragment : Fragment() {

    private var _binding: FragmentFinxRecommendationBinding? = null
    private val binding get() = _binding!!

    private lateinit var finXViewModel: FinXViewModel
    private val searchResults = mutableListOf<FinxSmSearchApiRsp>()
    private lateinit var adapter: SearchAdapter

    private var entryPrice: String? = null
    private var slPrice: String? = null
    private var targetPrice: String? = null
    private var orderType: Boolean = true
    private var finxRecommendationMetadata: FinXRecommendationMetadata? = null
    private var selectedScrip: FinxSmSearchApiRsp? = null

    //validation for input fields
    private val maxDigitsBeforeDecimal = 10
    private val maxDigitsAfterDecimal = 2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
        finXViewModel.searchScrip.observe(viewLifecycleOwner) {
            when (it) {
                is ApiCallState.Loading -> {
                    binding.pgSearch.visible()
                }

                is ApiCallState.Success -> {
                    it.data?.let { response ->
                        binding.pgSearch.gone()
                        searchResults.clear()
                        searchResults.addAll(response.response?.filterNotNull() ?: emptyList())
                        adapter.updateData(searchResults)
                    }
                }

                is ApiCallState.Error -> {
                    binding.pgSearch.gone()
                    Log.e("TAG", "setUpObservers: Error ${it.errorMessage}")
                }
            }
        }

        finXViewModel.multiTouchLineRes.observe(viewLifecycleOwner) {
            when (it) {
                is ApiCallState.Loading -> {
                    binding.pgLtp.visible()
                }

                is ApiCallState.Success -> {
                    it.data?.let {
                        binding.pgLtp.gone()
                        Log.e("TAG", "setUpObservers: Success $it")
                        //FinXScripInfo.setLTP(selectedScrip?.segment, it.Response?.alMt?.get(0)?.ltp)
                        FinXScripInfo.setLtpAndCpp(it.Response?.alMt?.get(0))

                        entryPrice = FinXScripInfo.ltp.toString()

                        if (orderType) {
                            //TODO : Expect to add 5% SL and 10% Target
                            slPrice = "" //(FinXScripInfo.ltp - 10).toString()
                            targetPrice = "" //(FinXScripInfo.ltp + 10).toString()
                        } else {
                            slPrice = "" //(FinXScripInfo.ltp + 10).toString()
                            targetPrice = "" //(FinXScripInfo.ltp - 10).toString()
                        }

                        binding.tvLtp.text = "${FinXScripInfo.ltp}"
                        binding.tvCcp.text = FinXScripInfo.getCcp()
                        //binding.tvCcp.setTextColor(ContextCompat.getColor(requireContext(), FinXScripInfo.getCcpColor()))
                        binding.tvCcp.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                FinXScripInfo.getCcpColor()
                            )
                        )

                        binding.etEntryPriceValue.setText(entryPrice)
                        binding.etSlPriceValue.setText(slPrice)
                        binding.etTargetPriceValue.setText(targetPrice)
                    }
                }

                is ApiCallState.Error -> {
                    binding.pgLtp.gone()
                    Log.e("TAG", "setUpObservers: Error ${it.errorMessage}")
                }
            }
        }
    }

    @SuppressLint("LogNotTimber")
    private fun setUpOnClickListeners() {

        binding.etSearch.addTextChangedListener {
            val strLength = it?.length ?: 0
            showSearchList(selectedScrip == null || strLength != 0)

            if (strLength >= 2) {
                binding.ibClear.visible()
                performSearch(it.toString())
            }
        }

        binding.etSearch.setOnFocusChangeListener { view, hasFocus ->
            val imm = getSystemService(requireContext(), InputMethodManager::class.java)

            if (hasFocus) imm?.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
            else imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }

        binding.ibClear.setOnClickListener {
            binding.etSearch.text?.clear()
            binding.ibClear.gone()
            searchResults.clear()
            adapter.updateData(searchResults)
            showSearchList(selectedScrip == null)
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

        /**
         * Saving the value in big-decimal instead of double as for large 8-10 digit number(without decimal)
         * the conversion to double gives the scientific notation i.e. E so to resolve this
         * using the big-decimal and after convert it into plain-string (without scientific notation)
         * */
        binding.btnPost.setOnClickListener {
            val entryPriceValue =
                binding.etEntryPriceValue.text.toString().ifEmpty { "0.0" }.toBigDecimal()
            val slPriceValue = binding.etSlPriceValue.text.toString().ifEmpty { "0.0" }.toBigDecimal()
            val targetPriceValue =
                binding.etTargetPriceValue.text.toString().ifEmpty { "0.0" }.toBigDecimal()

            val message: String
            val isValid = if (orderType) {
                message = "For Buy: Please enter all fields correctly"
                slPriceValue < entryPriceValue && entryPriceValue < targetPriceValue
            } else {
                message =
                    "For Sell: Please enter all fields correctly"
                slPriceValue > entryPriceValue && entryPriceValue > targetPriceValue
            }

            if (isValid && selectedScrip != null) {
                finxRecommendationMetadata = FinXRecommendationMetadata(
                    entryPrice = entryPriceValue.toPlainString(),
                    slPrice = slPriceValue.toPlainString(),
                    targetPrice = targetPriceValue.toPlainString(),
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
            showSearchList(true)
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

            binding.ibClear.gone()

            binding.etSearch.setText("")
            binding.etSearch.clearFocus()

            binding.tvScripName.text = selectedItem.getScripName()

            showSearchList(false)

            finXViewModel.getMultiTouchLine(selectedItem.token, selectedItem.segment)
        }

        binding.rvSearchScripResult.layoutManager = LinearLayoutManager(context)

        binding.rvSearchScripResult.addItemDecoration(
            DividerItemDecoration(
                binding.rvSearchScripResult.context,
                DividerItemDecoration.VERTICAL
            )
        )

        binding.rvSearchScripResult.adapter = adapter

        binding.ibBack.setOnClickListener {
            funOnBackPressed()
        }

        onBackPressedHandler {
            funOnBackPressed()
        }

        binding.etSearch.requestFocus()
    }

    private fun funOnBackPressed() {
        FinXDialog.alertDialogF2(
            frag = this,
            msg = getString(R.string.do_you_want_to_save_changes),
            positiveText = R.string.lm_chat_create_chat_post,
            positiveClickListener = { dialog, _ ->
                binding.btnPost.callOnClick()
                dialog.dismiss()
            },
            negativeText = R.string.discard,
            negativeClickListener = { dialog, _ ->
                dialog.dismiss()
                requireActivity().finish()
            })
    }

    private fun onBackPressedHandler(onClickFunction: () -> Unit) = try {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = onClickFunction()
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    } catch (e: Exception) {
        e.printStackTrace()
    }

    private fun showSearchList(flag: Boolean) {
        if (flag) {
            binding.rvSearchScripResult.visible()
            binding.clgData.gone()
        } else {
            binding.rvSearchScripResult.gone()
            binding.clgData.visible()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
