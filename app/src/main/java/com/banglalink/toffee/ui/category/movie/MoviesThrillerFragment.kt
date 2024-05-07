package com.banglalink.toffee.ui.category.movie

import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.model.ChannelInfo

class MoviesThrillerFragment: MovieBaseFragment<ChannelInfo>() {
    override val cardTitle: String = "Thriller"
    
    companion object {
        @JvmStatic
        fun newInstance() = MoviesThrillerFragment()
    }

    override fun loadContent() {
        observe(viewModel.thrillerMovies){
            adapter.removeAll()
            adapter.addAll(it)
            showCard(it.isNotEmpty())
        }
    }

    override fun onStop() {
        viewModel.thrillerMovies.removeObservers(viewLifecycleOwner)
        super.onStop()
    }
}