package com.banglalink.toffee.ui.userchannel

import androidx.lifecycle.ViewModelProvider
import com.banglalink.toffee.R
import com.banglalink.toffee.model.ChannelVideo
import com.banglalink.toffee.ui.common.SingleListFragmentV2
import com.banglalink.toffee.ui.common.SingleListItemCallback

class ChannelVideosFragment : SingleListFragmentV2<ChannelVideo>(), SingleListItemCallback<ChannelVideo> {
    
    companion object {
        
        fun newInstance(): ChannelVideosFragment {
            return ChannelVideosFragment()
        }
    }
    
    override fun initAdapter() {
        mAdapter = ChannelVideoListAdapter(this)
        mViewModel = ViewModelProvider(this).get(ChannelVideosViewModel::class.java)
    }
    
    override fun onOpenMenu(item: ChannelVideo) {
        super.onOpenMenu(item)
    }
    
    override fun onItemClicked(item: ChannelVideo) {
        super.onItemClicked(item)
    }
    
    override fun getEmptyViewInfo(): Pair<Int, String?> {
        return Pair(R.drawable.ic_videos_empty, "You haven't uploaded any video yet")
    }
}