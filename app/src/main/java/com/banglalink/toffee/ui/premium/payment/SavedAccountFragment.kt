package com.banglalink.toffee.ui.premium.payment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentSavedAccountBinding
import com.banglalink.toffee.ui.common.BaseFragment

class SavedAccountFragment : BaseFragment() {
    private lateinit var binding:FragmentSavedAccountBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_saved_account, container, false)
        binding = FragmentSavedAccountBinding.inflate(inflater, container, false)
        return binding.root
    }
}