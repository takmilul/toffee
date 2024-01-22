package com.banglalink.toffee.ui.category.webseries

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.banglalink.toffee.databinding.FragmentWebSeriesBinding
import com.banglalink.toffee.enums.PageType
import com.banglalink.toffee.model.Category
import com.banglalink.toffee.ui.category.CategoryDetailsFragment
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.home.LandingPageViewModel
import java.util.Locale

class WebSeriesFragment: BaseFragment() {
    private var category: Category? = null
    private val binding get() = _binding!!
    private var _binding: FragmentWebSeriesBinding? = null
    private val landingViewModel by activityViewModels<LandingPageViewModel>()
    
    companion object {
        @JvmStatic
        fun newInstance() = WebSeriesFragment().apply {
            arguments = Bundle().also {
                it.putParcelable(CategoryDetailsFragment.ARG_CATEGORY_ITEM, category)
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        category = landingViewModel.selectedCategory.value
        val categoryId = category?.id?.toInt() ?: 0
        val categoryName = category?.categoryName ?: ""
        
        landingViewModel.pageType.value = PageType.Category
        landingViewModel.pageName.value = categoryName.uppercase(Locale.getDefault()) + "CATEGORY_PAGE"
        landingViewModel.featuredPageName.value = "$categoryName Page"
        landingViewModel.categoryId.value = categoryId
        mPref.categoryId.value = categoryId
        mPref.categoryName.value = categoryName
        landingViewModel.checkedSubCategoryChipId.value = 0
        landingViewModel.isDramaSeries.value = true
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWebSeriesBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().title = category?.categoryName ?: ""
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        landingViewModel.selectedCategory.value = null
    }
}