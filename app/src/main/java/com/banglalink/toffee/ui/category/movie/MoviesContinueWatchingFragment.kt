package com.banglalink.toffee.ui.category.movie

import com.banglalink.toffee.model.ChannelInfo

class MoviesContinueWatchingFragment : MovieBaseFragment<ChannelInfo>() {
    override val cardTitle: String = "Continue Watching"

    companion object {
        @JvmStatic
        fun newInstance() = MoviesContinueWatchingFragment()
    }

    override fun loadContent() {
        /*observe(viewModel.englishMovies){
            adapter.addAll(it)
        }*/
    }
}