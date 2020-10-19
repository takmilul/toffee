package com.banglalink.toffee.ui.landing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.UgcCategory
import com.banglalink.toffee.ui.category.CategoryDetailsFragment
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.home.LandingPageViewModel
import com.banglalink.toffee.ui.home.PopularVideoListAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_landing_latest_videos.*
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class LatestVideosFragment: HomeBaseFragment() {
    private lateinit var mAdapter: PopularVideoListAdapter

    private val viewModel by activityViewModels<LandingPageViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_landing_latest_videos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val category: UgcCategory? = parentFragment?.arguments?.getParcelable(CategoryDetailsFragment.ARG_CATEGORY_ITEM) as UgcCategory?

        mAdapter = PopularVideoListAdapter(object : BaseListItemCallback<ChannelInfo> {
            override fun onItemClicked(item: ChannelInfo) {
                homeViewModel.fragmentDetailsMutableLiveData.postValue(item)
            }
        })

        with(latestVideosList) {
            adapter = mAdapter
        }

        viewAllButton.setOnClickListener {
            
        }

        observeList(category)
    }

    private fun observeList(category: UgcCategory?) {
        lifecycleScope.launchWhenStarted {
            val latestVideos = if(category != null) {
                viewModel.loadLatestVideosByCategory(category)
            } else {
                viewModel.loadLatestVideos()
            }
            latestVideos.collectLatest {
                mAdapter.submitData(it)
            }
        }
    }

    override fun removeItemNotInterestedItem(channelInfo: ChannelInfo) {

    }
}