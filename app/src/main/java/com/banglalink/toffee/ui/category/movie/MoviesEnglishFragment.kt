package com.banglalink.toffee.ui.category.movie

import com.banglalink.toffee.extension.observe

class MoviesEnglishFragment: MovieBaseFragment() {
    override val cardTitle: String = "English"
    companion object {
        @JvmStatic
        fun newInstance() = MoviesEnglishFragment()
    }

    override fun loadContent() {
        observe(viewModel.englishMovies){
            adapter.addAll(it)
        }
    }

    override fun onStop() {
        viewModel.englishMovies.removeObservers(viewLifecycleOwner)
        super.onStop()
    }
}