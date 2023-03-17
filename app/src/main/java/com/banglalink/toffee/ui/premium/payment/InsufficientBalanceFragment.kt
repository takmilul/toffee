package com.banglalink.toffee.ui.premium.payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.data.network.request.RechargeByBkashRequest
import com.banglalink.toffee.databinding.FragmentInsufficientBalanceBinding
import com.banglalink.toffee.extension.navigatePopUpTo
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.safeClick
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.ui.common.ChildDialogFragment
import com.banglalink.toffee.ui.premium.PremiumViewModel
import com.banglalink.toffee.ui.widget.ToffeeProgressDialog
import com.banglalink.toffee.util.unsafeLazy

class InsufficientBalanceFragment : ChildDialogFragment() {
    
    private var _binding: FragmentInsufficientBalanceBinding? = null
    private val binding get() = _binding!!
    private val viewModel by activityViewModels<PremiumViewModel>()
    private val progressDialog by unsafeLazy { ToffeeProgressDialog(requireContext()) }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentInsufficientBalanceBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buyWithRecharge.safeClick({
            callAndObserveRechargeByBkash()
        })
        binding.backImg.safeClick({ findNavController().popBackStack() })
        binding.goToHome.safeClick({
            runCatching {
                requireActivity().findNavController(R.id.home_nav_host).navigatePopUpTo(R.id.menu_feed)
            }
        })
    }

    private fun callAndObserveRechargeByBkash() {
        progressDialog.show()
        if (viewModel.selectedPremiumPack.value != null && viewModel.selectedDataPackOption.value != null) {
            observe(viewModel.rechargeByBkashUrlLiveData) { it ->
                progressDialog.dismiss()
                when(it) {
                    is Success -> {
                        it.data?.let {
                            if (it.statusCode != 200) {
                                requireContext().showToast(getString(R.string.try_again_message))
                                return@observe
                            }
                            val args = bundleOf(
                                "myTitle" to "Pack Details",
                                "url" to it.data?.bKashWebUrl.toString(),
                                "isHideBackIcon" to false,
                                "isHideCloseIcon" to true,
                                "isBkashBlRecharge" to true,
                            )
                            findNavController().navigatePopUpTo(R.id.paymentWebViewDialog, args)
                        } ?: requireContext().showToast(getString(R.string.try_again_message))
                    }
                    is Failure -> {
                        requireContext().showToast(it.error.msg)
                    }
                }
            }
            val selectedPremiumPack = viewModel.selectedPremiumPack.value
            val selectedDataPackOption = viewModel.selectedDataPackOption.value

            val request = RechargeByBkashRequest(
                customerId = mPref.customerId,
                password = mPref.password,
                paymentMethodId = selectedDataPackOption?.paymentMethodId ?: 0,
                packId = selectedPremiumPack?.id ?: 0,
                packTitle = selectedPremiumPack?.packTitle,
                dataPackId = selectedDataPackOption?.dataPackId ?: 0,
                dataPackCode = selectedDataPackOption?.packCode,
                dataPackDetail = selectedDataPackOption?.packDetails,
                dataPackPrice = selectedDataPackOption?.packPrice ?: 0,
                isPrepaid = selectedDataPackOption?.isPrepaid ?: 1
            )
            viewModel.getRechargeByBkashUrl(request)
        } else {
            progressDialog.dismiss()
            requireContext().showToast(getString(R.string.try_again_message))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        progressDialog.dismiss()
        _binding = null
    }
}