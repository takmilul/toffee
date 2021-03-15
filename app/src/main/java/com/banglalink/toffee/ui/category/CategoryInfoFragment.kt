package com.banglalink.toffee.ui.category

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentCategoryInfoBinding
import com.banglalink.toffee.extension.hide
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.show
import com.banglalink.toffee.model.Category
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.SubCategory
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.home.LandingPageViewModel
import com.banglalink.toffee.util.bindCategoryImage
import com.google.android.material.chip.Chip

class CategoryInfoFragment: HomeBaseFragment() {
    private val landingViewModel by activityViewModels<LandingPageViewModel>()
    private lateinit var categoryInfo: Category
    private lateinit var binding: FragmentCategoryInfoBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentCategoryInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        categoryInfo = requireParentFragment().requireArguments().getParcelable(CategoryDetailsFragment.ARG_CATEGORY_ITEM)!!
        binding.hashTagChipGroupHolder.hide()
        observeCategoryData()
        observeHashTags()
        observeSubCategories()
    }

    private fun observeSubCategories() {
        observe(landingViewModel.subCategories){
            when(it){
                is Resource.Success -> {
                    val subList = it.data.sortedBy { sub -> sub.id }
                    binding.subCategoryChipGroup.removeAllViews()
                    subList.forEachIndexed{ _, subCategory ->
                        val newChip = addChip(subCategory).apply {
                            tag = subCategory
                        }
                        binding.subCategoryChipGroup.addView(newChip)
                        if(subCategory.id == 0L) {
                            binding.subCategoryChipGroup.check(newChip.id)
                        }
                    }
                    binding.subCategoryChipGroup.setOnCheckedChangeListener { group, checkedId ->
                        val selectedChip = group.findViewById<Chip>(checkedId)
                        if(selectedChip != null) {
                            val selectedSub = selectedChip.tag as SubCategory
                            landingViewModel.subCategoryId.value = selectedSub.id.toInt()
                            landingViewModel.isDramaSeries.value = selectedSub.categoryId.toInt() == 9
                        }
                    }
                }
                is Resource.Failure -> {}
            }
        }
    }

    private fun observeHashTags(){
        observe(landingViewModel.hashtagList) {
            binding.hashTagChipGroupHolder.show()
            binding.hashTagChipGroup.removeAllViews()
            val hashTagList = it
            hashTagList.forEachIndexed{ _, hashTag ->
                if(hashTag.isNotBlank()) {
                    val newChip = addHashTagChip(hashTag).apply {
                        tag = hashTag
                    }
                    binding.hashTagChipGroup.addView(newChip)
                }
            }
            binding.hashTagChipGroup.setOnCheckedChangeListener{ group, checkedId ->
                val selectedHashTag = group.findViewById<Chip>(checkedId)
                if(selectedHashTag != null) {
                    val hashTag = selectedHashTag.tag as String
                    landingViewModel.selectedHashTag.value = hashTag.removePrefix("#")
                }
            }
        }
    }
    
    private fun addHashTagChip(hashTag: String): Chip {
        val intColor = ContextCompat.getColor(requireContext(), R.color.colorSecondaryDark)
        val textColor = ContextCompat.getColor(requireContext(), R.color.main_text_color)
        val foregroundColor = ContextCompat.getColor(requireContext(), R.color.hashtag_chip_color)
        val chipColor = createStateColor(intColor,foregroundColor)
        val chip = layoutInflater.inflate(R.layout.hashtag_chip_layout, binding.hashTagChipGroup, false) as Chip
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
        val chip = layoutInflater.inflate(R.layout.category_chip_layout, binding.subCategoryChipGroup, false) as Chip
        chip.text = subCategory.name
        chip.typeface = Typeface.DEFAULT_BOLD
        chip.id = View.generateViewId()
        chip.chipBackgroundColor = chipColor
        chip.chipStrokeColor = strokeColor
        chip.rippleColor = chipColor
        chip.setTextColor(createStateColor(Color.WHITE, textColor))
        return chip
    }

    private fun observeCategoryData() {
        categoryInfo.let {
            binding.categoryName.text = it.categoryName
            bindCategoryImage(binding.categoryIcon, categoryInfo)
            binding.categoryIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(),R.color.colorAccent2))
        }
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

    override fun removeItemNotInterestedItem(channelInfo: ChannelInfo) {}
}