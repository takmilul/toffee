package com.banglalink.toffee.ui.premium

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import coil.load
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentPremiumPackDetailsBinding
import com.banglalink.toffee.extension.checkVerification
import com.banglalink.toffee.extension.getBalloon
import com.banglalink.toffee.extension.hide
import com.banglalink.toffee.extension.ifNotNullOrEmpty
import com.banglalink.toffee.extension.navigatePopUpTo
import com.banglalink.toffee.extension.navigateTo
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.safeClick
import com.banglalink.toffee.extension.show
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.showAlignBottom
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.home.HomeViewModel
import com.banglalink.toffee.ui.widget.ToffeeProgressDialog
import com.banglalink.toffee.util.Utils
import com.banglalink.toffee.util.unsafeLazy

class PremiumPackDetailsFragment : BaseFragment(){
    
    private var isFreeTrialOver = false
    private var _binding: FragmentPremiumPackDetailsBinding? = null
    val binding get() = _binding!!
    private val viewModel by activityViewModels<PremiumViewModel>()
    private val homeViewModel by activityViewModels<HomeViewModel>()
    private val progressDialog by unsafeLazy { ToffeeProgressDialog(requireContext()) }

    private var tooltip: Any? = null
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPremiumPackDetailsBinding.inflate(layoutInflater)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.progressBar.load(R.drawable.content_loader)
        
        requireActivity().title = "Pack Details"
        
        if (viewModel.selectedPremiumPack.value?.isPackPurchased == false) {
            checkPackPurchased()
        }
        binding.isVerifiedUser = mPref.isVerifiedUser
        binding.data = viewModel.selectedPremiumPack.value
        
        observeMnpStatus()
        observePaymentMethodList()
        observePackStatus()
        observePremiumPackDetail()
        
        viewModel.selectedPremiumPack.value?.let {
            isFreeTrialOver = it.isAvailableFreePeriod == 1 && it.isPurchaseAvailable != 1 && mPref.activePremiumPackList.value?.any { activePack ->
                it.id == activePack.packId && activePack.isTrialPackUsed
            } ?: false
            
            viewModel.getPremiumPackDetail(it.id)
            
            with(binding) {
                if (isFreeTrialOver && mPref.isVerifiedUser) {
                    payNowButton.alpha = 0.5f
                    payNowButton.isEnabled = false
                    payNowButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.disabled_text_color))
                }
                
