package com.banglalink.toffee.ui.category.drama

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import com.banglalink.toffee.R
import com.banglalink.toffee.R.string
import com.banglalink.toffee.common.paging.ProviderIconCallback
import com.banglalink.toffee.databinding.FragmentDramaSeriesContentBinding
import com.banglalink.toffee.enums.FilterContentType.*
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.model.Category
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.MyChannelNavParams
import com.banglalink.toffee.model.SeriesPlaybackInfo
import com.banglalink.toffee.ui.category.CategoryDetailsFragment
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.home.LandingPageViewModel
import com.banglalink.toffee.ui.player.AddToPlaylistData
import kotlinx.coroutines.flow.collectLatest

class DramaSeriesContentFragment : HomeBaseFragment(), ProviderIconCallback<ChannelInfo> {

    private var category: Category? = null
    private var selectedFilter: Int = FEED.value
    private val viewModel by viewModels<DramaSeriesViewModel>()
    private val landingPageViewModel by activityViewModels<LandingPageViewModel>()
    private lateinit var binding: FragmentDramaSeriesContentBinding
    private lateinit var mAdapter: DramaSeriesListAdapter<ChannelInfo>
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?, ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_drama_series_content, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        category = parentFragment?.arguments?.getParcelable(CategoryDetailsFragment.ARG_CATEGORY_ITEM) as Category?
        setupEmptyView()
        mAdapter = DramaSeriesListAdapter(this)

        binding.latestVideosList.adapter = mAdapter
        landingPageViewModel.isDramaSeries.value = true
        observeLatestVideosList(category?.id?.toInt() ?: 0)

        observe(landingPageViewModel.subCategoryId) {
            if (selectedFilter == LATEST_VIDEOS.value || selectedFilter == FEED.value) {
                observeLatestVideosList(category?.id?.toInt() ?: 9, it)
            }
            else{
                observeTrendingVideosList(category?.id?.toInt() ?: 9, it)
            }
        }
        
        observe(landingPageViewModel.selectedHashTag) {
            lifecycleScope.launchWhenCreated {
                observeLatestVideosList(category?.id?.toInt() ?: 9, landingPageViewModel.subCategoryId.value ?: 0, 1, it)
            }
        }
        
        binding.filterButton.setOnClickListener {
            val popupMenu = android.widget.PopupMenu(requireContext(), it)
            popupMenu.menu.add(Menu.NONE, LATEST_VIDEOS.value, 1, getString(string.latestVideos))
            popupMenu.menu.add(Menu.NONE, TRENDING_VIDEOS.value, 2, getString(string.trendingVideos))
            popupMenu.show()

            popupMenu.setOnMenuItemClickListener { item ->
                selectedFilter = item.itemId
                binding.latestVideosHeader.text = item.title
                when(item.itemId){
                    LATEST_VIDEOS.value -> observeLatestVideosList(category?.id?.toInt() ?: 0)
                    TRENDING_VIDEOS.value -> observeTrendingVideosList(category?.id?.toInt() ?: 0)
                }
                true
            }
        }

        mAdapter.addLoadStateListener {
            binding.progressBar.isVisible = it.source.refresh is LoadState.Loading
            mAdapter.apply {
                val showEmpty = itemCount <= 0 && !it.source.refresh.endOfPaginationReached && it.source.refresh !is LoadState.Loading
                binding.emptyView.isVisible = showEmpty
                binding.latestVideosList.isVisible = !showEmpty
            }
        }
    }

    private fun getEmptyViewInfo(): Pair<Int, String?> {
        return Pair(0, "No item found")
    }

    private fun setupEmptyView() {
        val info = getEmptyViewInfo()
        if(info.first > 0) {
            binding.emptyViewIcon.setImageResource(info.first)
        }
        else {
            binding.emptyViewIcon.visibility = View.GONE
        }

        info.second?.let {
            binding.emptyViewLabel.text = it
        }
    }

    private fun observeTrendingVideosList(categoryId: Int, subCategoryId: Int = 0) {
        lifecycleScope.launchWhenStarted { 
            landingPageViewModel.loadMostPopularVideos(categoryId, subCategoryId).collectLatest {
                mAdapter.submitData(it)
            }
        }
    }

    private fun observeLatestVideosList(categoryId: Int, subCategoryId: Int = 0, isFilter: Int = 0, hashTag: String = "null") {
        lifecycleScope.launchWhenStarted {
            viewModel.loadDramaSeriesContents(categoryId, subCategoryId, isFilter, hashTag).collectLatest {
                mAdapter.submitData(it)
            }
        }
    }

    override fun onItemClicked(item: ChannelInfo) {
        val seriesData = SeriesPlaybackInfo(
            item.seriesSummaryId,
            item.seriesName ?: "",
            item.seasonNo,
            item.totalSeason,
            item.id.toInt(),
            item
        )
        homeViewModel.addToPlayListMutableLiveData.postValue(
            AddToPlaylistData(
                seriesData.playlistId(),
                listOf(item)
            )
        )
        homeViewModel.fragmentDetailsMutableLiveData.postValue(
            seriesData
        )
    }
    
    override fun onOpenMenu(view: View, item: ChannelInfo) {
        super.onOptionClicked(view, item)
        //onOptionClicked(view, item)
    }

    override fun onProviderIconClicked(item: ChannelInfo) {
        super.onProviderIconClicked(item)
        homeViewModel.myChannelNavLiveData.value = MyChannelNavParams(item.channel_owner_id)
    }

    override fun removeItemNotInterestedItem(channelInfo: ChannelInfo) {
        
    }

    override fun hideShareMenuItem(hide: Boolean): Boolean {
        return true
    }
}