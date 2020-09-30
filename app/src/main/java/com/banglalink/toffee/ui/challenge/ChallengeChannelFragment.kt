package com.banglalink.toffee.ui.challenge

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentChallengeResultBinding
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.util.unsafeLazy
import kotlinx.android.synthetic.main.fragment_challenge_result.*

class ChallengeChannelFragment : Fragment(), OnClickListener {

    private lateinit var binding: FragmentChallengeResultBinding
    private val viewModel by unsafeLazy { ViewModelProviders.of(this).get(ChallengeResultViewModel::class.java) }
    
    companion object {
        @JvmStatic
        fun newInstance() =
            ChallengeChannelFragment()
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_challenge_result, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.joinButton.visibility = VISIBLE
        binding.watchEntryButton.visibility = GONE
        
        observeData()
        viewModel.loadData()
        binding.joinButton.setOnClickListener(this)
    }

    private fun observeData() {
        observe(viewModel.liveData){
            when(it){
                is Success -> {
                    
                }
                is Failure -> {
                    
                }
            }
        }
    }

    override fun onClick(v: View?) {
        if (v == joinButton){
            findNavController().navigate(R.id.action_challengeResultFragment_to_challengeDetailFragment)
        }
    }
}