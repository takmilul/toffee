package com.banglalink.toffee.ui.challenge

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListFragment
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.model.Challenge

class ChallengeFragment: BaseListFragment<Challenge>(), BaseListItemCallback<Challenge> {

    override val mAdapter by lazy { ChallengeAdapter(this) }
    override val mViewModel by viewModels<ChallengeViewModel>()
    
    companion object{
        fun newInstance() = ChallengeFragment()
    }
    
    override fun onItemClicked(item: Challenge) {
        super.onItemClicked(item)
        findNavController().navigate(R.id.action_challengesFragment_to_challengeResultFragment)
    }
}