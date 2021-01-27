package com.banglalink.toffee.ui.channels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.banglalink.toffee.databinding.FragmentAllTvChannelsBinding
import com.banglalink.toffee.ui.common.BaseFragment

class ChannelFragmentNew: BaseFragment() {
    private lateinit var binding: FragmentAllTvChannelsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAllTvChannelsBinding.inflate(inflater, container, false)
        return binding.root
    }
}