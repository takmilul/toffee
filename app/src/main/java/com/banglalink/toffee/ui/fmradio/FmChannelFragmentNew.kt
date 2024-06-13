package com.banglalink.toffee.ui.fmradio

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.banglalink.toffee.databinding.FragmentFmChannelsNewBinding
import com.banglalink.toffee.ui.channels.ChannelFragmentFactory
import com.banglalink.toffee.ui.common.BaseFragment

class FmChannelFragmentNew : BaseFragment(){
    private var _binding: FragmentFmChannelsNewBinding?=null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = Bundle().apply {
            putInt("sub_category_id", 0)
            putString("sub_category", "")
            putString("category", "Karaoke - Stingray")
            putString("title", "Karaoke - Stingray")
            putBoolean("show_selected", true)
            putBoolean("is_stingray", false)
            putBoolean("is_fmRadio", true)
        }
        childFragmentManager.fragmentFactory = ChannelFragmentFactory(args)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFmChannelsNewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}