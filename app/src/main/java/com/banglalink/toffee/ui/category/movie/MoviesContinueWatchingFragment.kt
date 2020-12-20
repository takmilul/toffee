package com.banglalink.toffee.ui.category.movie

import androidx.lifecycle.lifecycleScope
import com.banglalink.toffee.model.ChannelInfo
import kotlinx.coroutines.flow.collectLatest

class MoviesContinueWatchingFragment : MovieBaseFragment<ChannelInfo>() {
    override val cardTitle: String = "Continue Watching"

    companion object {
        @JvmStatic
        fun newInstance() = MoviesContinueWatchingFragment()
    }

    override fun loadContent() {
        lifecycleScope.launchWhenStarted {
            viewModel.getContinueWatchingFlow(1).collectLatest {
                adapter.removeAll()
                adapter.addAll(it)
            }
        }
    }
}