package com.banglalink.toffee.ui.category.movie

import android.os.Bundle
import android.view.View
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.model.ChannelInfo

class MoviesTrendingNowFragment: MovieBaseFragment<ChannelInfo>() {
    override val cardTitle: String = "Trending"

    companion object {
        @JvmStatic
        fun newInstance() = MoviesTrendingNowFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadTrendingNowMovies()

    }
    
    override fun loadContent() {
        observe(viewModel.trendingNowMovies){
            adapter.removeAll()
            adapter.addAll(it)
            showCard(it.isNotEmpty())
        }
    }

    override fun onStop() {
        viewModel.trendingNowMovies.removeObservers(viewLifecycleOwner)
        super.onStop()
    }
}