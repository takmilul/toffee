package com.banglalink.toffee.ui.category.webseries

import android.R.attr
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.filter
import com.banglalink.toffee.R
import com.banglalink.toffee.R.string
import com.banglalink.toffee.common.paging.ProviderIconCallback
import com.banglalink.toffee.databinding.FragmentWebSeriesContentBinding
import com.banglalink.toffee.enums.FilterContentType.*
import com.banglalink.toffee.extension.hide
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.show
import com.banglalink.toffee.extension.showLoadingAnimation
import com.banglalink.toffee.model.*
import com.banglalink.toffee.ui.category.CategoryDetailsFragment
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.home.LandingPageViewModel
import com.banglalink.toffee.ui.player.AddToPlaylistData
import com.google.android.material.chip.Chip
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class WebSeriesContentFragment : HomeBaseFragment(), ProviderIconCallback<ChannelInfo> {

    private val binding get() = _binding!!
    private var category: Category? = null
    private var selectedSubCategoryId: Int = 0
    private var selectedFilter: Int = FEED.value
    private var _binding: FragmentWebSeriesContentBinding? = null
    private lateinit var mAdapter: WebSeriesListAdapter<ChannelInfo>
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
        
        observeHashTags()
        observeSubCategories()
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
            item.activeSeasonList,
            item.video_share_url,
            item.id.toInt(),
            item
        )
        homeViewModel.addToPlayListMutableLiveData.postValue(
            AddToPlaylistData(
                seriesData.playlistId(),
                listOf(item)
            )
        )
        homeViewModel.playContentLiveData.postValue(
            seriesData
        )
    }
    
    private fun observeSubCategories() {
        observe(landingPageViewModel.subCategories) {
            if (it.isNotEmpty()) {
                binding.subCategoryChipGroup.removeAllViews()
                val subList = it.sortedBy { sub -> sub.id }
                subList.let { list ->
                    list.forEachIndexed { _, subCategory ->
                        val newChip = addChip(subCategory).apply {
                            tag = subCategory
                        }
                        binding.subCategoryChipGroup.addView(newChip)
                        if (subCategory.id == 0L) {
                            binding.subCategoryChipGroup.check(newChip.id)
                        }
                    }
                }
                binding.subCategoryChipGroup.setOnCheckedChangeListener { group, checkedId ->
                    val selectedChip = group.findViewById<Chip>(checkedId)
                    if (selectedChip != null) {
                        val selectedSub = selectedChip.tag as SubCategory
                        selectedSubCategoryId = selectedSub.id.toInt()
                        landingPageViewModel.subCategoryId.value = selectedSub.id.toInt()
                        landingPageViewModel.isDramaSeries.value = selectedSub.categoryId.toInt() == 9
                    }
                }
            } else {
                binding.subCategoryChipGroupHolder.hide()
            }
        }
    }
    
    private fun observeHashTags() {
        observe(landingPageViewModel.hashtagList) {
            if (it.isNotEmpty()) {
                binding.hashTagChipGroup.removeAllViews()
                binding.hashTagChipGroup.setOnCheckedChangeListener { group, checkedId ->
                    val selectedHashTag = group.findViewById<Chip>(checkedId)
                    if (selectedHashTag != null) {
                        val hashTag = selectedHashTag.tag as String
                        landingPageViewModel.selectedHashTag.value = hashTag.removePrefix("#")
                    } else {
                        landingPageViewModel.subCategoryId.value = selectedSubCategoryId
                    }
                }
                it.let { list ->
                    list.forEachIndexed { _, hashTag ->
                        if (hashTag.isNotBlank()) {
                            val newChip = addHashTagChip(hashTag).apply {
                                tag = hashTag
                            }
                            binding.hashTagChipGroup.addView(newChip)
                        }
                    }
                }
            } else {
                binding.hashTagChipGroupHolder.hide()
            }
        }
    }
    
    private fun addHashTagChip(hashTag: String): Chip {
        val intColor = ContextCompat.getColor(requireContext(), R.color.colorSecondaryDark)
        val textColor = ContextCompat.getColor(requireContext(), R.color.main_text_color)
        val foregroundColor = ContextCompat.getColor(requireContext(), R.color.hashtag_chip_color)
        val chipColor = createStateColor(intColor, foregroundColor)
        val chip = layoutInflater.inflate(
            R.layout.hashtag_chip_layout,
            binding.hashTagChipGroup,
            false
        ) as Chip
        chip.text = hashTag
        chip.id = View.generateViewId()
        chip.chipBackgroundColor = chipColor
        chip.rippleColor = chipColor
        chip.setTextColor(createStateColor(Color.WHITE, textColor))
        return chip
    }
    
    private fun addChip(subCategory: SubCategory): Chip {
        val intColor = ContextCompat.getColor(requireContext(), R.color.colorSecondaryDark)
        val textColor = ContextCompat.getColor(requireContext(), R.color.main_text_color)
        val chipColor = createStateColor(intColor)
        val strokeColor = createStateColor(intColor, textColor)
        val chip = layoutInflater.inflate(
            R.layout.category_chip_layout,
            binding.subCategoryChipGroup,
            false
        ) as Chip
        chip.text = subCategory.name
        chip.typeface = Typeface.DEFAULT_BOLD
        chip.id = View.generateViewId()
        chip.chipBackgroundColor = chipColor
        chip.chipStrokeColor = strokeColor
        chip.rippleColor = chipColor
        chip.setTextColor(createStateColor(Color.WHITE, textColor))
        return chip
    }
    
    private fun createStateColor(selectedColor: Int, unSelectedColor: Int = Color.TRANSPARENT): ColorStateList {
        return ColorStateList(
            arrayOf(
                intArrayOf(attr.state_checked),
                intArrayOf()
            ),
            intArrayOf(
                selectedColor,
                unSelectedColor
            )
        )
    }
    
    override fun onOpenMenu(view: View, item: ChannelInfo) {
        super.onOptionClicked(view, item)
    }

    override fun onProviderIconClicked(item: ChannelInfo) {
        super.onProviderIconClicked(item)
        homeViewModel.myChannelNavLiveData.value = MyChannelNavParams(item.channel_owner_id)
    }

    override fun showShareMenuItem(hide: Boolean): Boolean {
        return true
    }
}