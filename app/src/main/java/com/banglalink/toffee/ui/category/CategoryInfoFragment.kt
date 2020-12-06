package com.banglalink.toffee.ui.category

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.ProviderIconCallback
import com.banglalink.toffee.enums.PageType.Category
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.UgcCategory
import com.banglalink.toffee.model.UgcSubCategory
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.home.FeaturedCategoryListAdapter
import com.banglalink.toffee.ui.home.LandingPageViewModel
import com.banglalink.toffee.util.Utils
import com.banglalink.toffee.util.bindCategoryImage
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_category_info.*
import kotlinx.coroutines.Job

@AndroidEntryPoint
class CategoryInfoFragment: HomeBaseFragment(), ProviderIconCallback<ChannelInfo> {
    private lateinit var mAdapter: FeaturedCategoryListAdapter
    private val viewModel by viewModels<CategoryInfoViewModel>()
    private val landingViewModel by activityViewModels<LandingPageViewModel>()
    private var scrollJob: Job? = null
    private lateinit var categoryInfo: UgcCategory

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_category_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        categoryInfo = requireParentFragment().requireArguments().getParcelable(CategoryDetailsFragment.ARG_CATEGORY_ITEM)!!

        mAdapter = FeaturedCategoryListAdapter(this)

        landingViewModel.pageType.value = Category
        landingViewModel.categoryId.value = categoryInfo.id.toInt()
        /*with(categoryListPager) {
            adapter = mAdapter

            registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    mAdapter.getItem(position)?.let {
                        channelDescription.text = it.program_name
                        channelName.text = it.content_provider_name
                        bindImageFromUrl(channelLogo, it.channelProfileUrl)
                    }
                }
            })
        }

        TabLayoutMediator(category_scroll_indicator, categoryListPager, true) { tab_, position -> }.attach()*/

        observeList()
        observeCategoryData()

        follow_button.setOnClickListener {
            viewModel.updateFollow(categoryInfo.id.toInt())
        }
        channelLogo.setOnClickListener{
//            landingViewModel.redirectToChannel(this, )
        }
    }

    private fun observeList() {
        viewModel.requestList(categoryInfo.id)

        observe(viewModel.featuredList) {
            mAdapter.removeAll()
            mAdapter.addAll(it)
            mAdapter.notifyDataSetChanged()
//            startPageScroll()
        }

        observe(viewModel.subcategoryList) {
            val subList = it.sortedBy { sub -> sub.id }
            categoryChipGroup.removeAllViews()

//            categoryChipGroup.isSelectionRequired = true
//            categoryChipGroup.isSingleSelection = true
            subList.forEachIndexed{ idx, subCategory ->
                val newChip = addChip(subCategory).apply {
                    tag = subCategory
                }
                categoryChipGroup.addView(newChip)
                if(subCategory.id == 0L) {
                    categoryChipGroup.check(newChip.id)
                }
            }

            categoryChipGroup.setOnCheckedChangeListener { group, checkedId ->
                val selectedChip = group.findViewById<Chip>(checkedId)
                if(selectedChip != null) {
                    val selectedSub = selectedChip.tag as UgcSubCategory
                    landingViewModel.loadSubcategoryVideos(selectedSub.categoryId.toInt(), selectedSub.id.toInt())
                }
            }
        }

        observe(viewModel.followerCount) {
            follower_count.text = "${Utils.getFormattedViewsText(it.toString())} Followers"
        }

        observe(viewModel.isCategoryFollowing) {
            follow_button.text = if(it == 0) "Follow" else "Followed"
        }
    }

    private fun addChip(subCategory: UgcSubCategory): Chip {
        val intColor = Color.parseColor(categoryInfo.colorCode)

        val chip = layoutInflater.inflate(R.layout.category_chip_layout, categoryChipGroup, false) as Chip
        chip.text = subCategory.name
        chip.id = View.generateViewId()

        val chipColor = createChipBg(intColor)

        chip.chipBackgroundColor = chipColor
        chip.chipStrokeColor = ColorStateList.valueOf(intColor)
        chip.rippleColor = chipColor

        return chip
    }

    private fun observeCategoryData() {
        categoryInfo.let {
            categoryName.text = it.categoryName
//            follower_count.text = "${Random.nextInt(500..10000)} Followers"
            val intColor = Color.parseColor(it.colorCode)

            categoryIcon.background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                color = ColorStateList.valueOf(intColor)
            }
            bindCategoryImage(categoryIcon, categoryInfo)
        }
    }

    private fun createChipBg(color: Int): ColorStateList {
        return ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf()
            ),
            intArrayOf(
                color,
                Color.BLACK
            )
        )
    }

    /*private fun startPageScroll() {
        scrollJob?.cancel()

        scrollJob = lifecycleScope.launch {
            while(isActive) {
                delay(5000)
                if(isActive && mAdapter.itemCount > 0) {
                    categoryListPager?.currentItem = (categoryListPager.currentItem + 1) % mAdapter.itemCount
                }
            }
        }
    }*/
    
    override fun onItemClicked(item: ChannelInfo) {
        homeViewModel.fragmentDetailsMutableLiveData.postValue(item)
    }

    override fun onProviderIconClicked(item: ChannelInfo) {
        super.onProviderIconClicked(item)
        landingViewModel.navigateToMyChannel(this, item.id.toInt(), item.channel_owner_id, item.isSubscribed)
    }
    
    override fun removeItemNotInterestedItem(channelInfo: ChannelInfo) {

    }
}