package com.banglalink.toffee.ui.challenge

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.databinding.FragmentChallengeDetailBinding
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.util.unsafeLazy

class ChallengeDetailFragment : Fragment(), BaseListItemCallback<String> {

    private lateinit var mAdapter: ChallengeDetailAdapter
    private var _binding: FragmentChallengeDetailBinding ? = null
    private val binding get() = _binding!!
    private val viewModel by unsafeLazy { ViewModelProviders.of(this).get(ChallengeDetailViewModel::class.java) }
    
    companion object {
        
        @JvmStatic
        fun newInstance() =
            ChallengeDetailFragment()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_challenge_detail, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAdapter = ChallengeDetailAdapter(this)
        binding.rewardList.adapter = mAdapter
        
        observeData()
        viewModel.loadData()
    }

    private fun observeData() {
        observe(viewModel.liveData){
            when(it){
                is Success -> {
                    mAdapter.addAll(it.data.rewardList!!)
                }
                is Failure -> {
                    activity?.showToast(it.error.msg)
                }
            }
        }
    }
}