package com.banglalink.toffee.ui.landing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.banglalink.toffee.R
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.UgcCategory
import com.banglalink.toffee.ui.category.CategoryDetailsFragment
import com.banglalink.toffee.ui.common.AlertDialogReactionFragment
import com.banglalink.toffee.ui.common.ContentReactionCallback
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.home.LandingPageViewModel
import com.banglalink.toffee.ui.home.PopularVideoListAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_landing_latest_videos.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class LatestVideosFragment: HomeBaseFragment(), ContentReactionCallback<ChannelInfo> {
    private lateinit var mAdapter: PopularVideoListAdapter

    private val viewModel by activityViewModels<LandingPageViewModel>()
    private var category: UgcCategory? = null
    private var listJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_landing_latest_videos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        category = parentFragment?.arguments?.getParcelable(CategoryDetailsFragment.ARG_CATEGORY_ITEM) as UgcCategory?

        mAdapter = PopularVideoListAdapter(this)

        with(latestVideosList) {
            adapter = mAdapter
        }

        viewAllButton.setOnClickListener {
            
        }

        observe(viewModel.latestVideoLiveData) {
            observeList(it.first, it.second)
        }

        observeList(category?.id?.toInt() ?: 0)
    }

    private fun observeList(categoryId: Int, subCategoryId: Int = 0) {
        listJob?.cancel()
        listJob = lifecycleScope.launchWhenStarted {
            val latestVideos = if(categoryId == 0) {
                viewModel.loadLatestVideos()
            } else {
                viewModel.loadLatestVideosByCategory(categoryId, subCategoryId)
            }
            latestVideos.collectLatest {
                mAdapter.submitData(it)
            }
        }
    }
    override fun onItemClicked(item: ChannelInfo) {
        homeViewModel.fragmentDetailsMutableLiveData.postValue(item)
    }

    override fun onOpenMenu(view: View, item: ChannelInfo) {
        openMenu(view, item)
    }

    override fun onReactionClicked(view: View, item: ChannelInfo) {
        super.onReactionClicked(view, item)
        AlertDialogReactionFragment.newInstance(view, item).show(requireActivity().supportFragmentManager, "ReactionDialog")
    }

    override fun onShareClicked(view: View, item: ChannelInfo) {
        super.onShareClicked(view, item)
        homeViewModel.shareContentLiveData.postValue(item)
    }

    override fun onProviderIconClicked(item: ChannelInfo) {
        super.onProviderIconClicked(item)
        viewModel.navigateToMyChannel(this, item.channel_owner_id, item.isSubscribed)
    }
    
    override fun removeItemNotInterestedItem(channelInfo: ChannelInfo) {

    }

    private fun openMenu(view: View, item: ChannelInfo) {
        hideShareMenuItem(true)
        super.onOptionClicked(view, item)
    }

    override fun hideShareMenuItem(hide: Boolean): Boolean {
        return hide
    }
}