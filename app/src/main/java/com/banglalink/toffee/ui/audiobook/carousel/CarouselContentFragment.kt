package com.banglalink.toffee.ui.audiobook.carousel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.databinding.FragmentCarouselContentBinding
import com.banglalink.toffee.extension.hide
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.show
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.home.LandingPageViewModel
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class CarouselContentFragment : BaseFragment(), BaseListItemCallback<PosterDemo> {
    private lateinit var binding: FragmentCarouselContentBinding
    private lateinit var mAdapter: CarouselContentAdapter
    private var slideJob: Job? = null
    private val viewModel by activityViewModels<LandingPageViewModel>()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCarouselContentBinding.inflate(inflater, container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.featuredJob?.cancel()
        viewModel.featuredJob = null
        mAdapter = CarouselContentAdapter(this)
        binding.carouselViewpager.adapter = mAdapter
        TabLayoutMediator(binding.carouselIndicator, binding.carouselViewpager, true) { _, _ -> }.attach()
        observeList()
    }

    private fun observeList() {
        val demoImages = listOf<PosterDemo>(
            PosterDemo(
                "https://s3-alpha-sig.figma.com/img/0e08/dfc1/4ffe27ea0c7e170a7e92494f6e0757a3?Expires=1705881600&Key-Pair-Id=APKAQ4GOSFWCVNEHN3O4&Signature=CB6CRyxQOx1ZHBnvRGDzrKLTN3DkDaqvGutZG93DJDV-u1yoyRCGV1MswVqCqRcpjy-ktqvCCZ0bKYBDr0U~SxDuArArsURNvAfBIyrPJddm-hfXWG9IPMwMzGEqBjssaosFfhyoi-BgG848GSm0Feplhgp3FKPH0LjKQd-JVr9mEkXdfmGz6~yleF~-Ualp8uWNbK~Hwr-zJyJt~6QKx7pOpRxj2AFPHnsQLPIePjsKrwkPb9vlYZBVpiwxGbhN8ARNG-CVMQ30Ai-gdBk6EMv~PQP-m5IGA721zNBdsfjkkWcMc7fWafWDywWBOH9NY4EQlek1Tf-WYkE~HV~Kdw__"
            ),
            PosterDemo(
                "https://www.boierduniya.com/drive/2020/02/Base-1-118.jpg"
            ),
            PosterDemo(
                "https://static-01.daraz.com.bd/p/3d8260806fa6d48e79e0dba156dfe58d.jpg_750x750.jpg_.webp"
            )
        )
        demoImages.let {
            mAdapter.removeAll()
            mAdapter.addAll(it)
            startPageScroll()
            binding.carouselViewpager.show()
        }
    }

    override fun onItemClicked(item: PosterDemo) {
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