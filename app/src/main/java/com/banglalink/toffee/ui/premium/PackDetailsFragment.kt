package com.banglalink.toffee.ui.premium

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import coil.load
import com.banglalink.toffee.R
import com.banglalink.toffee.data.network.response.PremiumPack
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
    
    private var pack: PremiumPack? = null
    private var _binding: FragmentPackDetailsBinding? = null
    private val binding get() = _binding!!
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
        pack = viewModel.selectedPack.value
        
        pack?.let {
            observePremiumPackDetail()
            viewModel.getPremiumPackDetail(it.id)
            with(binding) {
                packBannerImageView.load(it.packImage)
                packStatusIcon.load(if (it.isPackPurchased) R.drawable.ic_premium_activated else R.drawable.ic_premium)
                packNameTextView.text = it.packTitle
                packPriceTextView.text = it.packSubtitle
                packExpiryDateTextView.text = it.expiryDate
                packPurchaseMsgTextView.text = it.packDetail
                footerBuyNowBar.isVisible = !it.isPackPurchased
                footerPaymentStatus.isVisible = it.isPackPurchased
                with(binding) {
                    payNowButton.safeClick({
                        requireActivity().checkVerification {
                            observePaymentStatus()
                            homeViewModel.getPackStatus()
                        }
                    })
                }
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
                    if (pack != null) {
                        it.data.find {
                            try {
                                it.packId == pack!!.id && it.isActive && mPref.getSystemTime().before(Utils.getDate(it.expiryDate))
                            } catch (e: Exception) {
                                false
                            }
                        }?.let {activePack ->
                            val purchasedPack = pack!!.copy(
                                isPackPurchased = activePack.isActive,
                                expiryDate = "Expires on ${Utils.formatPackExpiryDate(activePack.expiryDate)}",
                                packDetail = if (pack!!.isAvailableFreePeriod == 1) activePack.packDetail else "You have bought ${activePack.packDetail} pack"
                            )
                            viewModel.selectedPack.value = purchasedPack
                            findNavController().popBackStack(R.id.packDetailsFragment, true)
                            findNavController().navigate(R.id.packDetailsFragment)
                        } ?: run {
                            observePaymentMethodList()
                            viewModel.getPackPaymentMethodList(pack!!.id)
                        }
                    }
                }
                is Failure -> {
                    requireContext().showToast(it.error.msg)
                }
            }
        }
    }
    
    private fun observePaymentMethodList() {
        observe(viewModel.paymentMethodState) {
            when(it) {
                is Success -> {
                    findNavController().navigate(R.id.bottomSheetPaymentMethods, bundleOf("paymentMethods" to it.data))
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