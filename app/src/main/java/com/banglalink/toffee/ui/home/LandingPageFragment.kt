package com.banglalink.toffee.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.activityViewModels
import com.banglalink.toffee.R
import com.banglalink.toffee.enums.PageType.Landing
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.google.android.material.appbar.AppBarLayout
import dagger.hilt.android.AndroidEntryPoint

class LandingPageFragment : HomeBaseFragment(){
    private val landingViewModel by activityViewModels<LandingPageViewModel>()
    private lateinit var landingAppbar: AppBarLayout
    private lateinit var landingCoordinator: CoordinatorLayout
    private lateinit var latestVideoScroller: NestedScrollView

    companion object {
        fun newInstance(): LandingPageFragment {
            return LandingPageFragment()
        }
    }

    override fun removeItemNotInterestedItem(channelInfo: ChannelInfo) {
//        popularVideoListAdapter.remove(channelInfo)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if(isEnabled) {
                    if(appbarOffset != 0) {
                        latestVideoScroller.smoothScrollTo(0, 0, 0)
                        landingAppbar.setExpanded(true, true)
                    } else {
                        isEnabled = false
                        requireActivity().onBackPressed()
                    }
                }
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_landing_page2, container, false)
    }

    fun onBackPressed(): Boolean {
        return false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = getString(R.string.app_name_short)
        landingViewModel.pageType.value = Landing
        landingViewModel.categoryId.value = 0
        landingViewModel.subCategoryId.value = 0
        landingViewModel.isDramaSeries.value = false

        landingCoordinator = view.findViewById(R.id.landing_coordinator)
        landingAppbar = view.findViewById(R.id.landing_appbar)
        latestVideoScroller = view.findViewById(R.id.latestVideoScroller)

        landingAppbar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
            appbarOffset = verticalOffset
        })
    }

    private var appbarOffset = 0
}