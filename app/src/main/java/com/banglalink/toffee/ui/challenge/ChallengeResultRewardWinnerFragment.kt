package com.banglalink.toffee.ui.challenge

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.databinding.FragmentChallengeResultRewardWinnerBinding
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.ChallengeReward
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success

class ChallengeResultRewardWinnerFragment: Fragment(), BaseListItemCallback<ChallengeReward> {
    private lateinit var mAdapter: ChallengeResultRewardWinnerAdapter
    private var _binding: FragmentChallengeResultRewardWinnerBinding ? = null
    private val binding get() = _binding!!
    val viewModel by viewModels<ChallengeResultRewardWinnerViewModel>()

    companion object {
        @JvmStatic
        fun newInstance() = ChallengeResultRewardWinnerFragment ()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentChallengeResultRewardWinnerBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAdapter = ChallengeResultRewardWinnerAdapter(this)
        binding.listview.adapter = mAdapter
        observeList()
        viewModel.loadData()
    }

    private fun observeList() {
        viewModel.listData.observe(viewLifecycleOwner) {
            when(it) {
                is Success -> {
                    val itemCount = it.data.size
                    binding.winnerCountTextView.text = "Reward Winners ($itemCount)"
                    mAdapter.addAll(it.data)
                }
                is Failure -> {
                    activity?.showToast(it.error.msg)
                }
            }
        }
    }
}