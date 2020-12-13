package com.banglalink.toffee.ui.landing

import android.R.attr
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.banglalink.toffee.R
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.model.UgcCategory
import com.banglalink.toffee.model.UgcSubCategory
import com.banglalink.toffee.ui.category.CategoryDetailsFragment
import com.banglalink.toffee.ui.common.ContentReactionCallback
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.common.ReactionFragment
import com.banglalink.toffee.ui.home.LandingPageViewModel
import com.banglalink.toffee.ui.home.PopularVideoListAdapter
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_category_info.*
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

        if(viewModel.categoryId.value == 1){
            subCategoryChipGroupHolder.visibility = View.VISIBLE
            observeSubCategoryList()
        }
        
        filterButton.setOnClickListener {
            val popupMenu = PopupMenu(requireContext(), it)
            popupMenu.menu.add("Latest Videos")
            popupMenu.menu.add("Trending Videos")
            popupMenu.show()
            
            popupMenu.setOnMenuItemClickListener { item ->
                latestVideosHeader.text = item.title
                when(item.title){
                    "Latest Videos" -> observeList(category?.id?.toInt() ?: 0)
                    "Trending Videos" -> observeTrendingList(category?.id?.toInt() ?: 0)
                }
                true
            }
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
                viewModel.loadLatestVideos
            } else {
                viewModel.loadLatestVideosByCategory(categoryId, subCategoryId)
            }
            latestVideos.collectLatest {
                mAdapter.submitData(it)
            }
        }
    }
    
    private fun observeTrendingList(categoryId: Int, subCategoryId: Int = 0){
        listJob?.cancel()
        listJob = lifecycleScope.launchWhenStarted {
            val trendingVideos = if(categoryId == 0) {
                viewModel.loadMostPopularVideos
            } else {
                viewModel.loadMostPopularVideos
            }
            trendingVideos.collectLatest {
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

    override fun onReactionClicked(view: View, reactionCountView: View, item: ChannelInfo) {
        super.onReactionClicked(view, reactionCountView, item)
        requireActivity().supportFragmentManager.beginTransaction().add(ReactionFragment.newInstance(view, reactionCountView, item, true), ReactionFragment.TAG).commit()
    }

    override fun onReactionLongPressed(view: View, reactionCountView: View, item: ChannelInfo) {
        super.onReactionLongPressed(view, reactionCountView, item)
        requireActivity().supportFragmentManager.beginTransaction().add(ReactionFragment.newInstance(view, reactionCountView, item), ReactionFragment.TAG).commit()
    }
    
    override fun onShareClicked(view: View, item: ChannelInfo) {
        super.onShareClicked(view, item)
        homeViewModel.shareContentLiveData.postValue(item)
    }

    override fun onProviderIconClicked(item: ChannelInfo) {
        super.onProviderIconClicked(item)
        viewModel.navigateToMyChannel(this, item.id.toInt(), item.channel_owner_id, item.isSubscribed)
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
    
    private fun observeSubCategoryList() {
        observe(viewModel.subCategories){
            when(it){
                is Success -> {
                    val subList = it.data.sortedBy { sub -> sub.id }
                    subCategoryChipGroup.removeAllViews()
                    subList.forEachIndexed{ _, subCategory ->
                        val newChip = addChip(subCategory).apply {
                            tag = subCategory
                        }
                        subCategoryChipGroup.addView(newChip)
                        if(subCategory.id == 0L) {
                            subCategoryChipGroup.check(newChip.id)
                        }
                    }
                    subCategoryChipGroup.setOnCheckedChangeListener { group, checkedId ->
                        val selectedChip = group.findViewById<Chip>(checkedId)
                        if(selectedChip != null) {
                            val selectedSub = selectedChip.tag as UgcSubCategory
                            viewModel.loadSubcategoryVideos(selectedSub.categoryId.toInt(), selectedSub.id.toInt())
                        }
                    }
                }
                is Failure -> {}
            }
        }
    }

    private fun addChip(subCategory: UgcSubCategory): Chip {
        val intColor = ContextCompat.getColor(requireContext(), R.color.colorButtonSecondary)

        val chipColor = createStateColor(intColor)
        val chip = layoutInflater.inflate(R.layout.category_chip_layout, categoryChipGroup, false) as Chip
        chip.text = subCategory.name
        chip.id = View.generateViewId()

        chip.chipBackgroundColor = chipColor
        chip.chipStrokeColor = ColorStateList.valueOf(intColor)
        chip.rippleColor = chipColor
        chip.setTextColor(createStateColor(Color.WHITE, intColor))

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
}