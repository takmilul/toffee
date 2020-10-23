package com.banglalink.toffee.ui.challenge

import com.banglalink.toffee.common.paging.BaseListFragment
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.common.paging.BasePagingViewModel
import com.banglalink.toffee.model.ChannelInfo

class ChallengeResultVideosFragment: BaseListFragment<ChannelInfo>(), BaseListItemCallback<ChannelInfo> {

//    override val mViewModel by viewModels<ChallengeResultVideosViewModel>()
    override val mAdapter by lazy { ChallengeResultVideosAdapter(this) }

    companion object{
        fun newInstance(): ChallengeResultVideosFragment{
            return ChallengeResultVideosFragment()
        }
    }

    override val mViewModel: BasePagingViewModel<ChannelInfo>
        get() = TODO("Not yet implemented")

    /*override fun initAdapter() {
        mAdapter = ChallengeResultVideosAdapter(this)
        mViewModel = ViewModelProviders.of(this).get(ChallengeResultVideosViewModel::class.java)
        mViewModel.enableToolbar = true
    }*/
}