package com.banglalink.toffee.ui.landing

import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.forEach
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.analytics.ToffeeEvents
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.databinding.FragmentLandingCategoriesBinding
import com.banglalink.toffee.databinding.PlaceholderCategoriesBinding
import com.banglalink.toffee.enums.CategoryType
import com.banglalink.toffee.extension.px
import com.banglalink.toffee.extension.showLoadingAnimation
import com.banglalink.toffee.model.Category
import com.banglalink.toffee.ui.category.CategoryDetailsFragment
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.home.HomeViewModel
import com.banglalink.toffee.ui.home.LandingPageViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LandingCategoriesFragment: BaseFragment(), BaseListItemCallback<Category> {
    
    private lateinit var mAdapter: CategoriesListAdapter
    private var _binding: FragmentLandingCategoriesBinding? = null
    private val binding get() = _binding!!
    private val homeViewModel by activityViewModels<HomeViewModel>()
    private val viewModel by activityViewModels<LandingPageViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
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
        var isInitialized = false
        mAdapter = CategoriesListAdapter(this)
        
        binding.viewAllButton.setOnClickListener {
            parentFragment?.findNavController()?.navigate(R.id.menu_explore)
        }

        with(binding.placeholder) {
            val calculatedWidth = (Resources.getSystem().displayMetrics.widthPixels - (16.px * 3)) / 2.5    // 16dp margin
            val calculatedHeight = (calculatedWidth / 16) * 8.21    // Category card ratio = 16:8.21
            val iconSize = calculatedHeight / 2.62  // category_card_height : category_icon_height = 2.62:1
            this.forEach { placeholderView ->
                val binder = DataBindingUtil.bind<PlaceholderCategoriesBinding>(placeholderView)
                binder?.let {
                    it.categoryCardView.layoutParams.apply {
                        width = calculatedWidth.toInt()
                        height = calculatedHeight.toInt()
                    }
                    if (calculatedWidth < 136.px) {
                        (it.icon.layoutParams as ViewGroup.MarginLayoutParams).apply {
                            marginStart = 8.px
                            width = iconSize.toInt()
                            height = iconSize.toInt()
                        }
                        it.text.layoutParams.apply {
                            height = resources.getDimensionPixelOffset(R.dimen.placeholder_small_text_height)
                        }
                    }
                }
            }
        }
        
        with(binding.categoriesList) {
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                mAdapter.loadStateFlow.collectLatest {
                    val isLoading = it.source.refresh is LoadState.Loading || !isInitialized
                    val isEmpty = mAdapter.itemCount <= 0 && ! it.source.refresh.endOfPaginationReached
                    binding.placeholder.isVisible = isEmpty
                    binding.categoriesList.isVisible = ! isEmpty
                    binding.placeholder.showLoadingAnimation(isLoading)
                    isInitialized = true
                }
            }
            layoutManager = GridLayoutManager(context, 2, GridLayoutManager.HORIZONTAL, false)
            adapter = mAdapter
            setHasFixedSize(true)
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
        val args = Bundle().apply {
            putParcelable(CategoryDetailsFragment.ARG_CATEGORY_ITEM, item)
            putString(CategoryDetailsFragment.ARG_TITLE, item.categoryName)
        }
        ToffeeAnalytics.logEvent(ToffeeEvents.CATEGORY_EVENT+item.categoryName.lowercase().replace(" ", "_"))
        when(item.id.toInt()) {
            CategoryType.MOVIE.value -> {
                if (findNavController().currentDestination?.id != R.id.movieFragment && findNavController().currentDestination?.id==
                    R.id.menu_feed) {
                    findNavController().navigate(
                        R.id.action_landingCategoriesFragment_to_movieFragment,
                        args
                    )
                }
            }
            CategoryType.MUSIC.value ->{
                if (findNavController().currentDestination?.id != R.id.musicFragment && findNavController().currentDestination?.id==
                    R.id.menu_feed)
                {
                    findNavController().navigate(R.id.action_menu_feed_to_musicDetailsFragmant, args)
                }
            }
            CategoryType.DRAMA_SERIES.value -> {
                if (findNavController().currentDestination?.id != R.id.dramaSeriesFragment && findNavController().currentDestination?.id==
                    R.id.menu_feed)
                {
                    findNavController().navigate(R.id.action_landingCategoriesFragment_to_dramaSeriesFragment, args)
                }
            }
            else -> {
                if (findNavController().currentDestination?.id != R.id.categoryDetailsFragment && findNavController().currentDestination?.id==
                    R.id.menu_feed){
                    findNavController().navigate(R.id.action_landingCategoriesFragment_to_categoryDetailsFragment, args)
                   }
            }
        }
    }
}