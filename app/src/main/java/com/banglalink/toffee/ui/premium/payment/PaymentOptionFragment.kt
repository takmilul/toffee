package com.banglalink.toffee.ui.premium.payment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.ProviderIconCallback
import com.banglalink.toffee.databinding.ButtomSheetPaymentOptionBinding
import com.banglalink.toffee.enums.PageType
import com.banglalink.toffee.extension.safeClick
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.ChildDialogFragment
import com.banglalink.toffee.ui.home.LandingPageViewModel
import com.banglalink.toffee.ui.premium.PremiumContentAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class PaymentOptionFragment : ChildDialogFragment(), ProviderIconCallback<ChannelInfo> {
    private var _binding: ButtomSheetPaymentOptionBinding?=null
    private val binding get() = _binding!!
    private lateinit var mAdapter: PaymentOptionAdapter
    private val landingPageViewModel by activityViewModels<LandingPageViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ButtomSheetPaymentOptionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAdapter = PaymentOptionAdapter(this)

        with(binding.paymentMethodList){

            adapter=mAdapter

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
        findNavController().navigate(R.id.action_payment_to_pack)
//        findNavController().navigate(R.id.action_payment_to_trail)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}