package com.banglalink.toffee.ui.points

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentRedeemPointsSuccessBinding

class RedeemPointsSuccessFragment : Fragment() {
    
    lateinit var binding: FragmentRedeemPointsSuccessBinding
    
    companion object {
        fun createInstance(successMsg: String): RedeemPointsSuccessFragment{
            val instance = RedeemPointsSuccessFragment()
            val bundle = Bundle()
            bundle.putString("message", successMsg)
            instance.arguments = bundle
            return instance
        } 
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRedeemPointsSuccessBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val successMsg = arguments?.getString("message")
        binding.successMsgTextView.text = successMsg
    }
}