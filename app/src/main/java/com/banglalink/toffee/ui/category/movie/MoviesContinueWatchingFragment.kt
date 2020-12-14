package com.banglalink.toffee.ui.category.movie

class MoviesContinueWatchingFragment : MovieBaseFragment() {
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