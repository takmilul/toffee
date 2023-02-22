package com.banglalink.toffee.ui.premium

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.banglalink.toffee.common.paging.ProviderIconCallback
import com.banglalink.toffee.databinding.FragmentPremContentBinding
import com.banglalink.toffee.enums.PageType
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.home.LandingPageViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PremiumContentFragment : BaseFragment(), ProviderIconCallback<ChannelInfo> {
    
    private lateinit var mAdapter: PremiumContentAdapter
    private var _binding: FragmentPremContentBinding? = null
    private val binding get() = _binding!!
    private val landingPageViewModel by activityViewModels<LandingPageViewModel>()
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentPremContentBinding.inflate(layoutInflater)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        mAdapter = PremiumContentAdapter(this)
        
        with(binding.premiumContentListview) {
            adapter = mAdapter
        }
        observeList()
    }
    
    private fun observeList() {
        viewLifecycleOwner.lifecycleScope.launch {
            val content = if (landingPageViewModel.pageType.value == PageType.Landing) {
                landingPageViewModel.loadChannels()
            } else {
                landingPageViewModel.loadEditorsChoiceContent()
            }
            content.collectLatest {
                mAdapter.submitData(it)
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        binding.premiumContentListview.adapter = null
        _binding = null
    }
}