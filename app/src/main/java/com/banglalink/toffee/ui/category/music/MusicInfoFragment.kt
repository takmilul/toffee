package com.banglalink.toffee.ui.category.music

import android.content.res.ColorStateList
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
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.home.LandingPageViewModel
import com.banglalink.toffee.util.BindingUtil
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MusicInfoFragment: HomeBaseFragment() {
    
    private var category: Category? = null
    private val binding get() = _binding!!
    @Inject lateinit var bindingUtil: BindingUtil
    private var _binding: FragmentMusicInfoBinding? = null
    private val landingViewModel by activityViewModels<LandingPageViewModel>()
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMusicInfoBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.isStingray = mPref.isStingrayActive
        category = landingViewModel.selectedCategory.value
        requireActivity().title = category?.categoryName ?: ""
        setCategoryUiInfo()
        binding.categoryMusicShare.safeClick({
            category?.categoryShareUrl?.let { requireActivity().handleUrlShare(it) }
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
    
    private fun setCategoryUiInfo() {
        category?.let {
            binding.categoryName.text = it.categoryName
            bindingUtil.bindCategoryIcon(binding.categoryIcon, category)
            binding.categoryIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.colorAccent2))
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}