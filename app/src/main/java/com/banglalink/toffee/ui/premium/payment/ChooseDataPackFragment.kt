package com.banglalink.toffee.ui.premium.payment

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.Constants
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.data.network.response.PackPaymentMethod
import com.banglalink.toffee.databinding.ButtomSheetChooseDataPackBinding
import com.banglalink.toffee.extension.*
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.PaymentMethodBean
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.bkash.api.ApiInterface
import com.banglalink.toffee.ui.bkash.api.BkashApiClient
import com.banglalink.toffee.ui.bkash.model.CreatePaymentBodyRequest
import com.banglalink.toffee.ui.bkash.model.CreatePaymentResponse
import com.banglalink.toffee.ui.bkash.model.GrantTokenBodyRequest
import com.banglalink.toffee.ui.bkash.model.GrantTokenResponse
import com.banglalink.toffee.ui.common.ChildDialogFragment
import com.banglalink.toffee.ui.common.Html5PlayerViewActivity
import com.banglalink.toffee.ui.premium.DataPackAdapter
import com.banglalink.toffee.ui.premium.PremiumChannelAdapter
import com.banglalink.toffee.ui.premium.PremiumViewModel
import com.banglalink.toffee.ui.widget.ToffeeProgressDialog
import com.banglalink.toffee.util.unsafeLazy
import okhttp3.internal.notify
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChooseDataPackFragment : ChildDialogFragment(), BaseListItemCallback<PackPaymentMethod> {
    private lateinit var mAdapter: DataPackAdapter
    private var _binding: ButtomSheetChooseDataPackBinding? = null
    private val binding get() = _binding!!
    private val viewModel by activityViewModels<PremiumViewModel>()
    private var paymentName : String? = null
    private val progressDialog by unsafeLazy {
        ToffeeProgressDialog(requireContext())
    }
    companion object {
        var bkashSandboxUsername = "sandboxTokenizedUser01"
        var bkashSandboxPassword = "sandboxTokenizedUser12345"
        var bkashSandboxAppKey = "7epj60ddf7id0chhcm3vkejtab"
        var bkashSandboxAppSecret = "18mvi27h9l38dtdv110rq5g603blk0fhh5hg46gfb27cp2rbs66f"
        var sessionIdToken = ""
    }
    
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
        viewModel.selectedPaymentMethod2.value=null
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
                mAdapter = DataPackAdapter(this)
                binding.recyclerView.adapter = mAdapter
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
            if (paymentName == "bKash"){
                progressDialog.show()
                grantBkashToken()
            }
            else if (paymentName == "blPack"){
                viewModel.purchaseDataPack()
            }
        }
    }

    private fun grantBkashToken() {
        val apiService = BkashApiClient.client!!.create(ApiInterface::class.java)
        val call: Call<GrantTokenResponse> = apiService.postGrantToken(
            url = mPref.bkashGrantTokenUrl,
            bkashUsername = bkashSandboxUsername,
            bkashPassword = bkashSandboxPassword,
            GrantTokenBodyRequest(
                appKey = bkashSandboxAppKey,
                appSecret = bkashSandboxAppSecret
            )
        )
        call.enqueue(object : Callback<GrantTokenResponse> {
            override fun onResponse(call: Call<GrantTokenResponse>, response: Response<GrantTokenResponse>) {
                if (response.isSuccessful) {
                    progressDialog.hide()
                    sessionIdToken = response.body()?.idToken.toString()
                    if (response.body()?.statusCode != "0000"){
                        requireContext().showToast(response.body()?.statusMessage)
                    }
                    createBkashPayment()
                }
            }

            override fun onFailure(call: Call<GrantTokenResponse>, t: Throwable) {
                requireContext().showToast("Something went to wrong")
                progressDialog.hide()
            }
        })
    }
    //    URL/bkash/callback/{packageId}/{dataPackId}/{subscriberId}/{password}/{msisdn}/{isBlNumber}/{deviceType}/{deviceId}/{netType}/{osVersion}/{appVersion}/{appMode}
    private fun createBkashPayment() {
        val callBackUrl = "${mPref.bkashCallbackUrl}${viewModel.selectedPack.value?.id}/${viewModel.selectedPaymentMethod2.value?.dataPackId}/${mPref.customerId}/${mPref.password}/${mPref.phoneNumber}/${mPref.isBanglalinkNumber}/${Constants.DEVICE_TYPE}/${cPref.deviceId}/${mPref.netType}/${"android_"+ Build.VERSION.RELEASE}/${cPref.appVersionName}/${cPref.appTheme}"
        val amount = viewModel.selectedPaymentMethod2.value?.packPrice.toString()
        val apiService = BkashApiClient.client!!.create(ApiInterface::class.java)
        val call: Call<CreatePaymentResponse> = apiService.postPaymentCreate(
            url = mPref.bkashCreateUrl,
            authorization = "Bearer $sessionIdToken",
            xAppKey = bkashSandboxAppKey,
            CreatePaymentBodyRequest(
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
        call.enqueue(object : Callback<CreatePaymentResponse> {
            override fun onResponse(call: Call<CreatePaymentResponse>, response: Response<CreatePaymentResponse>) {
                if (response.isSuccessful) {
                    requireContext().showToast(response.body()?.message)
                    requireActivity().launchActivity<Html5PlayerViewActivity> {
                        putExtra(Html5PlayerViewActivity.CONTENT_URL, response.body()?.bkashURL.toString())
                        putExtra(Html5PlayerViewActivity.TITLE, "Pack Details")
                    }
                }
            }

            override fun onFailure(call: Call<CreatePaymentResponse>, t: Throwable) {
                requireContext().showToast("Something went to wrong")
                progressDialog.hide()
            }
        })
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
    fun hidePaymentOption(){
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