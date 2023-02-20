package com.banglalink.toffee.ui.premium.payment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.ProviderIconCallback
import com.banglalink.toffee.databinding.ButtomSheetChoosePackBinding
import com.banglalink.toffee.databinding.ButtomSheetPaymentOptionBinding
import com.banglalink.toffee.enums.PageType
import com.banglalink.toffee.extension.safeClick

import com.banglalink.toffee.model.ChannelInfo

import com.banglalink.toffee.ui.common.ChildDialogFragment
import com.banglalink.toffee.ui.home.LandingPageViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class PaymentChoosePackFragment : ChildDialogFragment(), ProviderIconCallback<ChannelInfo> {

    private var _binding: ButtomSheetChoosePackBinding?=null
    private val binding get() = _binding!!
    private lateinit var mAdapter: ChoosePackAdapter
    private val landingPageViewModel by activityViewModels<LandingPageViewModel>()



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ButtomSheetChoosePackBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAdapter = ChoosePackAdapter(this)

        with(binding.packList){

            adapter=mAdapter
            binding.backImg.safeClick( {findNavController().popBackStack()})

            binding.termsAndConditionsTwo.safeClick({
                showTermsAndConditionDialog()
            })

        }

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