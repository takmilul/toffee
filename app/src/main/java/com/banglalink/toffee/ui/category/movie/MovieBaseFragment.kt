package com.banglalink.toffee.ui.category.movie

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import com.banglalink.toffee.R
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
    private lateinit var binding: LayoutHorizontalContentContainerBinding
    protected val viewModel by activityViewModels<MovieViewModel>()
    private val landingPageViewModel by activityViewModels<LandingPageViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.layout_horizontal_content_container, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.titleTextView.text = cardTitle
        binding.listView.adapter = adapter
        loadContent()
    }

    protected abstract fun loadContent()

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
            homeViewModel.myChannelNavLiveData.value = MyChannelNavParams(item.id.toInt(), item.channel_owner_id, item.isSubscribed)
        }
    }

    override fun removeItemNotInterestedItem(channelInfo: ChannelInfo) {}
    override fun hideShareMenuItem(hide: Boolean): Boolean {
        return true
    }
}