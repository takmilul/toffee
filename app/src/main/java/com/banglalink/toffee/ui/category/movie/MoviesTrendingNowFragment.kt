package com.banglalink.toffee.ui.category.movie

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.ProviderIconCallback
import com.banglalink.toffee.databinding.LayoutHorizontalContentContainerBinding
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.common.ContentReactionCallback
import com.banglalink.toffee.ui.home.LandingPageViewModel

class MoviesTrendingNowFragment: BaseFragment(), ProviderIconCallback<ChannelInfo>, ContentReactionCallback<ChannelInfo> {
    private lateinit var adapter: MoviesAdapter
    private lateinit var binding: LayoutHorizontalContentContainerBinding
    private val viewModel by activityViewModels<MovieViewModel>()
    private val landingPageViewModel by activityViewModels<LandingPageViewModel>()

    companion object {
        @JvmStatic
        fun newInstance() = MoviesTrendingNowFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.layout_horizontal_content_container, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.titleTextView.text = "Trending Now"
        adapter = MoviesAdapter(this, false)
        binding.listView.adapter = adapter
        loadContent()
        viewModel.loadTrendingNowMovies()
    }

    private fun loadContent() {
        observe(viewModel.trendingNowMovies){
            adapter.addAll(it)
        }
    }
}