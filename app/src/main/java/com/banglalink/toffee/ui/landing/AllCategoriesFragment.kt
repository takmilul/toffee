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
import com.banglalink.toffee.extension.hide
import com.banglalink.toffee.extension.show
import com.banglalink.toffee.model.Category
import com.banglalink.toffee.ui.category.CategoryDetailsFragment
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.home.LandingPageViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class AllCategoriesFragment: BaseFragment(), BaseListItemCallback<Category> {
    
    private lateinit var mAdapter: CategoriesListAdapter
    private var _binding: FragmentLandingCategoriesBinding ? =null
    private val binding get() = _binding!!
    private val viewModel by activityViewModels<LandingPageViewModel>()
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLandingCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.categoriesBg.isVisible = false
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
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.loadCategories.collectLatest {
                mAdapter.submitData(it)
            }
        }
    }
    
    override fun onItemClicked(item: Category) {
        val args = Bundle().apply {
            putParcelable(CategoryDetailsFragment.ARG_CATEGORY_ITEM, item)
            putString(CategoryDetailsFragment.ARG_TITLE, item.categoryName)
            ToffeeAnalytics.logEvent(ToffeeEvents.CATEGORY_EVENT+item.categoryName.lowercase().replace(" ", "_"))
        }
        when(item.id.toInt()) {
            1 -> {
                parentFragment?.findNavController()?.navigate(R.id.movieFragment, args)
            }
            9 -> {
                parentFragment?.findNavController()?.navigate(R.id.dramaSeriesFragment, args)
            }
            else -> {
                parentFragment?.findNavController()?.navigate(R.id.categoryDetailsFragment, args)
            }
        }
    }
}