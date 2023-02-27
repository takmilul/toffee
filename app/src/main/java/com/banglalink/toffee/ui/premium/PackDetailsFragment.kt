package com.banglalink.toffee.ui.premium

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.banglalink.toffee.R
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
        
        observePremiumPackDetail()
        viewModel.getPremiumPackDetail(args.packId)
        
//        findNavController()?.navigate(R.id.startWatchingDialog)
        with(binding) {
            payNowButton.safeClick({
                activity?.checkVerification {
                    viewModel.getPremiumDataPackList(args.packId)
                    findNavController().navigate(R.id.bottomSheetPaymentMethods)
                }
            })
        }
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