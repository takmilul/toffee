package com.banglalink.toffee.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.FirebaseParams
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.analytics.ToffeeEvents
import com.banglalink.toffee.apiservice.BrowsingScreens
import com.banglalink.toffee.databinding.FragmentLandingPageBinding
import com.banglalink.toffee.enums.PageType.Landing
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.google.android.material.appbar.AppBarLayout
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@AndroidEntryPoint
class LandingPageFragment : HomeBaseFragment() {
    
    private var appbarOffset = 0
    private var _binding: FragmentLandingPageBinding ? = null
    @Inject @ApplicationContext lateinit var appContext: Context
    private val binding get() = _binding!!
    private val landingViewModel by activityViewModels<LandingPageViewModel>()
    
    companion object {
        fun newInstance(): LandingPageFragment {
            return LandingPageFragment()
        }
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLandingPageBinding.inflate(inflater, container, false)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                runCatching {
                    if (isEnabled) {
                        if (appbarOffset != 0) {
                            binding.latestVideoScroller.smoothScrollTo(0, 0, 0)
                            binding.landingAppbar.setExpanded(true, true)
                        } else {
                            isEnabled = false
                            requireActivity().onBackPressed()
                        }
                    }
                }
            }
        })
        return binding.root
    }
    
    private val offsetListener = AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
        appbarOffset = verticalOffset
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = getString(R.string.app_name_short)
        landingViewModel.categoryId.value = 0
        landingViewModel.pageType.value = Landing
        landingViewModel.pageName.value = BrowsingScreens.HOME_PAGE
        landingViewModel.featuredPageName.value = "Home Page"
        landingViewModel.isDramaSeries.value = false
        binding.landingAppbar.addOnOffsetChangedListener(offsetListener)
        binding.featuredPartnerFragment.isVisible = mPref.isFeaturePartnerActive
        observe(mPref.isFireworkInitialized) { isInitialized ->
            _binding?.fireworkFragment?.isVisible = mPref.isFireworkActive && isInitialized
        }
        ToffeeAnalytics.logEvent(ToffeeEvents.SCREEN_VIEW,  bundleOf(FirebaseParams.BROWSER_SCREEN to "home"))
    }
    
    override fun onDestroyView() {
        binding.landingAppbar.removeOnOffsetChangedListener(offsetListener)
        super.onDestroyView()
        _binding = null
    }
}