package com.banglalink.toffee.ui.premium

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentPackDetailsBinding
import com.banglalink.toffee.extension.*
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.ui.common.BaseFragment

class PackDetailsFragment : BaseFragment() {
    private var _binding: FragmentPackDetailsBinding? = null
    private val binding get() = _binding!!
    private lateinit var navController: NavController
    private lateinit var navOptions: NavOptions
    private val args by navArgs<PackDetailsFragmentArgs>()
    private val viewModel by activityViewModels<PremiumViewModel>()
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPackDetailsBinding.inflate(layoutInflater)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        changeToolBar()
        
        observePremiumPackDetail()
        viewModel.getPremiumPackDetail(args.packId)
        
//        findNavController()?.navigate(R.id.startWatchingDialog)
        with(binding) {
            payNowButton.safeClick({
                activity?.checkVerification {
                    findNavController().navigate(R.id.bottomSheetPaymentMethods)
                }
            })
        }
    }
    
    private fun observePremiumPackDetail() {
        observe(viewModel.premiumPackDetailLiveData) {
            when(it) {
                is Success -> {
                    it.data?.linearChannelList?.doIfNotNullOrEmpty { linearChannelList ->
                        viewModel.premiumPackLinearContentListLiveData.value = linearChannelList.toList()
                    }
                    it.data?.vodChannelList?.doIfNotNullOrEmpty { vodChannelList ->
                        viewModel.premiumPackVodContentListLiveData.value = vodChannelList.toList()
                    }
                }
                is Failure -> {
                    requireActivity().showToast(it.error.msg)
                }
            }
        }
    }
    
    private fun changeToolBar() {
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