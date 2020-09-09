package com.banglalink.toffee.ui.userchannel

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.banglalink.toffee.R
import com.banglalink.toffee.model.ChannelVideo
import com.banglalink.toffee.ui.common.SingleListFragmentV2
import com.banglalink.toffee.ui.common.SingleListItemCallback

class ChannelVideosFragment : SingleListFragmentV2<ChannelVideo>(), SingleListItemCallback<ChannelVideo> {
    
    private var enableToolbar: Boolean = false
    
    companion object {
        private const val SHOW_TOOLBAR = "enableToolbar"
        fun newInstance(enableToolbar: Boolean): ChannelVideosFragment {
            val instance = ChannelVideosFragment()
            val bundle = Bundle()
            bundle.putBoolean(SHOW_TOOLBAR, enableToolbar)
            instance.arguments = bundle
            return instance
        }
    }
    
    override fun initAdapter() {
        mAdapter = ChannelVideoListAdapter(this)
        mViewModel = ViewModelProvider(this).get(ChannelVideosViewModel::class.java)
        enableToolbar = arguments?.getBoolean(SHOW_TOOLBAR)?:false
        mViewModel.enableToolbar = enableToolbar
    }
    
    override fun getEmptyViewInfo(): Pair<Int, String?> {
        return Pair(R.drawable.ic_videos_empty, "You haven't uploaded any video yet")
    }
}