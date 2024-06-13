package com.banglalink.toffee.ui.category.movie

import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.model.ChannelInfo

class MoviesEnglishFragment: MovieBaseFragment<ChannelInfo>() {
    override val cardTitle: String = "English"
    companion object {
        @JvmStatic
        fun newInstance() = MoviesEnglishFragment()
    }

    override fun loadContent() {
        observe(viewModel.englishMovies){
            adapter.removeAll()
            adapter.addAll(it)
            showCard(it.isNotEmpty())
        }
    }

    override fun onStop() {
        viewModel.englishMovies.removeObservers(viewLifecycleOwner)
        super.onStop()
    }
}