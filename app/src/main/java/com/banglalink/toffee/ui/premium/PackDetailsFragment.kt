package com.banglalink.toffee.ui.premium

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.banglalink.toffee.R
import com.banglalink.toffee.data.network.response.PremiumPack
import com.banglalink.toffee.databinding.FragmentPackDetailsBinding
import com.banglalink.toffee.extension.checkVerification
import com.banglalink.toffee.extension.doIfNotNullOrEmpty
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.safeClick
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.ui.common.BaseFragment

class PackDetailsFragment : BaseFragment() {
    
    private var pack: PremiumPack? = null
    private var _binding: FragmentPackDetailsBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<PackDetailsFragmentArgs>()
    private val viewModel by activityViewModels<PremiumViewModel>()
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPackDetailsBinding.inflate(layoutInflater)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        changeToolbarIcon()
        pack = args.pack
        
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
                payNowButton.safeClick({
                    activity?.checkVerification {
                        viewModel.getPremiumDataPackList(it.id)
                        findNavController().navigate(R.id.bottomSheetPaymentMethods)
                    }
                })

            }
        }
        
//        findNavController()?.navigate(R.id.startWatchingDialog)

    }
    
    private fun observePremiumPackDetail() {
        observe(viewModel.premiumPackDetailState) { response ->
            when (response) {
                is Success -> {
                    response.data?.linearChannelList?.doIfNotNullOrEmpty {
                        viewModel.setLinearContentState(it.toList())
                    }
                    response.data?.vodChannelList?.doIfNotNullOrEmpty {
                        viewModel.setVodContentState(it.toList())
                    }
                }
                is Failure -> {
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