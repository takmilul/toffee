package com.banglalink.toffee.ui.challenge

import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.model.Challenge
import com.banglalink.toffee.ui.common.SingleListFragmentV2
import com.banglalink.toffee.ui.common.SingleListItemCallback

class ChallengeFragment: SingleListFragmentV2<Challenge>(), SingleListItemCallback<Challenge> {
    
    companion object{
        fun newInstance() = ChallengeFragment()
    }
    
    override fun initAdapter() {
        mAdapter = ChallengeAdapter(this)
        mViewModel = ViewModelProvider(this).get(ChallengeViewModel::class.java)
    }

    override fun onItemClicked(item: Challenge) {
        super.onItemClicked(item)
        findNavController().navigate(R.id.action_challengesFragment_to_challengeResultFragment)
    }
}