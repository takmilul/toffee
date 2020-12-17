package com.banglalink.toffee.ui.category.movie

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentMoviesPreviewBinding
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.BaseFragment

class MoviesPreviewFragment : BaseFragment() {
    private lateinit var moviePreviews: List<ChannelInfo>
    private lateinit var adapter: MoviesPreviewSliderAdapter
    private lateinit var binding: FragmentMoviesPreviewBinding
    private val viewModel by activityViewModels<MovieViewModel>()
    
    companion object {
        @JvmStatic
        fun newInstance() = MoviesPreviewFragment()
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?, ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_movies_preview, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.titleTextView.text = "Movie Previews"
        observeMoviePreviews()
        viewModel.loadMoviePreviews
    }

    private fun observeMoviePreviews() {
        observe(viewModel.moviePreviews){
            moviePreviews = it
            adapter = MoviesPreviewSliderAdapter(this)
            binding.viewPager.offscreenPageLimit = 2
            binding.viewPager.adapter = adapter
            binding.viewPager.setPageTransformer(ZoomInPageTransformer())
        }
    }

    override fun onStop() {
        viewModel.moviePreviews.removeObservers(viewLifecycleOwner)
        super.onStop()
    }
    
    inner class MoviesPreviewSliderAdapter(fragment: Fragment): FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int {
            return moviePreviews.size
        }
    
        override fun createFragment(position: Int): Fragment {
            return MoviesPreviewItemFragment.newInstance(moviePreviews[position])
        }
    }
}