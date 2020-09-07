package com.banglalink.toffee.ui.user_channel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentChannelPlaylistsBinding

class ChannelPlaylistsFragment : Fragment() {
    
    private lateinit var binding: FragmentChannelPlaylistsBinding
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_channel_playlists, container, false)
        return binding.root
    }
    
    companion object {
       
        fun createInstance(): ChannelPlaylistsFragment {
            return ChannelPlaylistsFragment()
        }
    }
}