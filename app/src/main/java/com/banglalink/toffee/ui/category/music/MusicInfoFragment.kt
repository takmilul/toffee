package com.banglalink.toffee.ui.category.music

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
import com.banglalink.toffee.databinding.FragmentMusicInfoBinding
import com.banglalink.toffee.extension.handleUrlShare
import com.banglalink.toffee.extension.hide
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.safeClick
import com.banglalink.toffee.extension.show
import com.banglalink.toffee.model.Category
import com.banglalink.toffee.model.SubCategory
import com.banglalink.toffee.ui.category.CategoryDetailsFragment
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.home.LandingPageViewModel
import com.banglalink.toffee.util.BindingUtil
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MusicInfoFragment: HomeBaseFragment() {
    private var selectedSubCategoryId: Int = 0
    private lateinit var categoryInfo: Category
    @Inject lateinit var bindingUtil: BindingUtil
    private var _binding: FragmentMusicInfoBinding? = null
    private val binding get() = _binding!!
    private val landingViewModel by activityViewModels<LandingPageViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        categoryInfo = requireParentFragment().requireArguments().getParcelable(CategoryDetailsFragment.ARG_CATEGORY_ITEM)!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMusicInfoBinding.inflate(inflater, container, false)
        binding.isStingray = mPref.isStingrayActive
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().title = categoryInfo.categoryName
        setCategoryUiInfo()
//        observeHashTags()
//        observeSubCategories()
        binding.categoryMusicShare.safeClick({
            categoryInfo.categoryShareUrl?.let { requireActivity().handleUrlShare(it) }
        })
        observerCatLinearContent()
    }
    
    private fun observerCatLinearContent(){
        observe(mPref.isCatWiseLinChannelAvailable){
            if(it){
                binding.cardView.show()
                binding.cardView2.hide()
                binding.cardView3.show()
            }else{
                binding.cardView.hide()
                binding.cardView2.show()
                binding.cardView3.hide()
            }
        }
    }

    private fun observeSubCategories() {
        observe(landingViewModel.subCategories){
            if (it.isNotEmpty()) {
                binding.subCategoryChipGroup.removeAllViews()
                val subList = it.sortedBy { sub -> sub.id }
                subList.let { list ->
                    list.forEachIndexed{ _, subCategory ->
                        val newChip = addChip(subCategory).apply {
                            tag = subCategory
                        }
                        binding.subCategoryChipGroup.addView(newChip)
                        if(subCategory.id == 0L) {
                            binding.subCategoryChipGroup.check(newChip.id)
                        }
                    }
                }
                binding.subCategoryChipGroup.setOnCheckedChangeListener { group, checkedId ->
                    val selectedChip = group.findViewById<Chip>(checkedId)
                    if(selectedChip != null) {
                        val selectedSub = selectedChip.tag as SubCategory
                        selectedSubCategoryId = selectedSub.id.toInt()
                        landingViewModel.subCategoryId.value = selectedSub.id.toInt()
                        landingViewModel.isDramaSeries.value = selectedSub.categoryId.toInt() == 9
                    }
                }
            }
            else {
                binding.subCategoryChipGroupHolder.hide()
            }
        }
    }

    private fun observeHashTags(){
        observe(landingViewModel.hashtagList) {
            if (it.isNotEmpty()) {
                binding.hashTagChipGroup.removeAllViews()
                binding.hashTagChipGroup.setOnCheckedChangeListener{ group, checkedId ->
                    val selectedHashTag = group.findViewById<Chip>(checkedId)
                    if(selectedHashTag != null) {
                        val hashTag = selectedHashTag.tag as String
                        landingViewModel.selectedHashTag.value = hashTag.removePrefix("#")
                    } else {
                        landingViewModel.subCategoryId.value = selectedSubCategoryId
                    }
                }
                it.let { list ->
                    list.forEachIndexed{ _, hashTag ->
                        if(hashTag.isNotBlank()) {
                            val newChip = addHashTagChip(hashTag).apply {
                                tag = hashTag
                            }
                            binding.hashTagChipGroup.addView(newChip)
                        }
                    }
                }
            }
            else {
                binding.hashTagChipGroupHolder.hide()
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

    private fun setCategoryUiInfo() {
        categoryInfo.let {
            binding.categoryName.text = it.categoryName
            bindingUtil.bindCategoryIcon(binding.categoryIcon, categoryInfo)
            binding.categoryIcon.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(),
                    R.color.colorAccent2))
//            if(it.categoryName=="Music Videos"){
//                binding.stingrayFragment.show()
//            }
//            else{
//                binding.stingrayFragment.hide()
//            }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}