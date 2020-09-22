package com.banglalink.toffee.ui.earnings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentWithdrawalSuccessBinding
import kotlinx.android.synthetic.main.fragment_withdrawal_success.*

class WithdrawalSuccessFragment : Fragment(), OnClickListener {
    private lateinit var binding: FragmentWithdrawalSuccessBinding

    companion object {
        @JvmStatic
        fun newInstance() =
            WithdrawalSuccessFragment()
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_withdrawal_success, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.backToTransactionButton.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if (v == backToTransactionButton){
            findNavController().navigate(R.id.action_withdrawalSuccessFragment_to_earningsFragment)
        }
    }
}