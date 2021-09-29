package com.banglalink.toffee.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentLandingPageBinding
import com.banglalink.toffee.enums.PageType.Landing
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.google.android.material.appbar.AppBarLayout

class LandingPageFragment : HomeBaseFragment() {
    
    private var appbarOffset = 0
    private var _binding: FragmentLandingPageBinding ? = null
    private val binding get() = _binding!!
    private val landingViewModel by viewModels<LandingPageViewModel>()

    companion object {
        fun newInstance(): LandingPageFragment {
            return LandingPageFragment()
        }
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLandingPageBinding.inflate(inflater, container, false)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
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
        })
        return binding.root
    }

    override fun onDestroyView() {
        binding.landingAppbar.removeOnOffsetChangedListener(offsetListener)
        super.onDestroyView()
        _binding = null
    }

    private val offsetListener = AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
        appbarOffset = verticalOffset
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = getString(R.string.app_name_short)
        landingViewModel.categoryId.value = 0
        landingViewModel.pageType.value = Landing
        landingViewModel.isDramaSeries.value = false
        binding.landingAppbar.addOnOffsetChangedListener(offsetListener)
        binding.featuredPartnerFragment.isVisible = mPref.isFeaturePartnerActive == "true"
    }
    
    fun onBackPressed(): Boolean {
        return false
    }
}