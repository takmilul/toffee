package com.banglalink.toffee.ui.category.movie

import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.model.ChannelInfo

class MoviesActionFragment: MovieBaseFragment<ChannelInfo>() {
    override val cardTitle: String = "Action"
    
    companion object {
        @JvmStatic
        fun newInstance() = MoviesActionFragment()
    }

    override fun loadContent() {
        observe(viewModel.actionMovies){
            adapter.addAll(it)
        }
    }

    override fun onStop() {
        viewModel.actionMovies.removeObservers(viewLifecycleOwner)
        super.onStop()
    }
}