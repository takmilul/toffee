package com.banglalink.toffee.ui.challenge

import androidx.lifecycle.ViewModelProviders
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.SingleListFragmentV2
import com.banglalink.toffee.ui.common.SingleListItemCallback

class ChallengeResultVideosFragment: SingleListFragmentV2<ChannelInfo>(), SingleListItemCallback<ChannelInfo> {
    
    companion object{
        fun newInstance(): ChallengeResultVideosFragment{
            return ChallengeResultVideosFragment()
        }
    }
    
    override fun initAdapter() {
        mAdapter = ChallengeResultVideosAdapter(this)
        mViewModel = ViewModelProviders.of(this).get(ChallengeResultVideosViewModel::class.java)
        mViewModel.enableToolbar = true
    }
}