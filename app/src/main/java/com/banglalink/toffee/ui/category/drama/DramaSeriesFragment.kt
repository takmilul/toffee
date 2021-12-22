package com.banglalink.toffee.ui.category.drama

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.analytics.ToffeeEvents
import com.banglalink.toffee.databinding.FragmentDramaSeriesBinding
import com.banglalink.toffee.enums.PageType
import com.banglalink.toffee.model.Category
import com.banglalink.toffee.ui.category.CategoryDetailsFragment
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.home.LandingPageViewModel

class DramaSeriesFragment: BaseFragment() {
    private lateinit var category: Category
    private var _binding: FragmentDramaSeriesBinding ? = null
    private val binding get() = _binding!!
    private val landingViewModel by activityViewModels<LandingPageViewModel>()

    companion object {
        @JvmStatic
        fun newInstance() = DramaSeriesFragment().apply {
            arguments = Bundle().also {
                it.putParcelable(CategoryDetailsFragment.ARG_CATEGORY_ITEM, category)
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        category = requireArguments().getParcelable(CategoryDetailsFragment.ARG_CATEGORY_ITEM)!!
        activity?.title = category.categoryName
        landingViewModel.pageType.value = PageType.Category
        landingViewModel.categoryId.value = category.id.toInt()
        landingViewModel.checkedSubCategoryChipId.value = 0
        landingViewModel.isDramaSeries.value = true
        ToffeeAnalytics.logEvent(ToffeeEvents.SCREEN_VIEW,  bundleOf("firebase_screen" to category.categoryName))
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDramaSeriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}