                payNowButton.safeClick({
                    requireActivity().checkVerification {
                        progressDialog.show()
                        if (!mPref.isMnpStatusChecked && mPref.isVerifiedUser && mPref.isMnpCallForSubscription) {
                            homeViewModel.getMnpStatus()
                        }
                        else {
                            viewModel.selectedPremiumPack.value?.let {
                                viewModel.getPackStatus(0, it.id)
                            } ?: run {
                                progressDialog.hide()
                                requireContext().showToast(getString(R.string.try_again_message))
                            }
                        }
                    }
                })
            }
        } ?: run { 
            binding.progressBar.hide()
            requireContext().showToast(getString(R.string.try_again_message))
        }
        
        observe(mPref.packDetailsPageRefreshRequired){
            if(it == true) {
                findNavController().navigatePopUpTo(
                    resId = R.id.packDetailsFragment,
                    popUpTo = R.id.premiumPackListFragment,
                    inclusive = false
                )
                if (mPref.prePurchaseClickedContent.value == null) {
                    findNavController().navigateTo(R.id.startWatchingDialog)
                } else {
                    mPref.prePurchaseClickedContent.value = null
                }
            }
        }
        
        binding.infoIcon.safeClick({
            //https://github.com/douglasjunior/android-simple-tooltip
//            tooltip = SimpleTooltip.Builder(requireContext())
//                .anchorView(binding.infoIcon)
//                .text(R.string.subscription_history_tooltip_text)
//                .gravity(Gravity.TOP)
//                .animated(false)
//                .transparentOverlay(true)
//                .margin(0f)
//                .padding(16F.px)
//                .contentView(R.layout.tooltip_layout_subscription, R.id.tooltipText)
//                .arrowColor(ContextCompat.getColor(requireContext(), R.color.tooltip_bg_color))
//                .arrowHeight(10F.px)
//                .arrowWidth(14F.px)
//                .focusable(true)
//                .build()
//                .show()
            runCatching {
                it.showAlignBottom(
                    requireContext().getBalloon(
                        resources.getString(R.string.subscription_history_tooltip_text)
                    )
                )
            }
        })
    }
    
    private fun observePackStatus() {
        observe(viewModel.activePackListLiveData) {
            when(it) {
                is Success -> {
                    mPref.activePremiumPackList.value = it.data
                    val isPurchased = checkPackPurchased()
                    if (!isPurchased) {
                        viewModel.selectedPremiumPack.value?.id?.let {
                            viewModel.getPackPaymentMethodList(it)
                        } ?: run {
                            progressDialog.dismiss()
                            requireContext().showToast(getString(com.banglalink.toffee.R.string.try_again_message))
                        }
                    } else {
                        progressDialog.dismiss()
                        findNavController().navigatePopUpTo(R.id.packDetailsFragment)
                    }
                }
                is Failure -> {
                    progressDialog.dismiss()
                    requireContext().showToast(it.error.msg)
                }
            }
        }
    }
    
    private fun checkPackPurchased(): Boolean {
        return if (mPref.isVerifiedUser) {
            viewModel.selectedPremiumPack.value?.let { pack ->
                mPref.activePremiumPackList.value?.find {
                    try {
                        it.packId == pack.id && it.isActive && mPref.getSystemTime().before(Utils.getDate(it.expiryDate))
                    } catch (e: Exception) {
                        false
                    }
                }?.let {
                    viewModel.selectedPremiumPack.value = pack.copy(
                        isPackPurchased = it.isActive,
                        expiryDate = "Expires on ${Utils.formatPackExpiryDate(it.expiryDate)}",
                        packDetail = if (it.isTrialPackUsed) it.packDetail else "You have bought ${it.packDetail} pack"
                    )
                    binding.isVerifiedUser = mPref.isVerifiedUser
                    binding.data = viewModel.selectedPremiumPack.value
                    true
                } ?: false
            } ?: false
        } else false
    }
    
    private fun observePaymentMethodList() {
        observe(viewModel.paymentMethodState) {
            progressDialog.dismiss()
            when(it) {
                is Success -> {
                    viewModel.paymentMethod.value = it.data
                    findNavController().navigateTo(
                        resId = R.id.bottomSheetPaymentMethods,
                        args = bundleOf("paymentMethods" to it.data)
                    )
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

                    if (response.data!!.totalCount==0){
                        binding.premiumChannelGroup.hide()
                        binding.premiumContentGroup.hide()
                        binding.emptyView.show()

                    }

                    response.data?.linearChannelList?.ifNotNullOrEmpty {
                        binding.premiumChannelGroup.show()
                        binding.emptyView.hide()
                        viewModel.setLinearContentState(it.toList())
                    }
                    response.data?.vodChannelList?.ifNotNullOrEmpty {
                        Log.d("TAG", "observePremiumPackDetail: Four")
                        binding.premiumContentGroup.show()
                        binding.emptyView.hide()
                        viewModel.setVodContentState(it.toList())
                    }
                }
                is Failure -> {
                    Log.d("TAG", "observePremiumPackDetail: Five")
                    binding.progressBar.hide()
                    requireActivity().showToast(response.error.msg)
                }
            }
        }
    }
    
    private fun observeMnpStatus() {
        observe(homeViewModel.mnpStatusBeanLiveData) { response ->
            when (response) {
                is Success -> {
                    if (response.data?.mnpStatus == 200){
                        mPref.isMnpStatusChecked = true
                        viewModel.selectedPremiumPack.value?.let {
                            viewModel.getPackStatus(0, it.id)
                        } ?: requireContext().showToast(getString(R.string.try_again_message))
                    }
                }
                is Failure -> {
                    requireContext().showToast(response.error.msg)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        progressDialog.dismiss()
        _binding = null
    }
}