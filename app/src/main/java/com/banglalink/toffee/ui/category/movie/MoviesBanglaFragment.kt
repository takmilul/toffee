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
            adapter.removeAll()
            adapter.addAll(it)
            showCard(it.isNotEmpty())
        }
    }

    override fun onStop() {
        viewModel.banglaMovies.removeObservers(viewLifecycleOwner)
        super.onStop()
    }
}