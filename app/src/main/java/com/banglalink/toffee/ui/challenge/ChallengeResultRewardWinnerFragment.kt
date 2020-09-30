package com.banglalink.toffee.ui.challenge

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.banglalink.toffee.R
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.ChallengeReward
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.ui.common.SingleListItemCallback
import com.banglalink.toffee.util.unsafeLazy
import kotlinx.android.synthetic.main.fragment_challenge_result_reward_winner.*

class ChallengeResultRewardWinnerFragment: Fragment(), SingleListItemCallback<ChallengeReward> {
    private lateinit var mAdapter: ChallengeResultRewardWinnerAdapter

    val viewModel by unsafeLazy {
        ViewModelProvider(this).get(ChallengeResultRewardWinnerViewModel::class.java)
    }

    companion object {
        @JvmStatic
        fun newInstance() = ChallengeResultRewardWinnerFragment ()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_challenge_result_reward_winner, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAdapter = ChallengeResultRewardWinnerAdapter(this)
        listview.adapter = mAdapter
        observeList()
        viewModel.loadData()
    }

    private fun observeList() {
        viewModel.listData.observe(viewLifecycleOwner, Observer {
            when(it) {
                is Success -> {
                    val itemCount = it.data.size
                    winnerCountTextView.text = "Reward Winners ($itemCount)"
                    mAdapter.addAll(it.data)
                }
                is Failure -> {
                    activity?.showToast(it.error.msg)
                }
            }
        })
    }
}