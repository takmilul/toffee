package com.banglalink.toffee.ui.landing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.HeartBeatManager
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.databinding.FragmentFeaturedPartnerBinding
import com.banglalink.toffee.extension.showLoadingAnimation
import com.banglalink.toffee.model.FeaturedPartner
import com.banglalink.toffee.ui.home.HomeViewModel
import com.banglalink.toffee.ui.home.LandingPageViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class FeaturedPartnerFragment : Fragment(), BaseListItemCallback<FeaturedPartner> {
    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var mAdapter: FeaturedPartnerAdapter
    @Inject lateinit var heartBeatManager: HeartBeatManager
    private var _binding: FragmentFeaturedPartnerBinding? = null
    private val binding get() = _binding!!
    private val viewModel by activityViewModels<LandingPageViewModel>()
    
    companion object {
        @JvmStatic
        fun newInstance() = FeaturedPartnerFragment()
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentFeaturedPartnerBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAdapter = FeaturedPartnerAdapter(this)
    
        var isInitialized = false
        with(binding.featuredPartnerList) {
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                mAdapter.loadStateFlow.collectLatest {
                    val isLoading = it.source.refresh is LoadState.Loading || !isInitialized
                    val isEmpty = mAdapter.itemCount <= 0 && ! it.source.refresh.endOfPaginationReached
                    binding.placeholder.isVisible = isEmpty
                    binding.featuredPartnerList.isVisible = ! isEmpty
                    binding.placeholder.showLoadingAnimation(isLoading)
                    isInitialized = true
                }
            }
            adapter = mAdapter
            setHasFixedSize(true)
        }
        observeFeaturedPartner()
    }
    
    private fun observeFeaturedPartner() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted { 
            viewModel.loadFeaturedPartners.collectLatest {
                mAdapter.submitData(it)
            }
        }
    }
    
    override fun onItemClicked(item: FeaturedPartner) {
        super.onItemClicked(item)
        
        item.webViewUrl?.let { url->
//            heartBeatManager.triggerEventViewingContentStart(item.id, "VOD")
//            homeViewModel.sendViewContentEvent(item)
            findNavController().navigate(R.id.htmlPageViewFragment, bundleOf("myTitle" to item.featurePartnerName, "url" to url))
//            requireActivity().launchActivity<Html5PlayerViewActivity> {
//                putExtra(
//                    Html5PlayerViewActivity.CONTENT_URL,
//                    url
//                )
//            }
        } ?: ToffeeAnalytics.logException(NullPointerException("External browser url is null"))
    }
    
    override fun onDestroyView() {
        binding.featuredPartnerList.adapter = null
        super.onDestroyView()
        _binding = null
    }
}