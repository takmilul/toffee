package com.banglalink.toffee.ui.challenge

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentChallengeResultBinding
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success

class ChallengeEntrySubmittedFragment : Fragment(), OnClickListener {

    private lateinit var binding: FragmentChallengeResultBinding
    private val viewModel by viewModels<ChallengeResultViewModel>()
    
    companion object {
        @JvmStatic
        fun newInstance() =
            ChallengeEntrySubmittedFragment()
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentChallengeResultBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.joinButton.visibility = GONE
        binding.watchEntryButton.visibility = VISIBLE
        
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
        if (v == binding.joinButton){
            findNavController().navigate(R.id.action_challengeResultFragment_to_challengeDetailFragment)
        }
    }
}