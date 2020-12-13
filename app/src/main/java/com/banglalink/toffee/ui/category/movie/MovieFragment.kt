package com.banglalink.toffee.ui.category.movie

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentMovieBinding
import com.banglalink.toffee.enums.PageType
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.model.UgcCategory
import com.banglalink.toffee.ui.category.CategoryDetailsFragment
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.home.LandingPageViewModel

class MovieFragment : BaseFragment() {
    private lateinit var category: UgcCategory
    private lateinit var binding: FragmentMovieBinding
    private val landingViewModel by activityViewModels<LandingPageViewModel>()
    private val viewModel by activityViewModels<MovieViewModel>()

    companion object {
        @JvmStatic
        fun newInstance() = MovieFragment().apply {
            arguments = Bundle().also {
                it.putParcelable(CategoryDetailsFragment.ARG_CATEGORY_ITEM, category)
            }
        }
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_movie, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        category = requireArguments().getParcelable(CategoryDetailsFragment.ARG_CATEGORY_ITEM)!!
        activity?.title = category.categoryName
        landingViewModel.pageType.value = PageType.Category
        landingViewModel.categoryId.value = category.id.toInt()
        observeCardsVisibility()
        viewModel.loadMovieCategoryDetail()
    }

    private fun observeCardsVisibility() {
        observe(viewModel.movieContentCards){
            binding.moviePreviewFragment.visibility = if (it.moviePreview == 1) View.VISIBLE else View.GONE
            binding.comingSoonFragment.visibility = if (it.commingSoon == 1) View.VISIBLE else View.GONE
        }
    }
}