package com.banglalink.toffee.ui.premium.payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.data.network.response.PackPaymentMethod
import com.banglalink.toffee.databinding.ButtomSheetChooseDataPackBinding
import com.banglalink.toffee.extension.*
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.PaymentMethodBean
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.ChildDialogFragment
import com.banglalink.toffee.ui.premium.DataPackAdapter
import com.banglalink.toffee.ui.premium.PremiumChannelAdapter
import com.banglalink.toffee.ui.premium.PremiumViewModel
import okhttp3.internal.notify

class ChooseDataPackFragment : ChildDialogFragment(), BaseListItemCallback<PackPaymentMethod> {
    private lateinit var mAdapter: DataPackAdapter
    private var _binding: ButtomSheetChooseDataPackBinding? = null
    private val binding get() = _binding!!
    private val viewModel by activityViewModels<PremiumViewModel>()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ButtomSheetChooseDataPackBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAdapter = DataPackAdapter(this)
        binding.recyclerView.adapter = mAdapter
        
        viewModel.paymentMethod.value?.let { paymentTypes ->
            val packPaymentMethodList = mutableListOf<PackPaymentMethod>()
            val prePaid = paymentTypes.bl?.pREPAID
            val postPaid = paymentTypes.bl?.pOSTPAID
            if (prePaid != null && prePaid.isNotEmpty()) {
                packPaymentMethodList.add(PackPaymentMethod(listTitle = "Banglalink Prepaid Packs"))
                prePaid.forEach {
                    packPaymentMethodList.add(
                        PackPaymentMethod(
                            dataPackId = it.dataPackId,
                            paymentMethodId = it.paymentMethodId,
                            isNonBlFree = it.isNonBlFree,
                            packCode = it.packCode,
                            packDetails = it.packDetails,
                            packPrice = it.packPrice,
                            packDuration = it.packDuration,
                            sortByCode = it.sortByCode,
                            isPrepaid = it.isPrepaid,
                        )
                    )
                }
            }
            
            if (postPaid != null && postPaid.isNotEmpty()) {
                packPaymentMethodList.add(PackPaymentMethod(listTitle = "Banglalink PostPaid Packs"))
                postPaid.forEach {
                    packPaymentMethodList.add(
                        PackPaymentMethod(
                            dataPackId = it.dataPackId,
                            paymentMethodId = it.paymentMethodId,
                            isNonBlFree = it.isNonBlFree,
                            packCode = it.packCode,
                            packDetails = it.packDetails,
                            packPrice = it.packPrice,
                            packDuration = it.packDuration,
                            sortByCode = it.sortByCode,
                            isPrepaid = it.isPrepaid,
                        )
                    )
                }
            }
    
            packPaymentMethodList.let {
                mAdapter.addAll(it.toList())
            }
        }
        observe(viewModel.selectedPaymentMethod2){
            if (it.listTitle == null) {
                mAdapter.setSelectedItem(it)
                showPaymentOption()
            }
        }
        observe(viewModel.packPurchaseResponseCode) {
            when (it) {
                is Resource.Success -> {
                    requireContext().showToast(it.toString())
                    val args = Bundle().apply {
                        putInt("errorLogicCode", it.data.status ?: 0)
                    }
                    findNavController().navigate(R.id.dataPackPurchaseDialog,args)
                }
                is Resource.Failure -> {
                }
            }
        }
        
        binding.backImg.safeClick({ findNavController().popBackStack() })
        binding.termsAndConditionsTwo.safeClick({ showTermsAndConditionDialog() })
        
        binding.buyNow.setOnClickListener {
            viewModel.purchaseDataPack()
        }
    }
    
    override fun onItemClicked(item: PackPaymentMethod) {
        super.onItemClicked(item)
        viewModel.selectedPaymentMethod2.value = item
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    fun showPaymentOption() {
        binding.termsAndConditionsOne.show()
        binding.termsAndConditionsTwo.show()
        binding.buyNow.show()
        
    }
    
    private fun showTermsAndConditionDialog() {
        findNavController().navigate(
            R.id.htmlPageViewDialog, bundleOf(
                "myTitle" to getString(R.string.terms_and_conditions),
                "url" to mPref.termsAndConditionUrl
            )
        )
    }
}