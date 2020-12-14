package com.banglalink.toffee.ui.category.movie

import com.banglalink.toffee.extension.observe

class MoviesThrillerFragment: MovieBaseFragment() {
    override val cardTitle: String = "Thriller"
    
    companion object {
        @JvmStatic
        fun newInstance() = MoviesThrillerFragment()
    }

    override fun loadContent() {
        observe(viewModel.thrillerMovies){
            adapter.addAll(it)
        }
    }

    override fun onStop() {
        viewModel.thrillerMovies.removeObservers(viewLifecycleOwner)
        super.onStop()
    }
}