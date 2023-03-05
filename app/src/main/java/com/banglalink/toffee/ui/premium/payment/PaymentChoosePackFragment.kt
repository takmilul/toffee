package com.banglalink.toffee.ui.premium.payment

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.Constants
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.ProviderIconCallback
import com.banglalink.toffee.databinding.ButtomSheetChoosePackBinding
import com.banglalink.toffee.enums.PageType
import com.banglalink.toffee.extension.launchActivity
import com.banglalink.toffee.extension.safeClick
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.bkash.api.ApiInterface
import com.banglalink.toffee.ui.bkash.api.BkashApiClient
import com.banglalink.toffee.ui.bkash.model.CreatePaymentBodyRequest
import com.banglalink.toffee.ui.bkash.model.CreatePaymentResponse
import com.banglalink.toffee.ui.bkash.model.GrantTokenBodyRequest
import com.banglalink.toffee.ui.bkash.model.GrantTokenResponse
import com.banglalink.toffee.ui.common.ChildDialogFragment
import com.banglalink.toffee.ui.common.Html5PlayerViewActivity
import com.banglalink.toffee.ui.home.LandingPageViewModel
import com.banglalink.toffee.ui.widget.ToffeeProgressDialog
import com.banglalink.toffee.util.unsafeLazy
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PaymentChoosePackFragment : ChildDialogFragment(), ProviderIconCallback<ChannelInfo> {

    private var _binding: ButtomSheetChoosePackBinding? = null
    private val binding get() = _binding!!
    private lateinit var mAdapter: ChoosePackAdapter
    private val landingPageViewModel by activityViewModels<LandingPageViewModel>()
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ButtomSheetChoosePackBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAdapter = ChoosePackAdapter(requireContext(), this)

        with(binding.packList) {
            adapter = mAdapter
            binding.backImg.safeClick({ findNavController().popBackStack() })
            binding.termsAndConditionsTwo.safeClick({ showTermsAndConditionDialog() })
        }

        binding.buyNow.safeClick({
            progressDialog.show()
            grantBkashToken()
        })

        observeList()
    }

    private fun observeList() {
        viewLifecycleOwner.lifecycleScope.launch {
            val content = if (landingPageViewModel.pageType.value == PageType.Landing) {
                landingPageViewModel.loadLandingEditorsChoiceContent()
            }
            else {
                landingPageViewModel.loadEditorsChoiceContent()
            }
            content.collectLatest {
                mAdapter.submitData(it)
            }
        }
    }

    private fun grantBkashToken() {
        val apiService = BkashApiClient.client!!.create(ApiInterface::class.java)
        val call: Call<GrantTokenResponse> = apiService.postGrantToken(
            mPref.bkashGrantTokenUrl,
            bkashSandboxUsername,
            bkashSandboxPassword,
            GrantTokenBodyRequest(
                bkashSandboxAppKey,
                bkashSandboxAppSecret
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
        val apiService = BkashApiClient.client!!.create(ApiInterface::class.java)
        val call: Call<CreatePaymentResponse> = apiService.postPaymentCreate(
            mPref.bkashCreateUrl,
            "Bearer $sessionIdToken",
            bkashSandboxAppKey,
            CreatePaymentBodyRequest(
                "0011",
                "01770618575",
                "${mPref.bkashCallbackUrl}1/1/${mPref.customerId}/${mPref.password}/${mPref.phoneNumber}/${mPref.isBanglalinkNumber}/${Constants.DEVICE_TYPE}/${cPref.deviceId}/${mPref.netType}/${"android_"+ Build.VERSION.RELEASE}/${cPref.appVersionName}/${cPref.appTheme}",
                "MI05MID54RF09123456One",
                "30",
                "BDT",
                "sale",
                "Inv0124",
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

    override fun onItemClicked(item: ChannelInfo) {
        binding.termsAndConditionsOne.visibility=View.VISIBLE
        binding.termsAndConditionsTwo.visibility=View.VISIBLE
        binding.buyNow.visibility=View.VISIBLE

    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showTermsAndConditionDialog() {
        findNavController().navigate(R.id.htmlPageViewDialog, bundleOf(
            "myTitle" to getString(R.string.terms_and_conditions),
            "url" to mPref.termsAndConditionUrl
        )
        )
    }
}