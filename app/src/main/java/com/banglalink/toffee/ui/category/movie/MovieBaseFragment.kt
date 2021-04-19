package com.banglalink.toffee.ui.category.movie

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import com.banglalink.toffee.common.paging.ProviderIconCallback
import com.banglalink.toffee.databinding.LayoutHorizontalContentContainerBinding
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.MyChannelNavParams
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.common.MyBaseAdapterV2
import com.banglalink.toffee.ui.home.LandingPageViewModel

abstract class MovieBaseFragment<T: Any>: HomeBaseFragment(), ProviderIconCallback<T> {
    protected abstract val cardTitle: String
    protected open val adapter: MyBaseAdapterV2<T> by lazy { MoviesAdapter(this) }
    private var _binding: LayoutHorizontalContentContainerBinding ? = null
    private val binding get() = _binding!!
    protected val viewModel by activityViewModels<MovieViewModel>()
    private val landingPageViewModel by activityViewModels<LandingPageViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = LayoutHorizontalContentContainerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.titleTextView.text = cardTitle
        binding.listView.adapter = adapter
        loadContent()
    }

    protected abstract fun loadContent()

    protected fun showCard(isShow: Boolean) {
        binding.root.isVisible = isShow
    }
    
    override fun onItemClicked(item: T) {
        homeViewModel.fragmentDetailsMutableLiveData.postValue(item)
    }

    override fun onOpenMenu(view: View, item: T) {
        if (item is ChannelInfo) {
            super.onOptionClicked(view, item)
        }
    }

    override fun onProviderIconClicked(item: T) {
        if (item is ChannelInfo) {
            homeViewModel.myChannelNavLiveData.value = MyChannelNavParams(item.channel_owner_id)
        }
    }

    override fun removeItemNotInterestedItem(channelInfo: ChannelInfo) {}
    override fun hideShareMenuItem(hide: Boolean): Boolean {
        return true
    }
}