package com.banglalink.toffee.ui.landing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.analytics.ToffeeEvents
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.databinding.FragmentLandingCategoriesBinding
import com.banglalink.toffee.enums.CategoryType
import com.banglalink.toffee.extension.hide
import com.banglalink.toffee.extension.show
import com.banglalink.toffee.model.Category
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.home.LandingPageViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AllCategoriesFragment: BaseFragment(), BaseListItemCallback<Category> {
    
    private val binding get() = _binding!!
    private lateinit var mAdapter: CategoriesListAdapter
    private var _binding: FragmentLandingCategoriesBinding ? =null
    private val viewModel by activityViewModels<LandingPageViewModel>()
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLandingCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.viewAllButton.isVisible = false
        binding.placeholder.hide()
        binding.categoriesList.show()
        
        mAdapter = CategoriesListAdapter(this, true)

        with(binding.categoriesList) {
            layoutManager = GridLayoutManager(context, 2)
            adapter = mAdapter
        }

        observeList()
    }

    private fun observeList() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loadCategories().collectLatest {
                mAdapter.submitData(it)
            }
        }
    }
    
    override fun onItemClicked(item: Category) {
        viewModel.selectedCategory.value = item
        ToffeeAnalytics.logEvent(ToffeeEvents.CATEGORY_EVENT+item.categoryName.lowercase().replace(" ", "_"))
        when(item.id.toInt()) {
            CategoryType.MOVIE.value -> {
                parentFragment?.findNavController()?.navigate(R.id.movieFragment)
            }
            CategoryType.MUSIC.value -> {
                parentFragment?.findNavController()?.navigate(R.id.musicDetailsFragmant)
            }
            CategoryType.DRAMA_SERIES.value -> {
                parentFragment?.findNavController()?.navigate(R.id.dramaSeriesFragment)
            }
            else -> {
                parentFragment?.findNavController()?.navigate(R.id.categoryDetailsFragment)
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}