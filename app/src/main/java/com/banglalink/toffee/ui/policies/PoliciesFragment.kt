package com.banglalink.toffee.ui.policies

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.data.repository.UserActivitiesRepository
import com.banglalink.toffee.databinding.FragmentPoliciesBinding
import com.banglalink.toffee.ui.common.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PoliciesFragment : BaseFragment() {
    
    private var _binding: FragmentPoliciesBinding? = null
    private val binding get() = _binding!!
    @Inject lateinit var userActivitiesRepository: UserActivitiesRepository
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPoliciesBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setPrefItemListener()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    private fun setPrefItemListener() {
        binding.prefPolicies.setOnClickListener { onClickCreatorsPolicy() }
        binding.prefPrivacy.setOnClickListener { onClickPrivacyPolicy() }
        binding.prefTerms.setOnClickListener { onClickTermsAndConditions() }
    }

    private fun onClickCreatorsPolicy() {
        val args = Bundle().apply {
            putString("myTitle", "Creators Policy")
            putString("url", mPref.creatorsPolicyUrl)
        }
        findNavController().navigate(R.id.termsAndConditionFragment, args)
    }
    
    private fun onClickPrivacyPolicy() {
        val args = Bundle().apply {
            putString("myTitle", "Privacy Policy")
            putString("url", mPref.privacyPolicyUrl)
        }
        findNavController().navigate(R.id.privacyPolicyFragment, args)
    }

    private fun onClickTermsAndConditions() {
        val args = Bundle().apply {
            putString("myTitle", "Terms & Conditions")
            putString("url", mPref.termsAndConditionUrl)
        }
        findNavController().navigate(R.id.termsAndConditionFragment, args)
    }
}