package com.banglalink.toffee.ui.premium

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import coil.load
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentPackDetailsBinding
import com.banglalink.toffee.extension.checkVerification
import com.banglalink.toffee.extension.doIfNotNullOrEmpty
import com.banglalink.toffee.extension.hide
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.safeClick
import com.banglalink.toffee.extension.show
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.home.HomeViewModel
import com.banglalink.toffee.util.Utils

class PackDetailsFragment : BaseFragment() {
    
    private var _binding: FragmentPackDetailsBinding? = null
    val binding get() = _binding!!
    private val viewModel by activityViewModels<PremiumViewModel>()
    private val homeViewModel by activityViewModels<HomeViewModel>()
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPackDetailsBinding.inflate(layoutInflater)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.progressBar.load(R.drawable.content_loader)
        changeToolbarIcon()
        if (viewModel.selectedPack.value?.isPackPurchased == false) {
            checkPackPurchased()
        }
        binding.data = viewModel.selectedPack.value
        
        observePaymentStatus()
        observePremiumPackDetail()
        
        viewModel.selectedPack.value?.let {
            viewModel.getPremiumPackDetail(it.id)
            with(binding) {
                payNowButton.safeClick({
                    requireActivity().checkVerification {
                        homeViewModel.getPackStatus()
                    }
                })
            }
        } ?: run { 
            binding.progressBar.hide()
            requireContext().showToast(getString(R.string.try_again_message))
        }
        
//        findNavController()?.navigate(R.id.startWatchingDialog)
    }
    
    private fun observePaymentStatus() {
        observe(homeViewModel.activePackListLiveData) {
            when(it) {
                is Success -> {
                    mPref.activePremiumPackList.value = it.data
                    val isPurchased = checkPackPurchased()
                    if (!isPurchased) {
                        viewModel.selectedPack.value?.id?.let {
                            observePaymentMethodList()
                            viewModel.getPackPaymentMethodList(it)
                        } ?: requireContext().showToast(getString(R.string.try_again_message))
                    }
                }
                is Failure -> {
                    requireContext().showToast(it.error.msg)
                }
            }
        }
    }
    
    private fun checkPackPurchased(): Boolean {
        return if (mPref.isVerifiedUser) {
            viewModel.selectedPack.value?.let { pack ->
                mPref.activePremiumPackList.value?.find {
                    try {
                        it.packId == pack.id && it.isActive && mPref.getSystemTime().before(Utils.getDate(it.expiryDate))
                    } catch (e: Exception) {
                        false
                    }
                }?.let {
                    viewModel.selectedPack.value = pack.copy(
                        isPackPurchased = it.isActive,
                        expiryDate = "Expires on ${Utils.formatPackExpiryDate(it.expiryDate)}",
                        packDetail = if (pack.isAvailableFreePeriod == 1) it.packDetail else "You have bought ${it.packDetail} pack"
                    )
                    binding.data = viewModel.selectedPack.value
                    true
                } ?: false
            } ?: false
        } else false
    }
    
    private fun observePaymentMethodList() {
        observe(viewModel.paymentMethodState) {
            when(it) {
                is Success -> {
                    viewModel.paymentMethod.value = it.data
                    findNavController().navigate(R.id.bottomSheetPaymentMethods, bundleOf("paymentMethods" to it.data), navOptions {
                        popUpTo(R.id.bottomSheetPaymentMethods) { inclusive = true }
                    })
                }
                is Failure -> {
                    requireContext().showToast(it.error.msg)
                }
            }
        }
    }
    
    private fun observePremiumPackDetail() {
        observe(viewModel.packDetailState) { response ->
            when (response) {
                is Success -> {
                    binding.progressBar.hide()
                    response.data?.linearChannelList?.doIfNotNullOrEmpty {
                        binding.premiumChannelGroup.show()
                        viewModel.setLinearContentState(it.toList())
                    }
                    response.data?.vodChannelList?.doIfNotNullOrEmpty {
                        binding.premiumContentGroup.show()
                        viewModel.setVodContentState(it.toList())
                    }
                }
                is Failure -> {
                    binding.progressBar.hide()
                    requireActivity().showToast(response.error.msg)
                }
            }
        }
    }
    
    private fun changeToolbarIcon() {
        val toolbar = activity?.findViewById<Toolbar>(R.id.toolbar)
        toolbar?.setNavigationIcon(R.drawable.ic_arrow_back)
        toolbar?.setNavigationOnClickListener {
            runCatching {
                findNavController().popBackStack()
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}