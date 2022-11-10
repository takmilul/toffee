package com.banglalink.toffee.ui.landing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.databinding.FragmentFeaturedPartnerBinding
import com.banglalink.toffee.extension.checkVerification
import com.banglalink.toffee.extension.observe
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
            var partnerId: Int
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                mAdapter.loadStateFlow.collectLatest {
                    val isLoading = it.source.refresh is LoadState.Loading || !isInitialized
                    val isEmpty = mAdapter.itemCount <= 0 && !it.source.refresh.endOfPaginationReached
                    binding.placeholder.isVisible = isEmpty
                    binding.featuredPartnerList.isVisible = !isEmpty
                    binding.placeholder.showLoadingAnimation(isLoading)
                    isInitialized = true
                    partnerId = mPref.featuredPartnerIdLiveData.value ?: 0
                    if (!isEmpty && partnerId > 0) {
                        loadFeaturedPartnerFromDeepLink(mPref.featuredPartnerIdLiveData.value)
                    }
                }
            }
            adapter = mAdapter
            setHasFixedSize(true)
        }
        if (mPref.isFeaturePartnerActive == "true") {
            observeFeaturedPartner()
        }
        
        observe(mPref.featuredPartnerIdLiveData) {
            loadFeaturedPartnerFromDeepLink(it)
        }
    }
    
    private fun loadFeaturedPartnerFromDeepLink(partnerId: Int?) {
        try {
            if (mPref.isFeaturePartnerActive == "true" && mAdapter.itemCount > 0 && partnerId != null && partnerId > 0) {
                for (i in 0 until mAdapter.itemCount) {
                    mAdapter.getItemByIndex(i)?.let {
                        if (it.id == partnerId) {
                            mPref.featuredPartnerIdLiveData.value = 0
                            onItemClicked(it)
                            return
                        }
                    }
                }
                mPref.featuredPartnerIdLiveData.value = 0
            }
        } catch (e: Exception) {
            ToffeeAnalytics.logBreadCrumb(e.message ?: "failed to open featured partner from deep link")
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
        item.let {
            if (it.isLoginRequired) {
                requireActivity().checkVerification {
                    openFeaturePartner(it)
                }
            } else {
                openFeaturePartner(it)
            }
        }
    }
    
    private fun openFeaturePartner(featuredPartner: FeaturedPartner) {
        featuredPartner.webViewUrl?.let { url ->
            viewModel.sendFeaturePartnerReportData(
                partnerName = featuredPartner.featurePartnerName.toString(),
                partnerId = featuredPartner.id
            )
            findNavController().navigate(
                R.id.htmlPageViewDialog_Home,
                bundleOf(
                    "myTitle" to "Back to TOFFEE",
                    "url" to url,
                    "isHideBackIcon" to false,
                    "isHideCloseIcon" to true
                )
            )
        } ?: ToffeeAnalytics.logException(NullPointerException("External browser url is null"))
    }
    
    override fun onDestroyView() {
        binding.featuredPartnerList.adapter = null
        super.onDestroyView()
        _binding = null
    }
}