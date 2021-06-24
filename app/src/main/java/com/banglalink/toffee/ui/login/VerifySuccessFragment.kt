package com.banglalink.toffee.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.banglalink.toffee.databinding.FragmentVerifySuccessBinding
import com.banglalink.toffee.extension.safeClick
import com.banglalink.toffee.ui.common.ChildDialogFragment

class VerifySuccessFragment : ChildDialogFragment() {
    private var _binding: FragmentVerifySuccessBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentVerifySuccessBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.okButton.safeClick({
            closeDialog()
            requireActivity().recreate()
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}