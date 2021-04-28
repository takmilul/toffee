package com.banglalink.toffee.ui.category.drama

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
import kotlinx.coroutines.flow.distinctUntilChangedBy

class DramaSeriesContentFragment : HomeBaseFragment(), ProviderIconCallback<ChannelInfo> {

    private var category: Category? = null
    private var selectedFilter: Int = FEED.value
    private var _binding: FragmentDramaSeriesContentBinding ? = null
    private lateinit var mAdapter: DramaSeriesListAdapter<ChannelInfo>
    private val binding get() = _binding!!
    private val viewModel by viewModels<DramaSeriesViewModel>()
    private val landingPageViewModel by activityViewModels<LandingPageViewModel>()
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?, ): View {
        _binding = FragmentDramaSeriesContentBinding.inflate(inflater, container, false)
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

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            mAdapter.loadStateFlow
//                .distinctUntilChangedBy { it.refresh }
                .collectLatest {
                binding.progressBar.isVisible = it.source.refresh is LoadState.Loading
                mAdapter.apply {
                    val isLoading = it.source.refresh is LoadState.Loading || !isInitialized
                    val isEmpty = mAdapter.itemCount <= 0 && ! it.source.refresh.endOfPaginationReached
                    binding.emptyView.isVisible = isEmpty && !isLoading
                    binding.placeholder.isVisible = isLoading
                    binding.latestVideosList.isVisible = !isEmpty
                    isInitialized = true
                }
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
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            landingPageViewModel.loadMostPopularVideos(categoryId, subCategoryId).collectLatest {
                mAdapter.submitData(it)
            }
        }
    }

    private fun observeLatestVideosList(categoryId: Int, subCategoryId: Int = 0, isFilter: Int = 0, hashTag: String = "null") {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
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

    override fun hideShareMenuItem(hide: Boolean): Boolean {
        return true
    }
}