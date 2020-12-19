package com.banglalink.toffee.ui.category.movie

import android.os.Bundle
import android.view.View
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.model.ChannelInfo

class MoviesTelefilmFragment: MovieBaseFragment<ChannelInfo>() {
    override val cardTitle: String = "Telefilm"
    companion object {
        @JvmStatic
        fun newInstance() = MoviesTelefilmFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadTelefilms
    }
    
    override fun loadContent() {
        observe(viewModel.telefilms){
            adapter.addAll(it)
        }
    }

    override fun onStop() {
        viewModel.telefilms.removeObservers(viewLifecycleOwner)
        super.onStop()
    }
}