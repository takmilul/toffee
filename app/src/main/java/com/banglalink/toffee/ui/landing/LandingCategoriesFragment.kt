package com.banglalink.toffee.ui.landing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.databinding.FragmentLandingCategoriesBinding
import com.banglalink.toffee.model.Category
import com.banglalink.toffee.ui.category.CategoryDetailsFragment
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.home.HomeViewModel
import com.banglalink.toffee.ui.home.LandingPageViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class LandingCategoriesFragment: BaseFragment() {
    private lateinit var mAdapter: CategoriesListAdapter

    private val viewModel by activityViewModels<LandingPageViewModel>()
    private val homeViewModel by activityViewModels<HomeViewModel>()
    private var _binding: FragmentLandingCategoriesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLandingCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        binding.categoriesList.adapter = null
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAdapter = CategoriesListAdapter(object : BaseListItemCallback<Category>{
            override fun onItemClicked(item: Category) {
                val args = Bundle().apply {
                    putParcelable(CategoryDetailsFragment.ARG_CATEGORY_ITEM, item)
                    putString(CategoryDetailsFragment.ARG_TITLE, item.categoryName)
                }
                when(item.id.toInt()) {
                    1 -> {
                        if (findNavController().currentDestination?.id != R.id.movieFragment && findNavController().currentDestination?.id==
                                R.id.menu_feed) {
                            findNavController().navigate(
                                R.id.action_landingCategoriesFragment_to_movieFragment,
                                args
                            )
                        }
                    }
                    9 -> {
                        if (findNavController().currentDestination?.id != R.id.dramaSeriesFragment && findNavController().currentDestination?.id==
                                R.id.menu_feed)
                        {
                            findNavController().navigate(R.id.action_landingCategoriesFragment_to_dramaSeriesFragment, args)
                        }
                    }
                    else -> {
                        if (findNavController().currentDestination?.id != R.id.categoryDetailsFragment && findNavController().currentDestination?.id==
                                R.id.menu_feed){
                            findNavController().navigate(R.id.action_landingCategoriesFragment_to_categoryDetailsFragment, args)}
                    }
                }
            }
        })

        binding.viewAllButton.setOnClickListener {
            homeViewModel.switchBottomTab.postValue(3)
        }

        with(binding.categoriesList) {
            layoutManager = GridLayoutManager(context, 2, GridLayoutManager.HORIZONTAL, false)
            adapter = mAdapter
        }

        /*val categories = PagingData.from(listOf(
            Category(),
            Category(),
            Category(),
            Category(),
            Category(),
            Category(),
        ))
        lifecycleScope.launch { 
            mAdapter.submitData(categories)
        }*/
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