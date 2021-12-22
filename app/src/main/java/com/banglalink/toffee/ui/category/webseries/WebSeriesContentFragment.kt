package com.banglalink.toffee.ui.category.webseries

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.filter
import com.banglalink.toffee.R.string
import com.banglalink.toffee.common.paging.ProviderIconCallback
import com.banglalink.toffee.databinding.FragmentWebSeriesContentBinding
import com.banglalink.toffee.enums.FilterContentType.*
import com.banglalink.toffee.extension.hide
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.show
import com.banglalink.toffee.extension.showLoadingAnimation
import com.banglalink.toffee.model.Category
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.MyChannelNavParams
import com.banglalink.toffee.model.SeriesPlaybackInfo
import com.banglalink.toffee.ui.category.CategoryDetailsFragment
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.home.LandingPageViewModel
import com.banglalink.toffee.ui.player.AddToPlaylistData
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class WebSeriesContentFragment : HomeBaseFragment(), ProviderIconCallback<ChannelInfo> {

    private var category: Category? = null
    private var selectedFilter: Int = FEED.value
    private var _binding: FragmentWebSeriesContentBinding? = null
    private lateinit var mAdapter: WebSeriesListAdapter<ChannelInfo>
    private val binding get() = _binding!!
    private val viewModel by viewModels<WebSeriesViewModel>()
    private val landingPageViewModel by activityViewModels<LandingPageViewModel>()
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?, ): View {
        _binding = FragmentWebSeriesContentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var isInitialized = false
        category = parentFragment?.arguments?.getParcelable(CategoryDetailsFragment.ARG_CATEGORY_ITEM) as Category?
        setupEmptyView()
        mAdapter = WebSeriesListAdapter(this)
        binding.latestVideosList.adapter = mAdapter
        landingPageViewModel.isDramaSeries.value = true

        observe(landingPageViewModel.subCategoryId) {
            binding.placeholder.show()
            binding.latestVideosList.hide()
            binding.placeholder.showLoadingAnimation(true)
            if (selectedFilter == LATEST_VIDEOS.value || selectedFilter == FEED.value) {
                observeLatestVideosList(category?.id?.toInt() ?: 9, it)
            }
            else{
                observeTrendingVideosList(category?.id?.toInt() ?: 9, it)
            }
        }
        observe(landingPageViewModel.selectedHashTag) {
            observeLatestVideosList(category?.id?.toInt() ?: 9, landingPageViewModel.subCategoryId.value ?: 0, 1, it)
        }
        observeLatestVideosList(category?.id?.toInt() ?: 0)
        
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

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            mAdapter.loadStateFlow.collectLatest {
                val isLoading = it.source.refresh is LoadState.Loading || !isInitialized
                val isEmpty = mAdapter.itemCount <= 0 && ! it.source.refresh.endOfPaginationReached
                binding.emptyView.isVisible = isEmpty && !isLoading
                binding.placeholder.isVisible = isLoading
                binding.latestVideosList.isVisible = !isEmpty && !isLoading
                binding.placeholder.showLoadingAnimation(isLoading)
                isInitialized = true
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
        viewLifecycleOwner.lifecycleScope.launch {
            landingPageViewModel.loadMostPopularVideos(categoryId, subCategoryId).collectLatest {
                mAdapter.submitData(it.filter { !it.isExpired })
            }
        }
    }

    private fun observeLatestVideosList(categoryId: Int, subCategoryId: Int = 0, isFilter: Int = 0, hashTag: String = "null") {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loadDramaSeriesContents(categoryId, subCategoryId, isFilter, hashTag).collectLatest {
                mAdapter.submitData(it.filter { !it.isExpired })
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

    override fun hideShareMenuItem(hide: Boolean): Boolean {
        return true
    }
}