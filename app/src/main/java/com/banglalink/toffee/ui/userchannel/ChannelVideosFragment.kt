package com.banglalink.toffee.ui.userchannel

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListFragment
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.model.ChannelInfo

class ChannelVideosFragment : BaseListFragment<ChannelInfo>(), BaseListItemCallback<ChannelInfo> {
    
    private var enableToolbar: Boolean = false

    override val mAdapter by lazy { ChannelVideoListAdapter(this) }
    override val mViewModel by viewModels<ChannelVideosViewModel>()
    
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
    
    /*override fun initAdapter() {
        mAdapter = ChannelVideoListAdapter(this)
        mViewModel = ViewModelProvider(this).get(ChannelVideosViewModel::class.java)
        enableToolbar = arguments?.getBoolean(SHOW_TOOLBAR)?:false
        mViewModel.enableToolbar = enableToolbar
    }*/
    
    override fun getEmptyViewInfo(): Pair<Int, String?> {
        return Pair(R.drawable.ic_videos_empty, "You haven't uploaded any video yet")
    }
}