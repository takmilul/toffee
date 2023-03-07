package com.banglalink.toffee.ui.premium.payment

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.Constants
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.data.network.request.CreatePaymentRequest
import com.banglalink.toffee.data.network.request.ExecutePaymentRequest
import com.banglalink.toffee.data.network.response.PackPaymentMethod
import com.banglalink.toffee.databinding.ButtomSheetChooseDataPackBinding
import com.banglalink.toffee.extension.*
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.ChildDialogFragment
import com.banglalink.toffee.ui.common.Html5PlayerViewActivity
import com.banglalink.toffee.ui.premium.DataPackAdapter
import com.banglalink.toffee.ui.premium.PremiumViewModel
import com.banglalink.toffee.ui.widget.ToffeeProgressDialog
import com.banglalink.toffee.util.unsafeLazy

class ChooseDataPackFragment : ChildDialogFragment(), BaseListItemCallback<PackPaymentMethod> {
    var sessionIdToken = ""
    private lateinit var mAdapter: DataPackAdapter
    private var _binding: ButtomSheetChooseDataPackBinding? = null
    private val binding get() = _binding!!
    private val viewModel by activityViewModels<PremiumViewModel>()
    private var paymentName: String? = null
    private var bkashPaymentID: String? = null
    private val progressDialog by unsafeLazy {
        ToffeeProgressDialog(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ButtomSheetChooseDataPackBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hidePaymentOption()
        viewModel.selectedPaymentMethod.value=null
        paymentName = arguments?.getString("paymentName", "") ?: ""
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
                is Success -> {
                    requireContext().showToast(it.toString())
                    val args = Bundle().apply {
                        putInt("errorLogicCode", it.data.status ?: 0)
                    }
                    findNavController().navigate(R.id.dataPackPurchaseDialog, args)
                }
                is Failure -> {
                    requireContext().showToast(it.error.msg)
                }
            }
        }
        binding.recyclerView.setPadding(0,0,0,24)

        binding.backImg.safeClick({ findNavController().popBackStack() })
        binding.termsAndConditionsTwo.safeClick({ showTermsAndConditionDialog() })
        
        binding.buyNow.setOnClickListener {
            if (paymentName == "bKash") {
                progressDialog.show()
                grantBkashToken()
            } else if (paymentName == "blPack") {
                viewModel.purchaseDataPack()
            }
        }
    }

    private fun grantBkashToken() {
        observe(viewModel.bKashGrandTokenState) { response ->
            when (response) {
                is Success -> {
                    progressDialog.hide()
                    sessionIdToken = response.data.idToken.toString()
                    if (response.data.statusCode != "0000") {
                        requireContext().showToast(response.data.statusMessage)
                    }
                    createBkashPayment()
                }
                is Failure -> {
                    requireContext().showToast("Something went to wrong")
                    progressDialog.hide()
                }
            }
        }
        viewModel.bkashGrandToken()
    }

    //    URL/bkash/callback/{packageId}/{dataPackId}/{subscriberId}/{password}/{msisdn}/{isBlNumber}/{deviceType}/{deviceId}/{netType}/{osVersion}/{appVersion}/{appMode}
    private fun createBkashPayment() {
        val callBackUrl =
            "${mPref.bkashCallbackUrl}${viewModel.selectedPack.value?.id}/${viewModel.selectedPaymentMethod.value?.dataPackId}/${mPref.customerId}/${mPref.password}/${mPref.phoneNumber}/${mPref.isBanglalinkNumber}/${Constants.DEVICE_TYPE}/${cPref.deviceId}/${mPref.netType}/${"android_" + Build.VERSION.RELEASE}/${cPref.appVersionName}/${cPref.appTheme}"
        val amount = viewModel.selectedPaymentMethod.value?.packPrice.toString()

        observe(viewModel.bKashCreatePaymentState) { response ->
            when (response) {
                is Success -> {
                    requireContext().showToast(response.data.message)
                    bkashPaymentID = response.data.paymentID
                    val args = Bundle().apply {
                        putString("myTitle", "Pack Details")
                        putString("Token", sessionIdToken)
                        putString("PaymentID", bkashPaymentID)
                        putString("url", response.data.bkashURL)
                    }
                    findNavController().navigate(R.id.premiumPageViewDialog, args)
                    progressDialog.hide()
                }
                is Failure -> {
                    requireContext().showToast("Something went to wrong")
                    progressDialog.hide()
                }
            }
        }
        viewModel.bkashCreatePayment(
            sessionIdToken, CreatePaymentRequest(
                mode = "0011",
                payerReference = "01770618575",
                callbackURL = callBackUrl,
                merchantAssociationInfo = "MI05MID54RF09123456One",
                amount = amount,
                currency = "BDT",
                intent = "sale",
                merchantInvoiceNumber = "Inv0124",
            )
        )
    }
    
    override fun onItemClicked(item: PackPaymentMethod) {
        super.onItemClicked(item)
        viewModel.selectedPaymentMethod.value = item
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}