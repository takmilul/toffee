package com.banglalink.toffee.ui.premium

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.banglalink.toffee.common.paging.ProviderIconCallback
import com.banglalink.toffee.databinding.FragmentPacDetailsBinding
import com.banglalink.toffee.databinding.FragmentPremChannelsBinding
import com.banglalink.toffee.enums.PageType
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.home.LandingPageViewModel
import com.banglalink.toffee.ui.widget.MarginItemDecoration
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PremiumChannelFragment: BaseFragment(), ProviderIconCallback<ChannelInfo> {
    private var _binding: FragmentPremChannelsBinding?=null
    private val binding get() = _binding!!

    private lateinit var mAdapter: PremiumChannelAdapter
    private val landingPageViewModel by activityViewModels<LandingPageViewModel>()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        _binding = FragmentPremChannelsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAdapter = PremiumChannelAdapter(this)

        with(binding.premiumChannelListview) {

            adapter = mAdapter


        }

        observeList()

    }
    private fun observeList() {
        viewLifecycleOwner.lifecycleScope.launch {
            val content = if (landingPageViewModel.pageType.value == PageType.Landing) {
                landingPageViewModel.loadChannels()
            }
            else {
                landingPageViewModel.loadChannels()
            }
            content.collectLatest {

                mAdapter.submitData(it)


            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        binding.premiumChannelListview.adapter = null
        _binding = null
    }
}