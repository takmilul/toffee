package com.banglalink.toffee.ui.userchannel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.banglalink.toffee.R

class ChannelPlaylistVideosFragment : Fragment() {
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        
        return inflater.inflate(R.layout.fragment_channel_playlist_videos, container, false)
    }
    
    companion object {
        fun newInstance() = ChannelPlaylistVideosFragment()
    }
}