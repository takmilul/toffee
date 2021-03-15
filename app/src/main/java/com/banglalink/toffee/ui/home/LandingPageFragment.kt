package com.banglalink.toffee.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentLandingPage2Binding
import com.banglalink.toffee.enums.PageType.Landing
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.google.android.material.appbar.AppBarLayout

class LandingPageFragment : HomeBaseFragment() {
    private var appbarOffset = 0
    private val landingViewModel by activityViewModels<LandingPageViewModel>()
    private lateinit var binding: FragmentLandingPage2Binding

    companion object {
        fun newInstance(): LandingPageFragment {
            return LandingPageFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentLandingPage2Binding.inflate(inflater, container, false)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = getString(R.string.app_name_short)
        landingViewModel.categoryId.value = 0
        landingViewModel.pageType.value = Landing
        landingViewModel.isDramaSeries.value = false

        binding.landingAppbar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
            appbarOffset = verticalOffset
        })
    }

    fun onBackPressed(): Boolean {
        return false
    }

    override fun removeItemNotInterestedItem(channelInfo: ChannelInfo) {
//        popularVideoListAdapter.remove(channelInfo)
    }

}