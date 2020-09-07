package com.banglalink.toffee.ui.user_channel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentChannelVideosBinding

class ChannelVideosFragment : Fragment() {
    
    private lateinit var binding: FragmentChannelVideosBinding
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_channel_videos, container, false)
        return binding.root
    }
    
    companion object {
        
        fun createInstance(): ChannelVideosFragment {
            return ChannelVideosFragment()
        }
    }
}