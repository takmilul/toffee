package com.banglalink.toffee.ui.userchannel

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.banglalink.toffee.common.paging.BaseListFragment
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.model.ChannelInfo

class ChannelPlaylistVideosFragment : BaseListFragment<ChannelInfo>(), BaseListItemCallback<ChannelInfo> {
    
    private var enableToolbar: Boolean = false

    override val mAdapter by lazy { ChannelPlaylistVideosListAdapter(this) }
    override val mViewModel by viewModels<ChannelPlaylistVideosViewModel>()
    
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
    
    /*override fun initAdapter() {
        mAdapter = ChannelPlaylistVideosListAdapter(this)
        mViewModel = ViewModelProvider(this).get(ChannelPlaylistVideosViewModel::class.java)
        enableToolbar = arguments?.getBoolean(SHOW_TOOLBAR) ?: false
        mViewModel.enableToolbar = enableToolbar
    }*/
}