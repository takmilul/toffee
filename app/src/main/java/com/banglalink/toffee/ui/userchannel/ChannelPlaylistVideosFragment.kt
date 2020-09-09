package com.banglalink.toffee.ui.userchannel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.banglalink.toffee.R
import com.banglalink.toffee.model.ChannelVideo
import com.banglalink.toffee.ui.common.SingleListFragmentV2
import com.banglalink.toffee.ui.common.SingleListItemCallback

class ChannelPlaylistVideosFragment : SingleListFragmentV2<ChannelVideo>(), SingleListItemCallback<ChannelVideo> {
    
    private var enableToolbar: Boolean = false
    
    companion object {
        private const val SHOW_TOOLBAR = "enableToolbar"
        fun newInstance(enableToolbar: Boolean): ChannelPlaylistVideosFragment {
            val instance = ChannelPlaylistVideosFragment()
            val bundle = Bundle()
            bundle.putBoolean(SHOW_TOOLBAR, enableToolbar)
            instance.arguments = bundle
            return instance
        } 
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        
        return inflater.inflate(R.layout.fragment_channel_playlist_videos, container, false)
    }
    
    override fun initAdapter() {
        mAdapter = ChannelPlaylistVideosListAdapter(this)
        mViewModel = ViewModelProvider(this).get(ChannelPlaylistVideosViewModel::class.java)
        enableToolbar = arguments?.getBoolean(SHOW_TOOLBAR) ?: false
        mViewModel.enableToolbar = enableToolbar
    }
}