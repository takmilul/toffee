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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.filter
import androidx.paging.map
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.data.database.LocalSync
import com.banglalink.toffee.databinding.FragmentCategoryInfoBinding
import com.banglalink.toffee.extension.*
import com.banglalink.toffee.model.Category
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.SubCategory
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.home.LandingPageViewModel
import com.banglalink.toffee.ui.landing.ChannelAdapter
import com.banglalink.toffee.util.BindingUtil
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CategoryInfoFragment : HomeBaseFragment() {
    
    private val binding get() = _binding!!
    @Inject lateinit var localSync: LocalSync
    private var selectedSubCategoryId: Int = 0
    private lateinit var categoryInfo: Category
    @Inject lateinit var bindingUtil: BindingUtil
    private lateinit var mAdapter: CategoryWiseLinearChannelAdapter
    private var _binding: FragmentCategoryInfoBinding? = null
    private val landingViewModel by activityViewModels<LandingPageViewModel>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        categoryInfo = requireParentFragment().requireArguments().getParcelable(CategoryDetailsFragment.ARG_CATEGORY_ITEM)!!
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCategoryInfoBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setCategoryUiInfo()
        observeHashTags()
        observeSubCategories()
        binding.categoryShareButton.safeClick({
            categoryInfo.categoryShareUrl?.let { requireActivity().handleUrlShare(it) }
        })
        
        mAdapter = CategoryWiseLinearChannelAdapter(requireContext(), bindingUtil, object : BaseListItemCallback<ChannelInfo> {
            override fun onItemClicked(item: ChannelInfo) {
                homeViewModel.playContentLiveData.postValue(item.apply {
                    if (isLive && categoryId == 16) {
                        isFromSportsCategory = true
                    }
                })
            }
        })
        
        with(binding.channelList) {
            adapter = mAdapter
        }
        
        observeLinearList()
        binding.viewAllButton.setOnClickListener {
            findNavController().navigate(R.id.menu_tv)
        }
    }
    
    private fun observeSubCategories() {
        observe(landingViewModel.subCategories) {
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
                        landingViewModel.subCategoryId.value = selectedSub.id.toInt()
                        landingViewModel.isDramaSeries.value = selectedSub.categoryId.toInt() == 9
                    }
                }
            } else {
                binding.subCategoryChipGroupHolder.hide()
            }
        }
    }
    
    private fun observeHashTags() {
        observe(landingViewModel.hashtagList) {
            if (it.isNotEmpty()) {
                binding.hashTagChipGroup.removeAllViews()
                binding.hashTagChipGroup.setOnCheckedChangeListener { group, checkedId ->
                    val selectedHashTag = group.findViewById<Chip>(checkedId)
                    if (selectedHashTag != null) {
                        val hashTag = selectedHashTag.tag as String
                        landingViewModel.selectedHashTag.value = hashTag.removePrefix("#")
                    } else {
                        landingViewModel.subCategoryId.value = selectedSubCategoryId
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
    
    private fun setCategoryUiInfo() {
        categoryInfo.let {
            binding.categoryName.text = it.categoryName
            bindingUtil.bindCategoryIcon(binding.categoryIcon, categoryInfo)
            binding.categoryIcon.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorAccent2
                )
            )
            binding.channelTv.text=it.categoryName+" Channels"
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
    
    private fun observeLinearList() {
        viewLifecycleOwner.lifecycleScope.launch {
            landingViewModel.loadCategoryWiseContent(mPref.categoryId.value ?: 0).collectLatest {
                binding.linearGroup.hide()
                binding.nonLinearGroup.show()
                
                mAdapter.submitData(
                    it.filter { !it.isExpired }.map { channel ->
                        localSync.syncData(channel)
                        
                        binding.placeholder.hide()
                        binding.channelList.show()
                        binding.linearGroup.show()
                        binding.nonLinearGroup.hide()
                        channel
                    }
                )
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}