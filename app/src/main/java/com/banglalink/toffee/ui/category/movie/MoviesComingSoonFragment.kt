package com.banglalink.toffee.ui.category.movie

import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.model.ComingSoonContent
import com.banglalink.toffee.ui.common.MyBaseAdapter

class MoviesComingSoonFragment : MovieBaseFragment<ComingSoonContent>() {
    override val cardTitle: String = "Coming Soon"
    override val adapter: MyBaseAdapter<ComingSoonContent> by lazy { MoviesComingSoonAdapter() }
    
    companion object {
        @JvmStatic
        fun newInstance() = MoviesComingSoonFragment()
    }

    override fun loadContent() {
        observe(viewModel.comingSoonContents){
            adapter.addAll(it)
            showCard(it.isNotEmpty())
        }
        viewModel.loadComingSoonContents
    }

    override fun onStop() {
        viewModel.comingSoonContents.removeObservers(viewLifecycleOwner)
        super.onStop()
    }
}