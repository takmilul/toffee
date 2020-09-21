package com.banglalink.toffee.ui.earnings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.R.layout
import com.banglalink.toffee.databinding.FragmentReviewWithdrawalBinding
import com.banglalink.toffee.model.ReviewWithdrawal
import com.banglalink.toffee.util.unsafeLazy
import kotlinx.android.synthetic.main.fragment_review_withdrawal.*

class ReviewWithdrawalFragment : Fragment(), OnClickListener {

    private lateinit var reviewWithdrawal: ReviewWithdrawal
    private lateinit var binding: FragmentReviewWithdrawalBinding
    private val viewModel by unsafeLazy {
        ViewModelProviders.of(this).get(ReviewWithdrawalViewModel::class.java)
    }

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
        binding = DataBindingUtil.inflate(inflater, layout.fragment_review_withdrawal, container, false)
        binding.lifecycleOwner = this
        binding.data = reviewWithdrawal
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.confirmButton.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if (v == confirmButton){
            findNavController().navigate(R.id.action_reviewWithdrawalFragment_to_withdrawalSuccessFragment)
        }
    }
}