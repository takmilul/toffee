package com.banglalink.toffee.ui.category.movie

import com.banglalink.toffee.extension.observe

class MoviesActionFragment: MovieBaseFragment() {
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