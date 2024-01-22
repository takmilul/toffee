package com.banglalink.toffee.ui.audiobook.carousel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.data.network.response.KabbikTopBannerApiResponse
import com.banglalink.toffee.databinding.FragmentCarouselContentBinding
import com.banglalink.toffee.extension.launchWithLifecycle
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.show
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.audiobook.AudioBookViewModel
import com.banglalink.toffee.ui.common.BaseFragment
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class CarouselContentFragment : BaseFragment(), BaseListItemCallback<KabbikTopBannerApiResponse.BannerItemBean> {
    private lateinit var binding: FragmentCarouselContentBinding
    private lateinit var mAdapter: CarouselContentAdapter
    private var slideJob: Job? = null
    private val viewModel by activityViewModels<AudioBookViewModel>()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCarouselContentBinding.inflate(inflater, container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAdapter = CarouselContentAdapter(this)
        binding.carouselViewpager.adapter = mAdapter
        TabLayoutMediator(binding.carouselIndicator, binding.carouselViewpager, true) { _, _ -> }.attach()
        observeList()

        launchWithLifecycle {
            viewModel.grantToken(
                success = { token->
                    viewModel.topBannerApi(token)
                },
                failure = {}
            )
        }
    }

    private fun observeList() {
        observe(viewModel.topBannerApiResponse){
            when(it){
                is Resource.Success->{
                    mAdapter.removeAll()
                    val freeItems = it.data.bannerItems.filter {item->
                        item.premium == 0 && item.price == 0
                    }
                    mAdapter.addAll(freeItems)
                    startPageScroll()
                    binding.carouselViewpager.show()
                }
                is Resource.Failure->{

                }
            }
        }
    }

    override fun onItemClicked(item: KabbikTopBannerApiResponse.BannerItemBean) {
        super.onItemClicked(item)
    }
    private fun startPageScroll() {
        slideJob?.cancel()
        slideJob = lifecycleScope.launch {
            while (isActive) {
                delay(5000)
                if (isActive && mAdapter.itemCount > 0) {
                    binding.carouselViewpager.currentItem = (binding.carouselViewpager.currentItem + 1) % mAdapter.itemCount
                }
            }
        }
    }
}