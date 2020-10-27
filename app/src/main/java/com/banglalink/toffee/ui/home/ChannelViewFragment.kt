package com.banglalink.toffee.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import com.banglalink.toffee.BR
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.databinding.CatchupDetailsListHeaderNewBinding
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.channels.ChannelFragment
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.landing.UserChannelViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChannelViewFragment: BaseFragment(), BaseListItemCallback<ChannelInfo> {
    private lateinit var binding: CatchupDetailsListHeaderNewBinding
    private val viewModel by viewModels<UserChannelViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.catchup_details_list_header_new, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val channelInfo = requireArguments().getParcelable<ChannelInfo>(ARG_CHANNEL_INFO) ?: return
        binding.setVariable(BR.data, channelInfo)
        binding.setVariable(BR.callback, this)
        binding.executePendingBindings()

        if (channelInfo.isLive) {
            binding.channelDetailsGroup.isVisible = false
            val fragment = parentFragmentManager.findFragmentById(R.id.relatedContainer)
            if (fragment !is ChannelFragment) {
                parentFragmentManager.commit {
                    replace(
                        R.id.relatedContainer,
                        ChannelFragment.createInstance(getString(R.string.menu_channel_text))
                    )
                }
            }
        } else {
            parentFragmentManager.commit { replace(R.id.relatedContainer, CatchupDetailsFragment.createInstance(channelInfo)) }
        }

        binding.subscribeButton.setOnClickListener {
            viewModel.toggleSubscriptionStatus(channelInfo.content_provider_id?.toLongOrNull() ?: 0L)
        }

        observe(viewModel.channelSubscriberCount) {
            binding.channelSubCount.text = it
        }

        observe(viewModel.isChannelSubscribed) {
            binding.subscribeButton.setSubscriptionInfo(it)
        }

        val contentProviderId =  channelInfo.content_provider_id?.toLongOrNull() ?: 0L
        val isOwner = if(contentProviderId == mPref.customerId.toLong()) 0 else 1
        viewModel.getChannelInfo(contentProviderId, isOwner)
    }

    companion object {
        const val ARG_CHANNEL_INFO = "arg-channel-info"

        fun newInstance(channelInfo: ChannelInfo): ChannelViewFragment {
            val args = Bundle()
            args.putParcelable(ARG_CHANNEL_INFO, channelInfo)
            val fragment = ChannelViewFragment()
            fragment.arguments = args
            return fragment
        }
    }
}