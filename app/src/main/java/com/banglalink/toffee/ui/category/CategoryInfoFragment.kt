package com.banglalink.toffee.ui.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.banglalink.toffee.R
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.Category
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.home.FeaturedListAdapter
import com.banglalink.toffee.ui.home.LandingPageViewModel
import com.banglalink.toffee.util.bindChannelLogo
import com.banglalink.toffee.util.unsafeLazy
import com.google.android.material.chip.Chip
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_category_info.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class CategoryInfoFragment: HomeBaseFragment() {

    private lateinit var mAdapter: FeaturedListAdapter

    val viewModel by unsafeLazy {
        ViewModelProvider(activity!!)[LandingPageViewModel::class.java]
    }

    companion object {
        fun newInstance(): CategoryInfoFragment {
            return CategoryInfoFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_category_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAdapter = FeaturedListAdapter(this) {
            homeViewModel.fragmentDetailsMutableLiveData.postValue(it)
        }

        with(categoryListPager) {
            adapter = mAdapter

            registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    mAdapter.getItem(position)?.let {
                        channelDescription.text = it.program_name
                        bindChannelLogo(channelLogo, it)
                    }
                }
            })
        }

        TabLayoutMediator(category_scroll_indicator, categoryListPager, true) { tab_, position -> }.attach()

        observeList()
        observeCategoryData()

        viewModel.loadCategoryInfo()
    }

    private fun observeList() {
        viewModel.featureContentLiveData.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Success -> {
                    mAdapter.addAll(it.data)

                    startPageScroll()
                }
                is Resource.Failure -> {
                    requireActivity().showToast(it.error.msg)
                }
            }
        })
    }

    private fun observeCategoryData() {
        val categoryInfo = requireParentFragment().requireArguments().getParcelable<Category>(CategoryDetailsFragment.ARG_CATEGORY_ITEM)!!

        categoryInfo.let {
            categoryName.text = it.name
            follower_count.text = "${it.numFollowers} Followers"
            categoryIcon.setImageResource(it.icon)

            categoryChipGroup.removeAllViews()
            it.genres.forEachIndexed { index, chipName->
                val chip = layoutInflater.inflate(R.layout.category_chip_layout, categoryChipGroup, false) as Chip
                chip.text = chipName
                categoryChipGroup.addView(chip)
                if(index == 0) {
                    chip.isChecked = true
                }
            }
        }
    }

    override fun removeItemNotInterestedItem(channelInfo: ChannelInfo) {

    }

    private fun startPageScroll() {
        lifecycleScope.launch {
            while(isActive) {
                delay(5000)
                if(isActive && mAdapter.itemCount > 0) {
                    categoryListPager?.currentItem = (categoryListPager.currentItem + 1) % mAdapter.itemCount
                }
            }
        }
    }
}