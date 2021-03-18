package com.banglalink.toffee.ui.category.movie

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentMovieBinding
import com.banglalink.toffee.enums.PageType
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.setVisibility
import com.banglalink.toffee.model.Category
import com.banglalink.toffee.ui.category.CategoryDetailsFragment
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.home.LandingPageViewModel
import com.banglalink.toffee.util.bindCategoryImage

class MovieFragment : BaseFragment() {
    private lateinit var category: Category
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
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMovieBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        category = requireArguments().getParcelable(CategoryDetailsFragment.ARG_CATEGORY_ITEM)!!
        activity?.title = category.categoryName
        landingViewModel.pageType.value = PageType.Category
        landingViewModel.categoryId.value = category.id.toInt()
        landingViewModel.isDramaSeries.value = false
        setCategoryIcon()
        observeCardsVisibility()
        viewModel.loadMovieCategoryDetail
    }

    private fun setCategoryIcon() {
        category.let {
            binding.categoryName.text = it.categoryName
            bindCategoryImage(binding.categoryIcon, it)
            binding.categoryIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(),R.color.colorAccent2))
        }
    }

    private fun observeCardsVisibility() {
        observe(viewModel.moviesContentCards){
            binding.featuredFragment.setVisibility(it.featuredContent == 1)
            binding.continueWatchingFragment.setVisibility(it.continueWatching == 1)
            binding.editorsChoiceFragment.setVisibility(it.editorsChoice == 1)
            binding.moviePreviewFragment.setVisibility(it.moviePreviews == 1)
            binding.trendingNowMoviesFragment.setVisibility(it.trendingNow == 1)
            binding.thrillerMoviesFragment.setVisibility(it.thriller == 1)
            binding.actionMoviesFragment.setVisibility(it.action == 1)
            binding.romanticMoviesFragment.setVisibility(it.romantic == 1)
            binding.banglaMoviesFragment.setVisibility(it.bangla == 1)
            binding.englishMoviesFragment.setVisibility(it.english == 1)
            binding.comingSoonFragment.setVisibility(it.comingSoon == 1)
            binding.telefilmFragment.setVisibility(it.telefilm == 1)
            binding.topMovieChannelsFragment.setVisibility(it.topMovieChannels == 1)
            binding.latestVideosFragment.setVisibility(it.feed == 1)
        }
    }

    override fun onStop() {
        viewModel.moviesContentCards.removeObservers(viewLifecycleOwner)
        super.onStop()
    }
}