package com.banglalink.toffee.ui.category.movie

import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.model.ChannelInfo

class MoviesBanglaFragment: MovieBaseFragment<ChannelInfo>() {
    override val cardTitle: String = "Bangla"

    companion object {
        @JvmStatic
        fun newInstance() = MoviesBanglaFragment()
    }

    override fun loadContent() {
        observe(viewModel.banglaMovies){
            adapter.addAll(it)
        }
    }

    override fun onStop() {
        viewModel.banglaMovies.removeObservers(viewLifecycleOwner)
        super.onStop()
    }
}