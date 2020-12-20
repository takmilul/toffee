package com.banglalink.toffee.ui.category

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.banglalink.toffee.R
import com.banglalink.toffee.enums.PageType.Category
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.model.UgcCategory
import com.banglalink.toffee.model.UgcSubCategory
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.home.LandingPageViewModel
import com.banglalink.toffee.util.bindCategoryImage
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.fragment_category_info.*

class CategoryInfoFragment: HomeBaseFragment() {
    private val landingViewModel by activityViewModels<LandingPageViewModel>()
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
        landingViewModel.pageType.value = (Category)
        landingViewModel.checkedSubCategoryChipId.value = 0
        landingViewModel.categoryId.value = (categoryInfo.id.toInt())
        landingViewModel.isDramaSeries.value = false

        observeList()
        observeCategoryData()
//        observeChipCheck()
    }

    private fun observeChipCheck() {
        lifecycleScope.launchWhenStarted { 
            observe(landingViewModel.checkedSubCategoryChipId){
                if (it != 0 && landingViewModel.subCategoryId.value == 0 && landingViewModel.categoryId.value != 0)
                    categoryChipGroup.check(categoryChipGroup[0].id)
            }
        }
    }

    private fun observeList() {
        observe(landingViewModel.subCategories){
            when(it){
                is Success -> {
                    val subList = it.data.sortedBy { sub -> sub.id }
                    categoryChipGroup.removeAllViews()
                    subList.forEachIndexed{ _, subCategory ->
                        val newChip = addChip(subCategory).apply {
                            tag = subCategory
                        }
                        categoryChipGroup.addView(newChip)
                        if(subCategory.id == 0L) {
                            categoryChipGroup.check(newChip.id)
                        }
                    }
                    categoryChipGroup.setOnCheckedChangeListener { group, checkedId ->
//                        landingViewModel.checkedSubCategoryChipId.value = checkedId
                        val selectedChip = group.findViewById<Chip>(checkedId)
                        if(selectedChip != null) {
                            val selectedSub = selectedChip.tag as UgcSubCategory
                            landingViewModel.categoryId.value = selectedSub.categoryId.toInt()
                            landingViewModel.subCategoryId.value = selectedSub.id.toInt()
                            landingViewModel.isDramaSeries.value = selectedSub.categoryId.toInt() == 9
//                            landingViewModel.loadSubcategoryVideos(selectedSub.categoryId.toInt(), selectedSub.id.toInt())
                        }
                    }
                }
                is Failure -> {}
            }
        }

        observe(landingViewModel.hashtagList) { hashtagList->
            hashTagChipGroup.removeAllViews()
            hashtagList.forEachIndexed{ _, hashtag ->
                val newChip = addHashtagChip(hashtag).apply {
                    tag = hashtag
                }
                hashTagChipGroup.addView(newChip)
            }
        }
    }

    private fun addHashtagChip(hashtag: String): Chip {
        val intColor = ContextCompat.getColor(requireContext(), R.color.colorButtonSecondary)
        val foregroundColor = ContextCompat.getColor(requireContext(), R.color.colorSecondaryAccent)

        val chipColor = createStateColor(intColor, foregroundColor)
        val chip = layoutInflater.inflate(R.layout.hashtag_chip_layout, hashTagChipGroup, false) as Chip
        chip.text = hashtag
//        chip.typeface = Typeface.DEFAULT_BOLD
        chip.id = View.generateViewId()

        chip.chipBackgroundColor = chipColor
//        chip.chipStrokeColor = ColorStateList.valueOf(intColor)
        chip.rippleColor = chipColor
        chip.setTextColor(createStateColor(Color.WHITE, intColor))

        return chip
    }

    private fun addChip(subCategory: UgcSubCategory): Chip {
        val intColor = ContextCompat.getColor(requireContext(), R.color.colorButtonSecondary)

        val chipColor = createStateColor(intColor)
        val chip = layoutInflater.inflate(R.layout.category_chip_layout, categoryChipGroup, false) as Chip
        chip.text = subCategory.name
        chip.typeface = Typeface.DEFAULT_BOLD
        chip.id = View.generateViewId()

        chip.chipBackgroundColor = chipColor
        chip.chipStrokeColor = ColorStateList.valueOf(intColor)
        chip.rippleColor = chipColor
        chip.setTextColor(createStateColor(Color.WHITE, intColor))

        return chip
    }

    private fun observeCategoryData() {
        categoryInfo.let {
            categoryName.text = it.categoryName
            bindCategoryImage(categoryIcon, categoryInfo)
            categoryIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(),R.color.colorAccent2))
        }
    }

    private fun createHashtagStateColor(selectedColor: Int, unSelectedColor: Int = Color.TRANSPARENT): ColorStateList {
        return ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf()
            ),
            intArrayOf(
                selectedColor,
                unSelectedColor
            )
        )
    }

    private fun createStateColor(selectedColor: Int, unSelectedColor: Int = Color.TRANSPARENT): ColorStateList {
        return ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf()
            ),
            intArrayOf(
                selectedColor,
                unSelectedColor
            )
        )
    }

    override fun removeItemNotInterestedItem(channelInfo: ChannelInfo) {

    }
}