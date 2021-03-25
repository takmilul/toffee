package com.banglalink.toffee.ui.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentSigninBinding

class SignInFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance() =
            SignInFragment()
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?, ): View {
        val binding = FragmentSigninBinding.inflate(inflater, container, false)
        return binding.root
    }
}