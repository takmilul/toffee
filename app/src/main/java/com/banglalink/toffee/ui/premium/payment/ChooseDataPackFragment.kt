package com.banglalink.toffee.ui.premium.payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.data.network.response.PackPaymentMethod
import com.banglalink.toffee.databinding.ButtomSheetChooseDataPackBinding
import com.banglalink.toffee.extension.*
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.ChildDialogFragment
import com.banglalink.toffee.ui.premium.DataPackAdapter
import com.banglalink.toffee.ui.premium.PremiumViewModel

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
        hidePaymentOption()
        viewModel.selectedPaymentMethod.value=null
        val paymentName = arguments?.getString("paymentName", "") ?: ""
        viewModel.paymentMethod.value?.let { paymentTypes ->
            val packPaymentMethodList = mutableListOf<PackPaymentMethod>()
            val prePaid = paymentTypes.bl?.pREPAID
            val postPaid = paymentTypes.bl?.pOSTPAID
            val bkash = paymentTypes.bkash?.dataPacks
            
            if(paymentName=="bKash"){
                packPaymentMethodList.clear()
                if (bkash != null && bkash.isNotEmpty()) {
                    packPaymentMethodList.addAll(bkash)
                }
            } else if(paymentName=="blPack"){
                packPaymentMethodList.clear()
                if (prePaid != null && prePaid.isNotEmpty()) {
                    packPaymentMethodList.add(PackPaymentMethod(listTitle = "Banglalink Prepaid Packs"))
                    packPaymentMethodList.addAll(prePaid)
                }
    
                if (postPaid != null && postPaid.isNotEmpty()) {
                    packPaymentMethodList.add(PackPaymentMethod(listTitle = "Banglalink PostPaid Packs"))
                    packPaymentMethodList.addAll(postPaid)
                }
            }
            
            packPaymentMethodList.let {
                mAdapter = DataPackAdapter(requireContext(),this)
                binding.recyclerView.adapter = mAdapter
                mAdapter.addAll(it.toList())
            }
        }
        observe(viewModel.selectedPaymentMethod){
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
        binding.recyclerView.setPadding(0,0,0,24)
        
        binding.backImg.safeClick({ findNavController().popBackStack() })
        binding.termsAndConditionsTwo.safeClick({ showTermsAndConditionDialog() })
        
        binding.buyNow.setOnClickListener {
            viewModel.purchaseDataPack()
        }
    }
    
    override fun onItemClicked(item: PackPaymentMethod) {
        super.onItemClicked(item)
        viewModel.selectedPaymentMethod.value = item
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    fun showPaymentOption() {
        binding.recyclerView.setPadding(0,0,0,8)
        binding.termsAndConditionsOne.show()
        binding.termsAndConditionsTwo.show()
        binding.buyNow.show()
        
    }
    fun hidePaymentOption(){
        binding.recyclerView.setPadding(0,0,0,24)
        binding.termsAndConditionsOne.hide()
        binding.termsAndConditionsTwo.hide()
        binding.buyNow.hide()
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