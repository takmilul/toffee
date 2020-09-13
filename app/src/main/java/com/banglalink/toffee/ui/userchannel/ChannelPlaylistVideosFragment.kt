package com.banglalink.toffee.ui.userchannel

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.SingleListFragmentV2
import com.banglalink.toffee.ui.common.SingleListItemCallback

class ChannelPlaylistVideosFragment : SingleListFragmentV2<ChannelInfo>(), SingleListItemCallback<ChannelInfo> {
    
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
    
    override fun initAdapter() {
        mAdapter = ChannelPlaylistVideosListAdapter(this)
        mViewModel = ViewModelProvider(this).get(ChannelPlaylistVideosViewModel::class.java)
        enableToolbar = arguments?.getBoolean(SHOW_TOOLBAR) ?: false
        mViewModel.enableToolbar = enableToolbar
    }
}