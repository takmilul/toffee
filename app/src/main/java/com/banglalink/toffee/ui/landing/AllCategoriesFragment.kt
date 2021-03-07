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
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.model.Category
import com.banglalink.toffee.ui.category.CategoryDetailsFragment
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.home.LandingPageViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_landing_categories.*
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class AllCategoriesFragment: BaseFragment() {
    private lateinit var mAdapter: CategoriesListAdapter

    private val viewModel by activityViewModels<LandingPageViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_landing_categories, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        categoriesBg.isVisible = false
//        categoriesHeader.isVisible = false
        viewAllButton.isVisible = false

        mAdapter = CategoriesListAdapter(object: BaseListItemCallback<Category> {
            override fun onItemClicked(item: Category) {
                val args = Bundle().apply {
                    putParcelable(CategoryDetailsFragment.ARG_CATEGORY_ITEM, item)
                    putString(CategoryDetailsFragment.ARG_TITLE, item.categoryName)
                }
                when(item.id.toInt()) {
                    1 -> {
                        parentFragment?.findNavController()?.navigate(R.id.action_allCategoriesFragment_to_movieFragment, args)
                    }
                    9 -> {
                        parentFragment?.findNavController()?.navigate(R.id.action_allCategoriesFragment_to_dramaSeriesFragment, args)
                    }
                    else -> {
                        parentFragment?.findNavController()?.navigate(R.id.action_allCategoriesFragment_to_categoryDetailsFragment, args)
                    }
                }
            }
        }, true)

        with(categoriesList) {
            layoutManager = GridLayoutManager(context, 2)
            adapter = mAdapter
        }

        observeList()
    }

    private fun observeList() {
        lifecycleScope.launchWhenStarted {
            viewModel.loadCategories.collectLatest {
                mAdapter.submitData(it)
            }
        }
    }
}