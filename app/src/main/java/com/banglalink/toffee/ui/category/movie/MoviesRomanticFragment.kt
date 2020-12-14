package com.banglalink.toffee.ui.category.movie

import com.banglalink.toffee.extension.observe

class MoviesRomanticFragment: MovieBaseFragment() {
    override val cardTitle: String = "Romantic"
    
    companion object {
        @JvmStatic
        fun newInstance() = MoviesRomanticFragment()
    }

    override fun loadContent() {
        observe(viewModel.romanticMovies){
            adapter.addAll(it)
        }
    }

    override fun onStop() {
        viewModel.romanticMovies.removeObservers(viewLifecycleOwner)
        super.onStop()
    }
}