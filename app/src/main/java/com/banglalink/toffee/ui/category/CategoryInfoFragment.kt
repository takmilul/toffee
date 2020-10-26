package com.banglalink.toffee.ui.category

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.*
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.common.SingleListItemCallback
import com.banglalink.toffee.ui.home.*
import com.banglalink.toffee.util.Utils
import com.banglalink.toffee.util.bindCategoryImage
import com.banglalink.toffee.util.bindChannelLogo
import com.banglalink.toffee.util.unsafeLazy
import com.google.android.material.chip.Chip
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_category_info.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.random.nextInt

@AndroidEntryPoint
class CategoryInfoFragment: HomeBaseFragment() {
    private lateinit var mAdapter: FeaturedCategoryListAdapter
    private val viewModel by viewModels<CategoryInfoViewModel>()
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

        mAdapter = FeaturedCategoryListAdapter(object: SingleListItemCallback<ChannelInfo> {
            override fun onItemClicked(item: ChannelInfo) {
                homeViewModel.fragmentDetailsMutableLiveData.postValue(item)
            }
        })

        with(categoryListPager) {
            adapter = mAdapter

            registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    mAdapter.getItem(position)?.let {
                        channelDescription.text = it.program_name
                        channelName.text = it.content_provider_name
                        bindChannelLogo(channelLogo, it)
                    }
                }
            })
        }

        TabLayoutMediator(category_scroll_indicator, categoryListPager, true) { tab_, position -> }.attach()

        observeList()
        observeCategoryData()

        follow_button.setOnClickListener {
            viewModel.updateFollow(categoryInfo.id.toInt())
        }
    }

    private fun observeList() {
        viewModel.requestList(categoryInfo.id)

        observe(viewModel.featuredList) {
            mAdapter.removeAll()
            mAdapter.addAll(it)
            mAdapter.notifyDataSetChanged()
            startPageScroll()
        }

        observe(viewModel.subcategoryList) {
            val subList = it.sortedBy { sub -> sub.id }
            categoryChipGroup.removeAllViews()

            subList.forEach{ subCategory ->
                categoryChipGroup.addView(addChip(subCategory).apply {
                    isChecked = subCategory.id == 0L
                })
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

    private fun startPageScroll() {
        scrollJob?.cancel()

        scrollJob = lifecycleScope.launch {
            while(isActive) {
                delay(5000)
                if(isActive && mAdapter.itemCount > 0) {
                    categoryListPager?.currentItem = (categoryListPager.currentItem + 1) % mAdapter.itemCount
                }
            }
        }
    }

    override fun removeItemNotInterestedItem(channelInfo: ChannelInfo) {

    }
}