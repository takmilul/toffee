package com.banglalink.toffee.ui.landing

import android.R.attr
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState.Loading
import com.banglalink.toffee.R
import com.banglalink.toffee.R.string
import com.banglalink.toffee.apiservice.BrowsingScreens
import com.banglalink.toffee.common.paging.ListLoadStateAdapter
import com.banglalink.toffee.data.database.LocalSync
import com.banglalink.toffee.databinding.FragmentLandingLatestVideosBinding
import com.banglalink.toffee.enums.FilterContentType.FEED
import com.banglalink.toffee.enums.FilterContentType.LATEST_VIDEOS
import com.banglalink.toffee.enums.FilterContentType.TRENDING_VIDEOS
import com.banglalink.toffee.enums.NativeAdAreaType
import com.banglalink.toffee.enums.Reaction.Love
import com.banglalink.toffee.extension.handleShare
import com.banglalink.toffee.extension.hide
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.show
import com.banglalink.toffee.extension.showLoadingAnimation
import com.banglalink.toffee.model.Category
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.MyChannelNavParams
import com.banglalink.toffee.model.SubCategory
import com.banglalink.toffee.ui.common.ContentReactionCallback
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.common.ReactionIconCallback
import com.banglalink.toffee.ui.common.ReactionPopup
import com.banglalink.toffee.ui.common.ReactionPopup.Companion.TAG
import com.banglalink.toffee.ui.home.LandingPageViewModel
import com.banglalink.toffee.ui.widget.MarginItemDecoration
import com.banglalink.toffee.util.BindingUtil
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LatestVideosFragment : HomeBaseFragment(), ContentReactionCallback<ChannelInfo> {
    
    private var listJob: Job? = null
    private var category: Category? = null
    @Inject lateinit var localSync: LocalSync
    private var selectedSubCategoryId: Int = 0
    private var selectedFilter: Int = FEED.value
    @Inject lateinit var bindingUtil: BindingUtil
    private lateinit var mAdapter: LatestVideosAdapter
    private var _binding: FragmentLandingLatestVideosBinding? = null
    private val binding get() = _binding!!
    private val viewModel by activityViewModels<LandingPageViewModel>()
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLandingLatestVideosBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        category = if (viewModel.pageName.value != BrowsingScreens.HOME_PAGE) viewModel.selectedCategory.value else null
        
        setupEmptyView()
        selectedFilter = FEED.value
        binding.latestVideosList.addItemDecoration(MarginItemDecoration(12))
        
        /* if native ad is not active then immediately initiate the adapter and load content list from API */
        if (!mPref.isNativeAdActive) {
            initAdapter()
            observeLatestVideosList(category?.id?.toInt() ?: 0)
        }
        
        /* if native ad is active, observe the live data which posted from the ad API response and initiate, load content list from 
        content API. always initiate the adapter in this case otherwise the native ad will not be added into the adapter and it 
        will not be shown. even if the content is already is displayed, load the content API again to refresh the list with the native ad attached in the content list */
        observe(homeViewModel.nativeAdApiResponseLiveData) {
            initAdapter()
            observeLatestVideosList(category?.id?.toInt() ?: 0)
        }
        
        /* only if the adapter has not been initialized then initialize the adapter. always load content API to sync the content 
        list with the view count and other counts. */
        observe(mPref.isViewCountDbUpdatedLiveData) {
            if (!this::mAdapter.isInitialized) {
                initAdapter()
            }
            observeLatestVideosList(category?.id?.toInt() ?: 0)
        }
        
        if (category?.id?.toInt() == 1) {
            createSubCategoryList()
        }
        
        observeHashTags()
        observeSubCategories()
        observeSubCategoryChange()
        observeHashTagChange()
        
        binding.filterButton.setOnClickListener { filterButtonClickListener(it) }
    }
    
    private fun initAdapter() {
        runCatching {
            val nativeAdSettings = if (selectedFilter == TRENDING_VIDEOS.value) {
                mPref.nativeAdSettings.value?.find {
                    it.area == NativeAdAreaType.TRENDING_VIDEO.value
                }
            } else {
                mPref.nativeAdSettings.value?.find {
                    it.area == NativeAdAreaType.LATEST_VIDEO.value
                }
            }
            val feedAdUnitId = nativeAdSettings?.adUnitId
            val adInterval = nativeAdSettings?.adInterval ?: 0
            val isAdActive = nativeAdSettings?.isActive ?: false
            val isNativeAdActive = mPref.isNativeAdActive && isAdActive && adInterval > 0 && !feedAdUnitId.isNullOrBlank()
            mAdapter = LatestVideosAdapter(isNativeAdActive, adInterval, feedAdUnitId, mPref, bindingUtil, this)
            
            loadStateFlow()
            with(binding.latestVideosList) {
                setHasFixedSize(true)
                layoutManager = LatestVideosLayoutManager(requireContext())
                adapter = mAdapter.withLoadStateFooter(ListLoadStateAdapter { mAdapter.retry() })
            }
        }
    }
    
    private fun loadStateFlow() {
        var isInitialized = false
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mAdapter.loadStateFlow.collectLatest {
                    with(binding) {
                        val isLoading = it.source.refresh is Loading || !isInitialized
                        val isEmpty = mAdapter.itemCount <= 0 && !it.source.refresh.endOfPaginationReached
                        emptyView.isVisible = isEmpty && !isLoading
                        placeholder.isVisible = isLoading
                        latestVideosList.isVisible = !isEmpty && !isLoading
                        placeholder.showLoadingAnimation(isLoading)
                        isInitialized = true
                    }
                }
            }
        }
    }
    
    private fun filterButtonClickListener(it: View?) {
        val popupMenu = PopupMenu(requireContext(), it)
        popupMenu.menu.add(Menu.NONE, LATEST_VIDEOS.value, 1, getString(string.latestVideos))
        popupMenu.menu.add(Menu.NONE, TRENDING_VIDEOS.value, 2, getString(string.trendingVideos))
        popupMenu.show()
        
        popupMenu.setOnMenuItemClickListener { item ->
            selectedFilter = item.itemId
            binding.latestVideosHeader.text = item.title
            when (item.itemId) {
                LATEST_VIDEOS.value -> observeLatestVideosList(category?.id?.toInt() ?: 0)
                TRENDING_VIDEOS.value -> observeTrendingVideosList(category?.id?.toInt() ?: 0)
            }
            true
        }
    }
    
    private fun observeSubCategoryChange() {
        observe(viewModel.subCategoryId) {
            if (viewModel.checkedSubCategoryChipId.value != 0 && it == 0 && category?.id?.toInt() != 0 && binding.subCategoryChipGroup.childCount > 0) {
                binding.subCategoryChipGroup.check(binding.subCategoryChipGroup[0].id)
            }
            
            if (selectedFilter == LATEST_VIDEOS.value || selectedFilter == FEED.value) {
                observeLatestVideosList(category?.id?.toInt() ?: 0, it)
            } else {
                observeTrendingVideosList(category?.id?.toInt() ?: 0, it)
            }
        }
    }
    
    private fun observeHashTagChange() {
        observe(viewModel.selectedHashTag) {
            listJob?.cancel()
            listJob = viewLifecycleOwner.lifecycleScope.launch {
                runCatching {
                    binding.latestVideosList.recycledViewPool.clear()
                    viewModel.loadHashTagContents(it, category?.id?.toInt() ?: 0, viewModel.subCategoryId.value ?: 0).collectLatest {
                        mAdapter.submitData(it)
                    }
                }
            }
        }
    }
    
    private fun observeSubCategories() {
        observe(viewModel.subCategories) {
            if (it.isNotEmpty()) {
                binding.subCategoryChipGroup.removeAllViews()
                binding.subCategoryChipGroupHolder.show()
                val subList = it.sortedBy { sub -> sub.id }
                subList.let { list ->
                    list.forEachIndexed { _, subCategory ->
                        val newChip = addChipNew(subCategory).apply {
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
                        viewModel.subCategoryId.value = selectedSub.id.toInt()
                        viewModel.isDramaSeries.value = selectedSub.categoryId.toInt() == 9
                    }
                }
            } else {
                binding.subCategoryChipGroupHolder.hide()
            }
        }
    }
    
    private fun observeHashTags() {
        observe(viewModel.hashtagList) {
            if (it.isNotEmpty() && viewModel.categoryId.value != 1) {
                binding.hashTagChipGroup.removeAllViews()
                binding.hashTagChipGroupHolder.show()
                binding.hashTagChipGroup.setOnCheckedChangeListener { group, checkedId ->
                    val selectedHashTag = group.findViewById<Chip>(checkedId)
                    if (selectedHashTag != null) {
                        val hashTag = selectedHashTag.tag as String
                        viewModel.selectedHashTag.value = hashTag.removePrefix("#")
                    } else {
                        viewModel.subCategoryId.value = selectedSubCategoryId
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
    
    private fun addChipNew(subCategory: SubCategory): Chip {
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
    
    private fun getEmptyViewInfo(): Pair<Int, String?> {
        return Pair(0, "No item found")
    }
    
    private fun setupEmptyView() {
        val info = getEmptyViewInfo()
        if (info.first > 0) {
            binding.emptyViewIcon.setImageResource(info.first)
        } else {
            binding.emptyViewIcon.visibility = View.GONE
        }
        
        info.second?.let {
            binding.emptyViewLabel.text = it
        }
    }
    
    private fun observeLatestVideosList(categoryId: Int, subCategoryId: Int = 0) {
        listJob?.cancel()
        listJob = viewLifecycleOwner.lifecycleScope.launch {
            runCatching {
                binding.latestVideosList.recycledViewPool.clear()
                if (categoryId == 0) {
                    viewModel.loadLatestVideos().collectLatest {
                        mAdapter.submitData(it)
                    }
                } else {
                    viewModel.loadLatestVideosByCategory(categoryId, subCategoryId).collectLatest {
                        mAdapter.submitData(it)
                    }
                }
            }
        }
    }
    
    private fun observeTrendingVideosList(categoryId: Int, subCategoryId: Int = 0) {
        listJob?.cancel()
        listJob = viewLifecycleOwner.lifecycleScope.launch {
            runCatching {
                binding.latestVideosList.recycledViewPool.clear()
                viewModel.loadMostPopularVideos(categoryId, subCategoryId).collectLatest {
                    mAdapter.submitData(it)
                }
            }
        }
    }
    
    override fun onItemClicked(item: ChannelInfo) {
        homeViewModel.playContentLiveData.postValue(item)
    }
    
    override fun onOpenMenu(view: View, item: ChannelInfo) {
        openMenu(view, item)
    }
    
    override fun onReactionClicked(view: View, reactionCountView: View, item: ChannelInfo) {
        super.onReactionClicked(view, reactionCountView, item)
        val iconLocation = IntArray(2)
        view.getLocationOnScreen(iconLocation)
        val reactionPopupFragment = ReactionPopup.newInstance(item, iconLocation, view.height).apply {
            setCallback(object : ReactionIconCallback {
                override fun onReactionChange(reactionCount: String, reactionText: String, reactionIcon: Int) {
                    (reactionCountView as TextView).text = reactionCount
                    (view as TextView).text = reactionText
                    view.setCompoundDrawablesWithIntrinsicBounds(reactionIcon, 0, 0, 0)
                    if (reactionText == Love.name) {
                        view.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorAccent))
                    } else {
                        view.setTextColor(ContextCompat.getColor(requireContext(), R.color.fixed_second_text_color))
                    }
                }
            })
        }
        childFragmentManager.commit { add(reactionPopupFragment, TAG) }
    }
    
    override fun onShareClicked(view: View, item: ChannelInfo, isPlaylist: Boolean) {
        super.onShareClicked(view, item, isPlaylist)
        requireActivity().handleShare(item)
    }
    
    override fun onProviderIconClicked(item: ChannelInfo) {
        super.onProviderIconClicked(item)
        homeViewModel.myChannelNavLiveData.value = MyChannelNavParams(item.channel_owner_id)
    }
    
    private fun openMenu(view: View, item: ChannelInfo) {
        showShareMenuItem(true)
        super.onOptionClicked(view, item)
    }
    
    override fun showShareMenuItem(hide: Boolean): Boolean {
        return hide
    }
    
    private fun createSubCategoryList() {
        observe(viewModel.subCategories) {
            if (it.isNotEmpty()) {
                binding.subCategoryChipGroup.removeAllViews()
                val subList = it.sortedBy { sub -> sub.id }
                subList.forEachIndexed { _, subCategory ->
                    val newChip = addChip(subCategory).apply {
                        tag = subCategory
                    }
                    binding.subCategoryChipGroup.addView(newChip)
                    if (subCategory.id == 0L) {
                        binding.subCategoryChipGroup.check(newChip.id)
                    }
                }
                binding.subCategoryChipGroup.setOnCheckedChangeListener { group, checkedId ->
                    val selectedChip = group.findViewById<Chip>(checkedId)
                    if (selectedChip != null) {
                        val selectedSub = selectedChip.tag as SubCategory
                        viewModel.subCategoryId.value = selectedSub.id.toInt()
                        viewModel.isDramaSeries.value = selectedSub.categoryId.toInt() == 9
                    }
                }
                binding.subCategoryChipGroupHolder.show()
            }
        }
    }
    
    private fun addChip(subCategory: SubCategory): Chip {
        val intColor = ContextCompat.getColor(requireContext(), R.color.colorSecondaryDark)
        val textColor = ContextCompat.getColor(requireContext(), R.color.main_text_color)
        
        val chipColor = createStateColor(intColor)
        val chip = layoutInflater.inflate(R.layout.category_chip_layout, binding.subCategoryChipGroup, false) as Chip
        chip.text = subCategory.name
        chip.typeface = Typeface.DEFAULT_BOLD
        chip.id = View.generateViewId()
        
        chip.chipBackgroundColor = chipColor
        chip.chipStrokeColor = createStateColor(intColor, textColor)
        chip.rippleColor = chipColor
        chip.setTextColor(createStateColor(Color.WHITE, textColor))
        
        return chip
    }
    
    private fun createStateColor(selectedColor: Int, unSelectedColor: Int = Color.TRANSPARENT): ColorStateList {
        return ColorStateList(
            arrayOf(intArrayOf(attr.state_checked), intArrayOf()),
            intArrayOf(selectedColor, unSelectedColor)
        )
    }
    
    override fun onDestroyView() {
        binding.latestVideosList.adapter = null
        super.onDestroyView()
        _binding = null
    }
}