package com.banglalink.toffee.ui.category.movie

import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.model.ChannelInfo

class MoviesRomanticFragment: MovieBaseFragment<ChannelInfo>() {
    override val cardTitle: String = "Romantic"
    
    companion object {
        @JvmStatic
        fun newInstance() = MoviesRomanticFragment()
    }

    override fun loadContent() {
        observe(viewModel.romanticMovies){
            adapter.addAll(it)
            showCard(it.isNotEmpty())
        }
    }

    override fun onStop() {
        viewModel.romanticMovies.removeObservers(viewLifecycleOwner)
        super.onStop()
    }
}