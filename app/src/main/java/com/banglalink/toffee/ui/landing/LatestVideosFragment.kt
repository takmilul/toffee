package com.banglalink.toffee.ui.landing

import android.R.attr
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.map
import com.banglalink.toffee.R
import com.banglalink.toffee.R.string
import com.banglalink.toffee.common.paging.ListLoadStateAdapter
import com.banglalink.toffee.data.database.LocalSync
import com.banglalink.toffee.databinding.FragmentLandingLatestVideosBinding
import com.banglalink.toffee.enums.FilterContentType.*
import com.banglalink.toffee.enums.Reaction.Love
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.show
import com.banglalink.toffee.model.Category
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.MyChannelNavParams
import com.banglalink.toffee.model.SubCategory
import com.banglalink.toffee.ui.category.CategoryDetailsFragment
import com.banglalink.toffee.ui.common.ContentReactionCallback
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.common.ReactionIconCallback
import com.banglalink.toffee.ui.common.ReactionPopup
import com.banglalink.toffee.ui.common.ReactionPopup.Companion.TAG
import com.banglalink.toffee.ui.home.LandingPageViewModel
import com.banglalink.toffee.ui.widget.MarginItemDecoration
import com.facebook.shimmer.ShimmerFrameLayout
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
    private var selectedFilter: Int = FEED.value
    private lateinit var mAdapter: LatestVideosAdapter
    private var _binding: FragmentLandingLatestVideosBinding ? = null
    private val binding get() = _binding!!
    private val viewModel by activityViewModels<LandingPageViewModel>()
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLandingLatestVideosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        binding.latestVideosList.adapter = null
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var isInitialized = false
        category = parentFragment?.arguments?.getParcelable(CategoryDetailsFragment.ARG_CATEGORY_ITEM) as Category?
    
        setupEmptyView()
        mAdapter = LatestVideosAdapter(this)
        
        with(binding.latestVideosList) {
            addItemDecoration(MarginItemDecoration(12))

            mAdapter.addLoadStateListener {
                val isLoading = it.source.refresh is LoadState.Loading || !isInitialized
                val isEmpty = mAdapter.itemCount <= 0 && ! it.source.refresh.endOfPaginationReached
                binding.emptyView.isVisible = isEmpty && !isLoading
                binding.placeholder.isVisible = isLoading
                binding.latestVideosList.isVisible = !isEmpty
                isInitialized = true
            }
            adapter = mAdapter.withLoadStateFooter(ListLoadStateAdapter { mAdapter.retry() })
            setHasFixedSize(true)
        }
        
        selectedFilter = FEED.value
        observeLatestVideosList(category?.id?.toInt() ?: 0)

        if (category?.id?.toInt() == 1) {
            createSubCategoryList()
        }
        
        observeSubCategoryChange()
        observeHashTagChange()
        
        binding.filterButton.setOnClickListener { filterButtonClickListener(it) }
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
            }
            else {
                observeTrendingVideosList(category?.id?.toInt() ?: 0, it)
            }
        }
    }

    private fun observeHashTagChange() {
        observe(viewModel.selectedHashTag) {
            listJob?.cancel()
            listJob = lifecycleScope.launchWhenCreated {
                viewModel.loadHashTagContents(it, category?.id?.toInt() ?: 0, viewModel.subCategoryId.value ?: 0).collectLatest {
                    mAdapter.submitData(it)
                }
            }
        }
    }

    private fun getEmptyViewInfo(): Pair<Int, String?> {
        return Pair(0, "No item found")
    }

    private fun setupEmptyView() {
        val info = getEmptyViewInfo()
        if (info.first > 0) {
            binding.emptyViewIcon.setImageResource(info.first)
        }
        else {
            binding.emptyViewIcon.visibility = View.GONE
        }

        info.second?.let {
            binding.emptyViewLabel.text = it
        }
    }

    private fun observeLatestVideosList(categoryId: Int, subCategoryId: Int = 0) {
        listJob?.cancel()
        listJob = lifecycleScope.launchWhenStarted {
            if (categoryId == 0) {
                viewModel.loadLatestVideos().collectLatest {
                    mAdapter.submitData(it.map { channel->
                        localSync.syncData(channel)
                        channel
                    })
                }
            }
            else {
                viewModel.loadLatestVideosByCategory(categoryId, subCategoryId).collectLatest {
                    mAdapter.submitData(it.map { channel->
                        localSync.syncData(channel)
                        channel
                    })
                }
            }
        }
    }
    
    private fun observeTrendingVideosList(categoryId: Int, subCategoryId: Int = 0) {
        listJob?.cancel()
        listJob = lifecycleScope.launchWhenStarted {
            viewModel.loadMostPopularVideos(categoryId, subCategoryId).collectLatest {
                mAdapter.submitData(it.map { channel->
                    localSync.syncData(channel)
                    channel
                })
            }
        }
    }
    
    override fun onItemClicked(item: ChannelInfo) {
        homeViewModel.fragmentDetailsMutableLiveData.postValue(item)
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
                    }
                    else {
                        view.setTextColor(ContextCompat.getColor(requireContext(), R.color.fixed_second_text_color))
                    }
                    Log.e(TAG, "setReaction: icon")
                }
            })
        }
        childFragmentManager.commit { add(reactionPopupFragment, TAG) }
    }

    override fun onShareClicked(view: View, item: ChannelInfo) {
        super.onShareClicked(view, item)
        homeViewModel.shareContentLiveData.postValue(item)
    }

    override fun onProviderIconClicked(item: ChannelInfo) {
        super.onProviderIconClicked(item)
        homeViewModel.myChannelNavLiveData.value = MyChannelNavParams(item.channel_owner_id)
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
    
    private fun createSubCategoryList() {
        lifecycleScope.launch {
            observe(viewModel.subCategories) {
                if (it.isNotEmpty()) {
                    binding.subCategoryChipGroup.removeAllViews()
                    binding.subCategoryChipGroup.setOnCheckedChangeListener { group, checkedId ->
                        val selectedChip = group.findViewById<Chip>(checkedId)
                        if (selectedChip != null) {
                            val selectedSub = selectedChip.tag as SubCategory
                            viewModel.subCategoryId.value = selectedSub.id.toInt()
                            viewModel.isDramaSeries.value = selectedSub.categoryId.toInt() == 9
                        }
                    }
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
                    binding.subCategoryChipGroupHolder.show()
                }
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
    
    private fun startLoadingAnimation(isStart: Boolean) {
        binding.placeholder.children.forEach {
            if (it is ShimmerFrameLayout) {
                if (isStart) {
                    it.startShimmer()
                }
                else {
                    it.stopShimmer()
                }
            }
        }
    }
}