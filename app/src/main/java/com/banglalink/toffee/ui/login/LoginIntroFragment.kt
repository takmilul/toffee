package com.banglalink.toffee.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentLoginIntroBinding
import com.banglalink.toffee.extension.safeClick
import com.banglalink.toffee.ui.common.ChildDialogFragment

class LoginIntroFragment : ChildDialogFragment() {
    private var _binding: FragmentLoginIntroBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoginIntroBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.signInButton.safeClick({
            findNavController().navigate(R.id.action_loginIntroFragment_to_loginContentFragment2)
//            findNavController().navigate(R.id.userInterestFragment2)
        })

        binding.skipSignIn.safeClick({ closeDialog() })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}