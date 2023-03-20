package com.banglalink.toffee.ui.premium

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import coil.load
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentPremiumPackDetailsBinding
import com.banglalink.toffee.extension.*
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.widget.ToffeeProgressDialog
import com.banglalink.toffee.util.Utils
import com.banglalink.toffee.util.unsafeLazy

class PremiumPackDetailsFragment : BaseFragment() {
    
    private var isFreeTrialOver = false
    private var _binding: FragmentPremiumPackDetailsBinding? = null
    val binding get() = _binding!!
    private val viewModel by activityViewModels<PremiumViewModel>()
    private val progressDialog by unsafeLazy { ToffeeProgressDialog(requireContext()) }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPremiumPackDetailsBinding.inflate(layoutInflater)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.progressBar.load(R.drawable.content_loader)
        onBackIconClicked()
        requireActivity().title = "Pack Details"
        
        if (viewModel.selectedPremiumPack.value?.isPackPurchased == false) {
            checkPackPurchased()
        }
        binding.data = viewModel.selectedPremiumPack.value
        
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
                        viewModel.getPackStatus()
                    }
                })
            }
        } ?: run { 
            binding.progressBar.hide()
            requireContext().showToast(getString(R.string.try_again_message))
        }
        
        observe(mPref.packDetailsPageRefreshRequired){
            if(it == true){
                findNavController().navigatePopUpTo(R.id.packDetailsFragment)
                findNavController().navigateTo(R.id.startWatchingDialog)
            }
        }
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
                    findNavController().navigatePopUpTo(
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

                    response.data?.linearChannelList?.doIfNotNullOrEmpty {

                        binding.premiumChannelGroup.show()
                        binding.emptyView.hide()
                        viewModel.setLinearContentState(it.toList())
                    }
                    response.data?.vodChannelList?.doIfNotNullOrEmpty {
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
    
    private fun onBackIconClicked() {
        val toolbar = requireActivity().findViewById<Toolbar>(R.id.toolbar)
        toolbar?.setNavigationOnClickListener {
            runCatching {
                findNavController().popBackStack()
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        progressDialog.dismiss()
        _binding = null
    }
}