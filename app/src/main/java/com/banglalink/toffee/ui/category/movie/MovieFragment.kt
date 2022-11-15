package com.banglalink.toffee.ui.category.movie

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.FirebaseParams
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.analytics.ToffeeEvents
import com.banglalink.toffee.databinding.FragmentMovieBinding
import com.banglalink.toffee.enums.PageType
import com.banglalink.toffee.extension.handleUrlShare
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.safeClick
import com.banglalink.toffee.model.Category
import com.banglalink.toffee.ui.category.CategoryDetailsFragment
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.home.LandingPageViewModel
import com.banglalink.toffee.util.BindingUtil
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class MovieFragment : BaseFragment() {
    @Inject lateinit var bindingUtil: BindingUtil
    private lateinit var category: Category
    private var _binding: FragmentMovieBinding ? = null
    private val binding get() = _binding!!
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
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        category = requireArguments().getParcelable(CategoryDetailsFragment.ARG_CATEGORY_ITEM)!!
        activity?.title = category.categoryName
        landingViewModel.pageType.value = PageType.Category
        landingViewModel.pageName.value = category.categoryName.uppercase(Locale.getDefault()) + "CATEGORY_PAGE"
        landingViewModel.featuredPageName.value = category.categoryName + " Page"
        landingViewModel.categoryId.value = category.id.toInt()
        mPref.categoryId.value = category.id.toInt()
        mPref.categoryName.value = category.categoryName
        landingViewModel.isDramaSeries.value = false
        ToffeeAnalytics.logEvent(ToffeeEvents.SCREEN_VIEW,  bundleOf(FirebaseParams.BROWSER_SCREEN to category.categoryName))
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMovieBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setCategoryIcon()
        observeCardsVisibility()
        viewModel.loadMovieCategoryDetail(category.id.toInt())
        binding.categoryMovieShare.safeClick({
            category.categoryShareUrl?.let { requireActivity().handleUrlShare(it) }
        })
    }

    private fun setCategoryIcon() {
        category.let {
            binding.categoryName.text = it.categoryName
            bindingUtil.bindCategoryIcon(binding.categoryIcon, it)
            binding.categoryIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(),R.color.colorAccent2))
        }
    }

    private fun observeCardsVisibility() {
        observe(viewModel.moviesContentCards){
            binding.featuredFragment.isVisible = it.featuredContent == 1
            binding.continueWatchingFragment.isVisible = it.continueWatching == 1
            binding.editorsChoiceFragment.isVisible = it.editorsChoice == 1
            binding.moviePreviewFragment.isVisible = it.moviePreviews == 1
            binding.trendingNowMoviesFragment.isVisible = it.trendingNow == 1
            binding.thrillerMoviesFragment.isVisible = it.thriller == 1
            binding.actionMoviesFragment.isVisible = it.action == 1
            binding.romanticMoviesFragment.isVisible = it.romantic == 1
            binding.banglaMoviesFragment.isVisible = it.bangla == 1
            binding.englishMoviesFragment.isVisible = it.english == 1
            binding.comingSoonFragment.isVisible = it.comingSoon == 1
            binding.telefilmFragment.isVisible = it.telefilm == 1
            binding.topMovieChannelsFragment.isVisible = it.topMovieChannels == 1
            binding.latestVideosFragment.isVisible = it.feed == 1
        }
    }

    override fun onStop() {
        viewModel.moviesContentCards.removeObservers(viewLifecycleOwner)
        super.onStop()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}