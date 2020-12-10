package com.banglalink.toffee.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.banglalink.toffee.R
import com.banglalink.toffee.enums.PageType.Landing
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.HomeBaseFragment

class LandingPageFragment : HomeBaseFragment(){
    private val landingViewModel by activityViewModels<LandingPageViewModel>()
    
    companion object {
        fun newInstance(): LandingPageFragment {
            return LandingPageFragment()
        }
    }

    override fun removeItemNotInterestedItem(channelInfo: ChannelInfo) {
//        popularVideoListAdapter.remove(channelInfo)
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
    }
}