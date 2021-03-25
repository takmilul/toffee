package com.banglalink.toffee.ui.earnings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentReviewWithdrawalBinding
import com.banglalink.toffee.model.ReviewWithdrawal

class ReviewWithdrawalFragment : Fragment(), OnClickListener {

    private lateinit var reviewWithdrawal: ReviewWithdrawal
    private var _binding: FragmentReviewWithdrawalBinding ? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<ReviewWithdrawalViewModel>()

    companion object {
        @JvmStatic
        fun newInstance(): ReviewWithdrawalFragment {
            return ReviewWithdrawalFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = ReviewWithdrawalFragmentArgs.fromBundle(requireArguments())
        val amount = args.withdrawalAmount
        val paymentMethod = args.paymentMethod
        reviewWithdrawal = ReviewWithdrawal(amount, paymentMethod.bankName, paymentMethod.accountName, paymentMethod.accountNumber)
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentReviewWithdrawalBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.data = reviewWithdrawal
        return binding.root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.confirmButton.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if (v == binding.confirmButton){
            findNavController().navigate(R.id.action_reviewWithdrawalFragment_to_withdrawalSuccessFragment)
        }
    }
}