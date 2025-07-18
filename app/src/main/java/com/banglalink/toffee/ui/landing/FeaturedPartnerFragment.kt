package com.banglalink.toffee.ui.landing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.databinding.FragmentFeaturedPartnerBinding
import com.banglalink.toffee.extension.showLoadingAnimation
import com.banglalink.toffee.model.FeaturedPartner
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.home.LandingPageViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FeaturedPartnerFragment : BaseFragment(), BaseListItemCallback<FeaturedPartner> {
    private lateinit var mAdapter: FeaturedPartnerAdapter
    private var _binding: FragmentFeaturedPartnerBinding? = null
    private val binding get() = _binding!!
    private val viewModel by activityViewModels<LandingPageViewModel>()
    
    companion object {
        @JvmStatic
        fun newInstance() = FeaturedPartnerFragment()
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFeaturedPartnerBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAdapter = FeaturedPartnerAdapter(this)
        binding.featuredPartnerHeader.text = mPref.featuredPartnerTitle
        var isInitialized = false
        with(binding.featuredPartnerList) {
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                mAdapter.loadStateFlow.collectLatest {
                    val isLoading = it.source.refresh is LoadState.Loading || !isInitialized
                    val isEmpty = mAdapter.itemCount <= 0 && !it.source.refresh.endOfPaginationReached
                    binding.placeholder.isVisible = isEmpty && isLoading
                    binding.featuredPartnerHeader.isVisible = !isEmpty
                    binding.featuredPartnerList.isVisible = !isEmpty
                    binding.placeholder.showLoadingAnimation(isLoading)
                    isInitialized = true
                }
            }
            adapter = mAdapter
            setHasFixedSize(true)
        }
        if (mPref.isFeaturePartnerActive) {
            observeFeaturedPartner()
        }
    }
    
    private fun observeFeaturedPartner() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loadFeaturedPartners().collectLatest {
                mAdapter.submitData(it)
            }
        }
    }
    
    override fun onItemClicked(item: FeaturedPartner) {
        super.onItemClicked(item)
        viewModel.featuredPartnerDeeplinkLiveData.value = item
    }
    
    override fun onDestroyView() {
        binding.featuredPartnerList.adapter = null
        super.onDestroyView()
        _binding = null
    }
}