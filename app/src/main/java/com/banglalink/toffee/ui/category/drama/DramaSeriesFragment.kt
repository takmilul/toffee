package com.banglalink.toffee.ui.category.drama

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentDramaSeriesBinding
import com.banglalink.toffee.enums.PageType
import com.banglalink.toffee.model.UgcCategory
import com.banglalink.toffee.ui.category.CategoryDetailsFragment
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.home.LandingPageViewModel

class DramaSeriesFragment: BaseFragment() {
    private lateinit var category: UgcCategory
    private lateinit var binding: FragmentDramaSeriesBinding
    private val landingViewModel by activityViewModels<LandingPageViewModel>()

    companion object {
        @JvmStatic
        fun newInstance() = DramaSeriesFragment().apply {
            arguments = Bundle().also {
                it.putParcelable(CategoryDetailsFragment.ARG_CATEGORY_ITEM, category)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_drama_series, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        category = requireArguments().getParcelable(CategoryDetailsFragment.ARG_CATEGORY_ITEM)!!
        activity?.title = category.categoryName
        landingViewModel.pageType.value = PageType.Category
        landingViewModel.categoryId.value = category.id.toInt()
        landingViewModel.subCategoryId.value = 0
        landingViewModel.isDramaSeries.value = true
    }

}